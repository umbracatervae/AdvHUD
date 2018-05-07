/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pt.advHUD;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.Tango.OnTangoUpdateListener;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoInvalidException;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Main Activity class for the Motion Tracking API Sample. Handles the connection to the Tango
 * service and propagation of Tango pose data Layout view.
 */
public class TangoMainActivity extends Activity {

    private static final String TAG = TangoMainActivity.class.getSimpleName();
    private Tango mTango;
    private TangoConfig mConfig;
    private float yaw = 0;
    private float pitch = 0;
    private float roll = -300; //bogus value so NULLPTREXCEPTION doesn't occur
    private float qx;
    private float qy;
    private float qz;
    private float qw;
    private float x;
    private float y;
    private float translation[] = new float[3];
    private float orientation[] = new float[4];
    float rotMatrix[] = new float[9];
    float euOrient[] = new float[3];
    private MapDrawable mapDrawable = new MapDrawable(1234);
    private ImageView mapView;
    private Button mRecordButton;
    private Button mFriendButton;
    private boolean recording = false;
    private boolean friendly = false;
    private File poseData;
    private BufferedReader friendlyReader;
    private TextView transViewX;
    private TextView transViewY;


    //Setup new thread to control UI view updates --> THIS IS A BIT SLOW WARNING!
    Thread updateTextViewThread = new Thread(){
        public void run(){
            while(true){
                TangoMainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(roll != -300 && translation[0] != 0 && translation[1] != 0 && translation[2] != 0) {
                            mapView.invalidate();
                            mapDrawable.setDegreeRotation((int)(-1*roll));
                            if(friendly){
                                try {
                                    if(friendlyReader.ready()) {
                                        mapDrawable.updateFriendlyInfo(poseData, friendlyReader);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            int cx = (int)((MapDrawable.width/MapDrawable.metricRangeTangoX)*(translation[0]+MapDrawable.metricRangeTangoX/2));
                            int cy = (int)(MapDrawable.height-((MapDrawable.height/MapDrawable.metricRangeTangoY)*(translation[1]+MapDrawable.metricRangeTangoY/2)));
                            mapDrawable.moveX = -1*(cx-(MapDrawable.width/2));
                            mapDrawable.moveY = -1*(cy-(MapDrawable.height/2));
                            mapDrawable.appendPathPoint(new Coordinate(cx,cy));
                            //transViewX.setText(String.valueOf(translation[0]));
                            //transViewY.setText(String.valueOf(translation[1]));

                        }
                    }
                });
                try {
                    Thread.sleep(10); //INCREASED BECAUSE RECORDING GOES TOO SLOWLY!! --> MAY NEED TO BE PUT INTO A DIFFERENT THREAD IF DYNAMIC WALL LIST TAKES TOO MUCH RESOURCE POWER
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motion_tracking);

        //"bogus" values so that NULLPTREXCEPTION doesn't occur
        translation[0] = 0;
        translation[1] = 0;
        translation[2] = 0;

        mapView = (ImageView)findViewById(R.id.mapView);
        mRecordButton = (Button)findViewById(R.id.recordButton);
        mFriendButton = (Button)findViewById(R.id.friendButton);

        mapView.setImageDrawable(mapDrawable);
        //updateTextViewThread.setPriority(Thread.MAX_PRIORITY); //CHANGE THIS WHEN ADDING NEW CODE
        updateTextViewThread.start();

        //Recording Pose Data via RecordButton
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!recording) {
                    Toast.makeText(TangoMainActivity.this,"Recording Pose Data Enabled",Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(TangoMainActivity.this,"Recording Pose Data Disabled",Toast.LENGTH_LONG).show();
                }
                recording = !recording;
            }
        });

