package paul.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    TextView mainText;
    Button clientButton;
    Button serverButton;
    Button exitButton;
    ConnectThread clientThread;
    AcceptThread serverThread;
    ConnectedThread socketThread;
    BluetoothDevice btDevice;

    Handler mHandler;
    private final BlockingQueue<Runnable> mDecodeWorkQueue;
    private final ThreadPoolExecutor mDecodeThreadPool;

    BluetoothAdapter mBluetoothAdapter;

    private interface MessageConstants {
        int MESSAGE_READ = 0;
        int MESSAGE_WRITE = 1;
        int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }

    public MainActivity(){
        mDecodeWorkQueue = new LinkedBlockingQueue<Runnable>();
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage){
                String msg = new String((byte []) inputMessage.obj);
                Log.d("MessageHandler","Received Message: " + msg);
                mainText.setText("");
                mainText.setText(msg);

            }
        };
        mDecodeThreadPool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),Runtime.getRuntime().availableProcessors(), 30,TimeUnit.SECONDS, mDecodeWorkQueue);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register for broadcasts when a device is discovered.
        //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //registerReceiver(mReceiver, filter);

        mainText = (TextView) findViewById(R.id.textview_main);
        clientButton = (Button) findViewById(R.id.button_client);
        serverButton = (Button) findViewById(R.id.button_server);
        exitButton = (Button) findViewById(R.id.button_exit);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null ) {
            // Device does not support bluetooth
            mainText.setText("Error: could not get bluetooth adapter");
        }

        if(!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0); //TODO: use request code (the argument set to 0) correctly
        }
        listPairedDevices();

        clientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Log.d("ClientClickListener","Attempting to connect to " + btDevice.getName() + " " + btDevice.getAddress());
                mainText.setText("Attempting to connect to " + btDevice.getName() + " " + btDevice.getAddress() + " ... ");
                clientThread = new ConnectThread(btDevice);
                mDecodeThreadPool.execute(clientThread);
                //bluetoothThread = new ConnectThread(btDevice);
                //bluetoothThread.run();
            }
        });
        serverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){

                Log.d("OnClickListener", "Attempting to Start Server Thread");
                serverThread = new AcceptThread();
                mDecodeThreadPool.execute(serverThread);
                //bluetoothThread = new AcceptThread();
                //bluetoothThread.run();
            }
        });
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                finish();
                System.exit(0);
            }
        });
    }

    private void listPairedDevices(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                mainText.setText(mainText.getText() + "\n" + deviceName + "\t" + deviceHardwareAddress);

                //go ahead and set the only device (0) to the main device
                btDevice = device;
            }
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
//    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                // Discovery has found a device. Get the BluetoothDevice
//                // object and its info from the Intent.
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                String deviceName = device.getName();
//                String deviceHardwareAddress = device.getAddress(); // MAC address
//                mainText.setText(mainText.getText() + "\n" + deviceName + "\t" + deviceHardwareAddress);
//            }
//        }
//    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        //unregisterReceiver(mReceiver);
    }

    private class AcceptThread implements Runnable {
        String NAME = "AcceptThreadName";
        UUID MY_UUID = UUID.fromString("55ba6a24-f236-11e6-bc64-92361f002671");
        String MY_TAG = "AcceptThread";


        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {

            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

            Log.d(MY_TAG, "AcceptThread Started");
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(MY_TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            Log.d(MY_TAG, "Waiting to accept socket");
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(MY_TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    Log.d(MY_TAG, "A connection was accepted, moving to ConnectThread");
                    socketThread = new ConnectedThread(socket);
                    mDecodeThreadPool.execute(socketThread);
                    //socketThread = new ConnectedThread(socket);
                    //socketThread.run();
                    //manageMyConnectedSocket(socket);

                    //as soon as we get a connection, we can stop listening (for now, in this example)
                    cancel();
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            mDecodeThreadPool.remove(socketThread);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(MY_TAG, "Could not close the connect socket", e);
            }
        }
    }

    private class ConnectThread implements Runnable {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private final UUID MY_UUID = UUID.fromString("55ba6a24-f236-11e6-bc64-92361f002671");
        private final String TAG = "AcceptThread";

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
            Log.d(TAG, "Created RfCommSocket with info: " + mmSocket.toString());
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                Log.d(TAG, "Unable to connect, closing socket");
                //mainText.setText("Unable to connect to " + mmSocket.getRemoteDevice().getName());
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            Log.d(TAG, "Socket created, moving to ConnectedThread");
            socketThread = new ConnectedThread(mmSocket);
            socketThread.run();

            //manageMyConnectedSocket(mmSocket);
            cancel();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    private class ConnectedThread implements Runnable {
        private final String TAG = "ConnectThread_Debug_Tag";
        private final BluetoothSocket mmSocket;
        private final DataInputStream mmInStream;
        private final DataOutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream
        private final String MY_TAG = "ConnectedThread";

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            DataInputStream tmpIn = null;
            DataOutputStream tmpOut = null;



            // Get the input and output streams; using temp objects because
            // member streams are final.
            Log.d(TAG, "Attempting to create input and output streams");
            try {
                tmpIn = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()
            String testMessage = "Hello Bluetooth World! Message id: ";
            int i = 0;

            Log.d(MY_TAG, "New Connection!");

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    //mainText.setText("Attempting to send/recieve");
                    Log.d(TAG, "Attempting to send message...");
                    i++;
                    String msg = testMessage + Integer.toString(i);
                    write(msg);

                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);



                    // Send the obtained bytes to the UI activity.

                    //Message readMsg = mHandler.obtainMessage();
                    //readMsg.obj = mmBuffer;
                    //readMsg.sendToTarget();

                    Message readMsg = mHandler.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    readMsg.sendToTarget();


                    //Log.d(TAG, "Attempting to write buffer: " + mmBuffer. + " to screen.");
                    //mainText.setText(mmBuffer.toString());

                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    //mainText.setText("Input stream disconnected");
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(String bytes) {
            try {
                mmOutStream.writeBytes(bytes);

                // We do NOT want to send the OUTPUT message with the UI activity
                // Share the sent message with the UI activity.
                //Message writtenMsg = mHandler.obtainMessage(
                //        MessageConstants.MESSAGE_WRITE, -1, -1, bytes);
                //writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                //For right now I don't care
                //Message writeErrorMsg =
                //        mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                //Bundle bundle = new Bundle();
                //bundle.putString("toast",
                //        "Couldn't send data to the other device");
                //writeErrorMsg.setData(bundle);
                //mHandler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

}
