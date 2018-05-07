package paul.btreliability;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    TextView mainText;

    Handler mHandler;
    private final BlockingQueue<Runnable> threadQueue;
    private final ThreadPoolExecutor threadPool;

    //ServerThread serverThread;
    //ClientThread clientThread;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice btDevice;

    Double oldDouble;
    Double receivedDouble;
    int missedDoubles;
    Double avgDoubleRate;
    long timeStamp;
    int totalDoubles;

    int sleepTime = 500; //milliseconds for the sender

    private final int BT_ENABLE_REQUEST_RESULT_CODE = 0;
    enum Mode {
        SERVER, CLIENT;
    }
    private final Mode setupMode = Mode.CLIENT;

    private interface MessageConstants {
        int MESSAGE_READ = 0;
        int MESSAGE_SEND = 1;
        // ... (Add other message types here as needed.)
    }

    public MainActivity(){

        missedDoubles = 0;
        totalDoubles = 0;
        avgDoubleRate = 0.0;
        oldDouble = 0.0;
        threadQueue = new LinkedBlockingQueue<Runnable>();
        int processor = Runtime.getRuntime().availableProcessors();
        threadPool = new ThreadPoolExecutor(processor, processor, 30, TimeUnit.SECONDS, threadQueue);
        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message inputMessage){
                if (inputMessage.what ==  MessageConstants.MESSAGE_READ){
                    receivedDouble = (Double) inputMessage.obj;
                    Double discrepancy = Math.ceil(100*(receivedDouble - oldDouble - 0.01))/100;
                    if(discrepancy != 0.0){
                        missedDoubles++;
                    }
                    oldDouble = receivedDouble;
                    totalDoubles++;
                    avgDoubleRate = (0.8)*avgDoubleRate + (0.2)*(System.currentTimeMillis() - timeStamp);
                    timeStamp = System.currentTimeMillis();
                    mainText.setText("Got Double: " + receivedDouble + "\nMissed Packets: " + missedDoubles + "\nTotal Packets: " + totalDoubles + "\nDiscrepancy: " + discrepancy  + "\nAvg Rate: " + avgDoubleRate);
                }
                else if (inputMessage.what == MessageConstants.MESSAGE_SEND){
                    receivedDouble = (Double) inputMessage.obj;
                    mainText.setText("Sending Double: " + receivedDouble);
                }
                else {
                    Log.e("Message Handler", "Didn't get an expected messageConstant");
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainText = (TextView) findViewById(R.id.textview_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            Log.e("MainActivity","Device does not appear to support bluetooth.");
        }
        if(!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BT_ENABLE_REQUEST_RESULT_CODE);
        }
        else {
            listPairedDevices();
            startCommunication();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == BT_ENABLE_REQUEST_RESULT_CODE){
            if(resultCode == RESULT_OK){
                Log.d("ActivityResult","Bluetooth Enabled by User");
                listPairedDevices();
                startCommunication();
            }
            else {
                Log.e("ActivityResult","Bluetooth remains disabled.");
            }
        }
    }

    protected void startCommunication(){
        if(setupMode == Mode.CLIENT) {
            Log.d("ActivityResult", "Starting thread as server");
            //clientThread = new ClientThread(btDevice);
            //threadPool.execute(clientThread);

            threadPool.execute(new ClientThread(btDevice));
        }
        else if (setupMode == Mode.SERVER){
            Log.d("ActivityResult", "Starting thread as client");
            //serverThread = new ServerThread();
            //threadPool.execute(serverThread);

            threadPool.execute(new ServerThread());
        }
        else {
            Log.e("StartCommunication","Unknown mode set. Cannot start comm threads");
        }
    }

    //TODO: prompt user to pair to the correct device
    private void listPairedDevices(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                mainText.setText(mainText.getText() + "\n" + deviceName + "\t" + deviceHardwareAddress);

                //go ahead and set the only device (0) to the main device
                //TODO: make this dynamic
                btDevice = device;
            }
        }
    }
    private class ServerThread implements Runnable{
        String NAME = "BT TestServer";
        UUID MY_UUID = UUID.fromString("55ba6a24-f236-11e6-bc64-92361f002671"); //TODO: make better uuid stuff
        String DEBUG_TAG = "ServerThread";

        private final BluetoothServerSocket serverSocket;

        public ServerThread(){
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            Log.d(DEBUG_TAG, "Server Thread Starting");

            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "Socket's listen() method failed", e);
            }
            serverSocket = tmp;
        }
        public void run() {
            BluetoothSocket socket = null;
            Log.d(DEBUG_TAG, "Waiting to accept socket");
            while (true) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    Log.e(DEBUG_TAG, "Socket's accept() method failed", e);
                    break;
                }
                if (socket != null) {
                    Log.d(DEBUG_TAG, "A connection was accepted.");
                    threadPool.execute( new ReceiverThread(socket) );
                    cancel();
                }
            }
        }
        private void cancel(){
            Log.d(DEBUG_TAG, "Cancel method called. Closing socket and exiting thread.");
            threadPool.remove(this);
            //try {
            //    serverSocket.close();
            //} catch (IOException e) {
            //    Log.e(DEBUG_TAG, "Could not close the connect socket", e);
            //}
        }
    }
    private class ClientThread implements Runnable{
        private final BluetoothSocket socket;
        private final BluetoothDevice device;
        private final UUID MY_UUID = UUID.fromString("55ba6a24-f236-11e6-bc64-92361f002671");

        String DEBUG_TAG = "ClientThread";

        public ClientThread(BluetoothDevice d){
            device = d;
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e){
                Log.e(DEBUG_TAG, "Socket's create() method failed", e);
            }
            socket = tmp;
            Log.d(DEBUG_TAG, "Created RfCommSocket with info: " + socket.toString());
            threadPool.execute(new SenderThread(socket));

            cancel();
        }

        @Override
        public void run() {
            mBluetoothAdapter.cancelDiscovery();

            try {
                socket.connect();
            } catch (IOException connectException){
                Log.e(DEBUG_TAG, "Unable to connect, closing socket", connectException);
                try {
                    socket.close();
                } catch (IOException closeException){
                    Log.e(DEBUG_TAG, "Could not close the client socket", closeException);
                }
                return;
            }

        }

        private void cancel(){
            Log.d(DEBUG_TAG, "Cancel method called. Closing socket and exiting thread.");
            threadPool.remove(this);
            //try {
            //    socket.close();
            //} catch (IOException e) {
            //    Log.e(DEBUG_TAG, "Could not close the connect socket", e);
            //}
        }
    }

    private class SenderThread implements Runnable {
        private final String DEBUG_TAG = "SenderThread";
        private final BluetoothSocket btSocket;
        private final DataInputStream inStream;
        private final DataOutputStream outStream;
        //private byte[] streamBuffer;

        public SenderThread(BluetoothSocket socket) {
            btSocket = socket;
            DataInputStream tmpIn = null;
            DataOutputStream tmpOut = null;

            Log.d(DEBUG_TAG, "Attempting to create input and output streams");
            try {
                tmpIn = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "Error occurred when creating input stream",e);
            }
            try {
                tmpOut = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "Error occurred when creating output stream",e);
            }

            inStream = tmpIn;
            outStream = tmpOut;
        }

        public void run(){
            //streamBuffer = new byte[1024];
            Double testDouble = 0.0;
            Message sent;

            Log.d(DEBUG_TAG, "Connection initiated. Sending data.");

            while(true) {

                send(testDouble);
                sent = mHandler.obtainMessage(
                        MessageConstants.MESSAGE_SEND, testDouble
                );
                sent.sendToTarget();
                testDouble = Math.ceil(100*(testDouble + 0.01))/100;
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        private void send(Double d){
            if(outStream != null && d != null) {
                try {
                    outStream.writeDouble(d);
                } catch (IOException e) {
                    Log.e(DEBUG_TAG, "unable to write double to outStream", e);
                }

            }
        }
    }

    private class ReceiverThread implements Runnable{
        private final String DEBUG_TAG = "SenderThread";
        private final BluetoothSocket btSocket;
        private final DataInputStream inStream;
        //private final DataOutputStream outStream;
        //private byte[] streamBuffer;

        public ReceiverThread(BluetoothSocket socket) {
            btSocket = socket;
            DataInputStream tmpIn = null;
            DataOutputStream tmpOut = null;
            timeStamp = System.currentTimeMillis();

            Log.d(DEBUG_TAG, "Attempting to create input and output streams");
            try {
                tmpIn = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "Error occurred when creating input stream",e);
            }
            //try {
            //    tmpOut = new DataOutputStream(socket.getOutputStream());
            //} catch (IOException e) {
            //    Log.e(DEBUG_TAG, "Error occurred when creating output stream",e);
            //}

            inStream = tmpIn;
            //outStream = tmpOut;
        }

        public void run() {
            //streamBuffer = new byte[1024];

            Log.d(DEBUG_TAG, "Connection initiated. Waiting for data.");
            Double receivedDouble = -1.0;

            while (true) {
                try {
                    receivedDouble = inStream.readDouble();
                } catch (IOException e){
                    Log.e(DEBUG_TAG, "Error while reading in the test double");
                }

                //pass the received message up to the UI handler
                Message received = mHandler.obtainMessage(
                        MessageConstants.MESSAGE_READ, receivedDouble
                );
                received.sendToTarget();

            }
        }
    }
}