        //Friendly Mode Enable/Disable
        mFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!friendly) {
                    Toast.makeText(TangoMainActivity.this,"Friendlies Displayed",Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(TangoMainActivity.this,"Friendlies Not Displayed",Toast.LENGTH_LONG).show();
                }
                friendly = !friendly;
                mapDrawable.setMultipleMode(friendly);
            }
        });

        //Recording pose information into a text file and then read out the data

        poseData = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "RecordData.txt");
        if(!poseData.exists()){
            try {
                poseData.createNewFile();
                Toast.makeText(TangoMainActivity.this, "RecordData.txt Created", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            friendlyReader = new BufferedReader(new FileReader(poseData));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //Recording pose information into a text file and then read out the data (For second recording)

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Initialize Tango Service as a normal Android Service, since we call mTango.disconnect()
        // in onPause, this will unbind Tango Service, so every time when onResume gets called, we
        // should create a new Tango object.
        mTango = new Tango(TangoMainActivity.this, new Runnable() {
            // Pass in a Runnable to be called from UI thread when Tango is ready, this Runnable
            // will be running on a new thread.
            // When Tango is ready, we can call Tango functions safely here only when there is no UI
            // thread changes involved.
            @Override
            public void run() {
                synchronized (TangoMainActivity.this) {
                    try {
                        mConfig = setupTangoConfig(mTango);
                        mTango.connect(mConfig);
                        startupTango();
                    } catch (TangoOutOfDateException e) {
                        Log.e(TAG, getString(R.string.exception_out_of_date), e);
                        showsToastAndFinishOnUiThread(R.string.exception_out_of_date);
                    } catch (TangoErrorException e) {
                        Log.e(TAG, getString(R.string.exception_tango_error), e);
                        showsToastAndFinishOnUiThread(R.string.exception_tango_error);
                    } catch (TangoInvalidException e) {
                        Log.e(TAG, getString(R.string.exception_tango_invalid), e);
                        showsToastAndFinishOnUiThread(R.string.exception_tango_invalid);
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        synchronized (this) {
            try {
                mTango.disconnect();
            } catch (TangoErrorException e) {
                Log.e(TAG, getString(R.string.exception_tango_error), e);
            }
        }
    }

    /**
     * Sets up the tango configuration object. Make sure mTango object is initialized before
     * making this call.
     */
    private TangoConfig setupTangoConfig(Tango tango) {
        // Create a new Tango Configuration and enable the TangoMainActivity API.
        TangoConfig config = tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_MOTIONTRACKING, true);

        // Tango service should automatically attempt to recover when it enters an invalid state.
        config.putBoolean(TangoConfig.KEY_BOOLEAN_AUTORECOVERY, true);
        return config;
    }

    /**
     * Set up the callback listeners for the Tango service and obtain other parameters required
     * after Tango connection.
     * Listen to new Pose data.
     */
    private void startupTango() {
        // Lock configuration and connect to Tango
        // Select coordinate frame pair
        final ArrayList<TangoCoordinateFramePair> framePairs =
                new ArrayList<TangoCoordinateFramePair>();
        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                TangoPoseData.COORDINATE_FRAME_DEVICE));

        // Listen for new Tango data
        mTango.connectListener(framePairs, new OnTangoUpdateListener() {
            @Override
            public void onPoseAvailable(final TangoPoseData pose) {
                updateLocation(pose);
            }

            @Override
            public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
                // We are not using onXyzIjAvailable for this app.
            }

            @Override
            public void onPointCloudAvailable(TangoPointCloudData pointCloud) {
                // We are not using onPointCloudAvailable for this app.
            }

            @Override
            public void onTangoEvent(final TangoEvent event) {
                // Ignoring TangoEvents.
            }

            @Override
            public void onFrameAvailable(int cameraId) {
                // We are not using onFrameAvailable for this application.
            }
        });
    }

    /**
     * Log the Position and Orientation of the given pose in the Logcat as information.
     *
     * @param pose the pose to log.
     */
    private void updateLocation(TangoPoseData pose) {
        StringBuilder stringBuilder = new StringBuilder();
        translation = pose.getTranslationAsFloats();
        stringBuilder.append("Position: " +translation[0] + ", " + translation[1] + ", " + translation[2]);
        orientation = pose.getRotationAsFloats();
        stringBuilder.append(". Orientation: " +
                orientation[0] + ", " + orientation[1] + ", " +
                orientation[2] + ", " + orientation[3]+"\n");
        qw = orientation[0];
        qx = orientation[1];
        qy = orientation[2];
        qz = orientation[3];
        //Extract Rotation Matrix
        rotMatrix[0] = 1-2*(qy*qy)-2*(qz*qz);
        rotMatrix[1] = (2*qx*qy)+(2*qz*qw);
        rotMatrix[2] = (2*qx*qz)-(2*qy*qw);
        rotMatrix[3] = (2*qx*qy)-(2*qz*qw);
        rotMatrix[4] = 1-(2*qx*qx)-(2*qz*qz);
        rotMatrix[5] = (2*qy*qz)+(2*qx*qw);
        rotMatrix[6] = (2*qx*qz)+(2*qy*qw);
        rotMatrix[7] = (2*qy*qz)-(2*qx*qw);
        rotMatrix[8] = 1-(2*qx*qx)-(2*qy*qy);
        //Get orientation information
        SensorManager.getOrientation(rotMatrix,euOrient);
        roll = (float)Math.toDegrees(euOrient[2]);
        //stringBuilder.append("Yaw: "+yaw+"\nPitch: "+pitch+"\nRoll: "+roll);
        //Log.i(TAG, stringBuilder.toString());


        //File IO Test
        FileWriter fw = null;
        BufferedWriter bw = null;

        if(recording){

                try {
                    fw = new FileWriter(poseData,true);
                    bw = new BufferedWriter(fw);
                    bw.write(stringBuilder.toString());

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i(TAG,"FILE ERROR!");
                } finally {
                    try {
                        if (bw != null) {
                            bw.close();
                        }

                        if (fw != null) {
                            fw.close();
                        }
                    }
                    catch (IOException e){
                        e.printStackTrace();
                        Log.i(TAG,"FILE CLOSE ERROR!");
                    }
                }

        }

    }

    /**
     * Display toast on UI thread.
     *
     * @param resId The resource id of the string resource to use. Can be formatted text.
     */
    private void showsToastAndFinishOnUiThread(final int resId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TangoMainActivity.this,
                        getString(resId), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

}


