package hud.advancedhud_moverio;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MainActivity extends AppCompatActivity {

    MoverioBluetooth btManager;
    BluetoothAdapter btAdapter;

    private final BlockingQueue<Runnable> threadQueue;
    private final ThreadPoolExecutor threadPool;
    private ReentrantReadWriteLock lock; //mutex lock

    private final int BT_ENABLE_REQUEST_INIT = 0;
    private final int BT_ENABLE_REQUEST_CONNECT = 1;

    private MapDrawable mapDrawable = new MapDrawable();
    private ImageView mapView;
    public ArrayList<Wall2D> mWallList = new ArrayList<Wall2D>();
    public boolean readyFlag = false;
    //public float[] translation = new float[3];
    //public float[] orientation = new float[4];
    public ArrayList<float[]> translations = new ArrayList<>();
    public ArrayList<float[]> orientations = new ArrayList<>();
    float rotMatrix[] = new float[9];
    float euOrient[] = new float[3];
    private float roll = -300; //bogus value so NULLPTREXCEPTION doesn't occur
    private float qx;
    private float qy;
    private float qz;
    private float qw;

    //for testing
    //private TextView transView;
    //private TextView orientView;
    //private String displayTrans;
    //private String displayOrient;

    Thread updateTextViewThread = new Thread() {
        public void run() {
            while (true) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (readyFlag) {
                            lock.readLock().lock();
                            try {
                                mapView.invalidate();
                                mapDrawable.setDynamicWallArray(mWallList);
                                mapDrawable.setDegreeRotation((int) (-roll));
                                int cx = (int) ((MapDrawable.width / MapDrawable.metricRangeTango) * (translations.get(0)[0] + MapDrawable.metricRangeTango / 2));
                                int cy = (int) (MapDrawable.height - ((MapDrawable.height / MapDrawable.metricRangeTango) * (translations.get(0)[1] + MapDrawable.metricRangeTango / 2)));
                                mapDrawable.moveX = -1 * (cx - (MapDrawable.width / 2));
                                mapDrawable.moveY = -1 * (cy - (MapDrawable.height / 2));
                                mapDrawable.appendPathPoint(new Coordinate(cx, cy));
                            }
                            finally{
                                lock.readLock().unlock();
                            }
                        }
                    }
                });
                try {
                    Thread.sleep(100); //10Hz Refresh Rate
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    public MainActivity() {
        threadQueue = new LinkedBlockingQueue<Runnable>();
        int processor = Runtime.getRuntime().availableProcessors();
        threadPool = new ThreadPoolExecutor(processor, processor, 30, TimeUnit.SECONDS, threadQueue);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lock = mapDrawable.getWallListLock();

        //getActionBar().hide();

        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        //winParams.flags |= WindowManager.LayoutParams.FLAG_SMARTFULLSCREEN;
        winParams.flags |= 0x80000000;
        win.setAttributes(winParams);

        mapView = (ImageView) findViewById(R.id.mapView);
        mapView.setImageDrawable(mapDrawable);

        //for debug
        //transView = (TextView)findViewById(R.id.translationView);
        //orientView = (TextView)findViewById(R.id.orientView);

        //follow for loop and assigment statement is to initialize the first orientation and translation arrays to zeroes
        translations.add(new float[3]);
        orientations.add(new float[4]);
        for (float t : translations.get(0)){
            t = 0;
        }
        for (float o : orientations.get(0)){
            o = 0;
        }

        //start the mapview updating thread
        updateTextViewThread.start();

        startBluetoothAdapter();
        btManager = new MoverioBluetooth(btAdapter);

        btManager.connect();
        if (btAdapter.isEnabled()) {
            threadPool.execute(new DataFetcher());
        }


    }

    public class DataFetcher implements Runnable {
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            while (btAdapter.isEnabled()) {
                if (btManager.isConnected() && !btManager.isConnecting()) {
                    Wall2D[] wallList = btManager.getData();
                    Double[] oL = btManager.getOrientation();
                    lock.writeLock().lock();
                    try {
                        if (wallList != null && oL != null) {
                            mWallList.clear();
                            //Log.d("DataFetcher", "Received wallList: Length: " + wallList.length);
                            for (int i = 0; i < wallList.length; i++) {
                                mWallList.add(wallList[i]);
                            }
                            while ( oL.length / 7 > translations.size()){
                                translations.add(new float[3]);
                                orientations.add(new float[4]);
                            }
                            for(int i = 0; i < oL.length/7; i++) {
                                translations.get(i)[0] = oL[7*i+0].floatValue();
                                translations.get(i)[1] = oL[7*i+1].floatValue();
                                translations.get(i)[2] = oL[7*i+2].floatValue();
                                orientations.get(i)[0] = oL[7*i+3].floatValue();
                                orientations.get(i)[1] = oL[7*i+4].floatValue();
                                orientations.get(i)[2] = oL[7*i+5].floatValue();
                                orientations.get(i)[3] = oL[7*i+6].floatValue();
                                //Log.d("DataFetcher", "Got position (" + translation[0] + "," + translation[1] + ")");
                            }
                            updateLocation();
                            readyFlag = true;
                        }
                    } finally {
                        lock.writeLock().unlock();
                    }
//                    if (walls != null) {
//                        for (int i = 0; i < walls.length; i++) {
//                            //Log.d("Receiving", "Got wall data: " + walls[i]);
//                            Double[] w = walls[i].getDoubleArray();
//                            Log.d("Receiving", "Wall " + i + " has Start = (" + w[0] + "," + w[1] + ")  End = (" + w[2] + "," + w[3] + ")");
//                        }
//                    }
                }
                //attempt to reconnect if the manager is neither connected nor currently attempting to connect
                else if (!btManager.isConnected() && !btManager.isConnecting()) {
                    //upon reconnection, forget the previous path
                    mapDrawable.clearPathHistory();
                    btManager.connect();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BT_ENABLE_REQUEST_INIT) {
            if (resultCode == RESULT_OK) {
                Log.d("Activity Result", "Bluetooth Enabled by User");
                btManager.connect();
                threadPool.execute(new DataFetcher());
            } else {
                Log.e("ActivityResult", "Bluetooth is Disabled.");
            }
        }
        if (requestCode == BT_ENABLE_REQUEST_CONNECT) {
            if (resultCode == RESULT_OK) {
                btManager.connect(); //now that bt is enabled we attempt to connect again
                threadPool.execute(new DataFetcher());
            } else {
                Log.e("ActivityResult", "Attempting to connect but bluetooth STILL not enabled");
            }
        }
    }

    protected void startBluetoothAdapter() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Log.e("StartBluetoothAdapter", "Unable to get BluetoothAdapter");
        } else if (!btAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, BT_ENABLE_REQUEST_INIT);
        }
    }

    private void updateLocation() {
        //for testing
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        //sb1.append("Position: "+translation[0]+", "+translation[1]+", "+translation[2]);
        //sb2.append("Orientation: "+orientation[0]+", "+orientation[1]+", "+orientation[2]+", "+orientation[3]);
        //displayTrans = sb1.toString();
        //displayOrient = sb2.toString();
        //for debug
        sb1.append(sb2.toString());
        Log.i(MainActivity.class.getSimpleName(),sb1.toString());
        //obtaining rotation matrix using quaternion notation

        qw = orientations.get(0)[0];
        qx = orientations.get(0)[1];
        qy = orientations.get(0)[2];
        qz = orientations.get(0)[3];
        //Extract Rotation Matrix
        rotMatrix[0] = 1 - 2 * (qy * qy) - 2 * (qz * qz);
        rotMatrix[1] = (2 * qx * qy) + (2 * qz * qw);
        rotMatrix[2] = (2 * qx * qz) - (2 * qy * qw);
        rotMatrix[3] = (2 * qx * qy) - (2 * qz * qw);
        rotMatrix[4] = 1 - (2 * qx * qx) - (2 * qz * qz);
        rotMatrix[5] = (2 * qy * qz) + (2 * qx * qw);
        rotMatrix[6] = (2 * qx * qz) + (2 * qy * qw);
        rotMatrix[7] = (2 * qy * qz) - (2 * qx * qw);
        rotMatrix[8] = 1 - (2 * qx * qx) - (2 * qy * qy);
        //Get orientation information
        SensorManager.getOrientation(rotMatrix, euOrient);
        roll = (float) Math.toDegrees(euOrient[2]);


    }
}
