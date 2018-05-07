package pt.advHUD;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Debug;
import android.os.Process;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class maintains a bluetooth connection with the moverio BT-200
 * it packages wall objects into bluetooth packets containing double arrays
 *
 * To use: create a TangoBluetooth object and pass it a BluetoothAdapter
 * call the connect() method to connect
 * call the send(double[]) method to send data to moverio
 * call the disconnect() method to disconnect
 *
 * Created by Paul on 3/20/2017.
 */

public class TangoBluetooth {
    private final String DEBUG_TAG = "TangoBluetooth";
    private final UUID MY_UUID = UUID.fromString("55ba6a24-f236-11e6-bc64-92361f002671");
    private final static Double FRAME_START = Double.NEGATIVE_INFINITY;
    private final static Double FRAME_DELIMITER = Double.MAX_VALUE;
    private final static Double FRAME_END = Double.POSITIVE_INFINITY;

    private final BluetoothAdapter btAdapter;
    private final BluetoothDevice btDevice;
    private final BlockingQueue<Runnable> threadQueue;

    private final ThreadPoolExecutor threadPool;
    private BluetoothSocket socket;
    private DataInputStream inStream;
    private DataOutputStream outStream;

    private ReentrantReadWriteLock lock; //mutex lock

    private boolean connected = false;

    public TangoBluetooth( BluetoothAdapter adapter){
        btAdapter = adapter;
        threadQueue = new LinkedBlockingQueue<Runnable>();
        int processor = Runtime.getRuntime().availableProcessors();
        threadPool = new ThreadPoolExecutor(processor, processor, 30, TimeUnit.SECONDS, threadQueue);
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        BluetoothDevice deviceTmp = null;
        lock = new ReentrantReadWriteLock();

        for (BluetoothDevice d : pairedDevices) {
            deviceTmp = d; //todo make this more dynamic
            break;
        }
        if(deviceTmp == null ){
            Log.e(DEBUG_TAG, "No paired devices found.");
        }
        btDevice = deviceTmp;
        connected = false;
    }

    public void connect() {
        if (!btAdapter.isEnabled()) {
            Log.e(DEBUG_TAG, "Bluetooth adapter not enabled. Cannot proceed");
            return;
        } else {
            String DEBUG_TAG = "ConnectThread";

            BluetoothSocket tmp = null;

            try {
                tmp = btDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "Socket's create() method failed", e);
            }
            socket = tmp;
            Log.d(DEBUG_TAG, "Created RFCommSocket with info: " + socket.toString());

            btAdapter.cancelDiscovery();
            try {
                socket.connect();
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "Unable to connect, closing socket");
                try {
                    socket.close();
                    return;
                } catch (IOException c) {
                    Log.e(DEBUG_TAG, "Could not close the client socket", c);
                }

            }

            DataInputStream tmpIn = null;
            DataOutputStream tmpOut = null;

            Log.d(DEBUG_TAG, "Attempting to create input and output streams");
            try {
                tmpIn = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "Error occurred when creating output stream", e);
            }

            inStream = tmpIn;
            outStream = tmpOut;
            connected = true;
        }

    }
    public void disconnect(){
        if(connected) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "Unable to close socket");
            }



            connected = false;
        }
        else{
            Log.e(DEBUG_TAG, "Attempted to disconnect bluetooth which was already disconnected.");
        }

    }

    //TODO make this run in background
    public void send(Double[] walls){ //array should be in format {x1, y1, x2, y2, ... }
        if(!connected){
            Log.e(DEBUG_TAG, "Bluetooth must be connected before sending.");
        }
        else{
            threadPool.execute(new Sender(walls));
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public class Sender implements Runnable {

        Double[] myWalls;

        public Sender(Double[] walls){
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            myWalls = walls;
        }
        @Override
        public void run(){
            lock.readLock().lock();
            try {
                for (int i = 0; i < myWalls.length; i++) {
                    try {
                        outStream.writeDouble(myWalls[i]);
                        //Log.d("Sender", "Sending Double: " + myWalls[i]);
                    } catch (IOException e) {
                        Log.e(DEBUG_TAG, "Unable to write Double to outStream. Attempting to reconnect...");
                        try {
                            socket.close();
                            connected = false;
                        } catch (IOException e1) {
                            Log.e(DEBUG_TAG,"Unable to close socket after dropped connection");
                        }
                        return;
                    }
                }
                try {
                    outStream.flush();
                } catch (IOException e) {
                    Log.e(DEBUG_TAG, "Problem flushing outStream.");
                }
            } finally {
                lock.readLock().unlock();
            }
            /*
            if(wallBuffer.size() > 0) {
                lock.readLock().lock();
                try {
                    while (wallBuffer.size() > 0) {
                        try {
                            Double d = wallBuffer.remove();
                            Log.d("Sender", "Sending Double: " + d);
                            outStream.writeDouble(d);
                            outStream.flush();
                        } catch (IOException e) {
                            Log.e(DEBUG_TAG, "Unable to write Double to outStream");
                        }
                    }
                }
                finally {
                     lock.readLock().unlock();
                }
            }
            */
        }
    }
    public static Double[] makeFrame(ArrayList<Double> orientation, ArrayList<Wall2D> walls){
        ArrayList<Double> dataFrame = new ArrayList<>();
        dataFrame.add(FRAME_START);
        if(orientation.size()%7 != 0){
            Log.e("MakeFrame","The position/orientation data segment does not contain 7 doubles.");
        }
        for (Double d : orientation){
            dataFrame.add(d);
        }
        dataFrame.add(FRAME_DELIMITER);
        Double[] tmp;

        for (Wall2D w : walls) {
            tmp = w.sendData();
            //add the 4 doubles in the walls vector
            for (int i=0; i<4; i++){
                dataFrame.add(tmp[i]);
            }
        }
        dataFrame.add(FRAME_END);

        Double[] doubles = new Double[dataFrame.size()];
        for (int i = 0; i < dataFrame.size(); i++) {
            doubles[i] = dataFrame.get(i);
        }
        return doubles;
    }
}
