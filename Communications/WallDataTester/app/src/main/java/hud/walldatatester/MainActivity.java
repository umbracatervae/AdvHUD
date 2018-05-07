package hud.walldatatester;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final int BT_ENABLE_REQUEST_INIT = 0;

    TangoBluetooth btManager;
    BluetoothAdapter btAdapter;

    Button testListButton1;
    Button testListButton2;
    Button testListButton3;
    Button testListButton4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btManager = null;


        testListButton1 = (Button) findViewById(R.id.sendWalls1);
        testListButton2 = (Button) findViewById(R.id.sendWalls2);
        testListButton3 = (Button) findViewById(R.id.sendWalls3);
        testListButton4 = (Button) findViewById(R.id.sendWalls4);

        testListButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btManager != null){
                    if(!btManager.isConnected()){
                        btManager.connect();
                    }
                    if(btManager.isConnected()){
                        ArrayList<Wall2D> wallList = testList1();
                        ArrayList<Double> oriList = testOrientation1();
                        btManager.send(TangoBluetooth.makeFrame(oriList,wallList));
                    }
                }
            }
        });
        testListButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btManager != null){
                    if(!btManager.isConnected()){
                        btManager.connect();
                    }
                    if(btManager.isConnected()){
                        ArrayList<Wall2D> wallList = testList2();
                        ArrayList<Double> oriList = testOrientation1();
                        btManager.send(TangoBluetooth.makeFrame(oriList,wallList));
                    }
                }
            }
        });
        testListButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btManager != null){
                    if(!btManager.isConnected()){
                        btManager.connect();
                    }
                    if(btManager.isConnected()){
                        ArrayList<Wall2D> wallList = testList3();
                        ArrayList<Double> oriList = testOrientation2();
                        btManager.send(TangoBluetooth.makeFrame(oriList,wallList));
                    }
                }
            }
        });
        testListButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btManager != null){
                    if(!btManager.isConnected()){
                        btManager.connect();
                    }
                    if(btManager.isConnected()){
                        ArrayList<Wall2D> wallList = testList4();
                        ArrayList<Double> oriList = testOrientation2();
                        btManager.send(TangoBluetooth.makeFrame(oriList,wallList));
                    }
                }
            }
        });

        startBluetoothAdapter();
        communicate();

    }

    private void communicate() { //run this after btAdapter is enabled
        if(btAdapter.isEnabled()) {
            btManager = new TangoBluetooth(btAdapter);
            do {
                btManager.connect();
            } while (!btManager.isConnected());

            //Generated Canned Data for transmission test
            ArrayList<Wall2D> wallList = testList1();
            ArrayList<Double> oriList = testOrientation1();

            btManager.send(TangoBluetooth.makeFrame(oriList,wallList));
        }
        else {
            Log.e("MainActivity","Bluetooth adapter must be enabled to communicate.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == BT_ENABLE_REQUEST_INIT){
            if(resultCode == RESULT_OK){
                Log.d("Activity Result","Bluetooth Enabled on Initialization by User");
                communicate();
            }
            else {
                Log.e("ActivityResult","Bluetooth is Disabled.");
            }
        }
    }
    protected void startBluetoothAdapter(){
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter == null) {
            Log.e("StartBluetoothAdapter", "Unable to get BluetoothAdapter");
        }
        else if(!btAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, BT_ENABLE_REQUEST_INIT);
        }
    }
    protected ArrayList<Wall2D> testList1(){
        ArrayList<Wall2D> testList = new ArrayList<>();
        Point A = new Point(90,0,400);
        Point B = new Point(175,0,400);
        Point C = new Point(90,0,140);
        Point D = new Point(175,0,190);
        Point E = new Point(-100,0,140);
        Point F = new Point(270,0,190);
        Point G = new Point(-100,0,90);
        Point H = new Point(90,0,90);
        Point I = new Point(90,0,40);
        Point J = new Point(270,0,40);
        Wall2D w0 = new Wall2D(A);
        w0.addPoint(C);
        Wall2D w1 = new Wall2D(C);
        w1.addPoint(E);
        Wall2D w2 = new Wall2D(G);
        w2.addPoint(H);
        Wall2D w3 = new Wall2D(H);
        w3.addPoint(I);
        Wall2D w4 = new Wall2D(I);
        w4.addPoint(J);
        Wall2D w5 = new Wall2D(J);
        w5.addPoint(F);
        Wall2D w6 = new Wall2D(F);
        w6.addPoint(D);
        Wall2D w7 = new Wall2D(D);
        w7.addPoint(B);
        testList.add(w0);
        testList.add(w1);
        testList.add(w2);
        testList.add(w3);
        testList.add(w4);
        testList.add(w5);
        testList.add(w6);
        testList.add(w7);
        return testList;
    }
    protected ArrayList<Wall2D> testList2(){
        ArrayList<Wall2D> testList = new ArrayList<>();
        Point A = new Point(50,0,500);
        Point B = new Point(100,0,300);
        Point C = new Point(80,0,160);
        Point D = new Point(125,0,200);
        Point E = new Point(-60,0,120);
        Point F = new Point(280,0,200);
        Point G = new Point(-120,0,60);
        Point H = new Point(100,0,100);
        Point I = new Point(80,0,50);
        Point J = new Point(290,0,30);
        Wall2D w0 = new Wall2D(A);
        w0.addPoint(C);
        Wall2D w1 = new Wall2D(C);
        w1.addPoint(E);
        Wall2D w2 = new Wall2D(G);
        w2.addPoint(H);
        Wall2D w3 = new Wall2D(H);
        w3.addPoint(I);
        Wall2D w4 = new Wall2D(I);
        w4.addPoint(J);
        Wall2D w5 = new Wall2D(J);
        w5.addPoint(F);
        Wall2D w6 = new Wall2D(F);
        w6.addPoint(D);
        Wall2D w7 = new Wall2D(D);
        w7.addPoint(B);
        testList.add(w0);
        testList.add(w1);
        testList.add(w2);
        testList.add(w3);
        testList.add(w4);
        testList.add(w5);
        testList.add(w6);
        testList.add(w7);
        return testList;
    }
    protected ArrayList<Wall2D> testList3(){
        ArrayList<Wall2D> testList = new ArrayList<>();
        Point A = new Point(400,0,90);
        Point B = new Point(400,0,175);
        Point C = new Point(140,0,90);
        Point D = new Point(190,0,175);
        Point E = new Point(140,0,-100);
        Point F = new Point(190,0,270);
        Point G = new Point(90,0,-100);
        Point H = new Point(90,0,90);
        Point I = new Point(40,0,90);
        Point J = new Point(40,0,270);
        Wall2D w0 = new Wall2D(A);
        w0.addPoint(C);
        Wall2D w1 = new Wall2D(C);
        w1.addPoint(E);
        Wall2D w2 = new Wall2D(G);
        w2.addPoint(H);
        Wall2D w3 = new Wall2D(H);
        w3.addPoint(I);
        Wall2D w4 = new Wall2D(I);
        w4.addPoint(J);
        Wall2D w5 = new Wall2D(J);
        w5.addPoint(F);
        Wall2D w6 = new Wall2D(F);
        w6.addPoint(D);
        Wall2D w7 = new Wall2D(D);
        w7.addPoint(B);
        testList.add(w0);
        testList.add(w1);
        testList.add(w2);
        testList.add(w3);
        testList.add(w4);
        testList.add(w5);
        testList.add(w6);
        testList.add(w7);
        return testList;
    }
    protected ArrayList<Wall2D> testList4(){
        ArrayList<Wall2D> testList = new ArrayList<>();
        Point A = new Point(300,0,100);
        Point B = new Point(400,0,150);
        Point C = new Point(150,0,100);
        Point D = new Point(180,0,200);
        Point E = new Point(140,0,-120);
        Point F = new Point(10,0,250);
        Point G = new Point(100,0,-120);
        Point H = new Point(100,0,80);
        Point I = new Point(50,0,80);
        Point J = new Point(50,0,240);
        Wall2D w0 = new Wall2D(A);
        w0.addPoint(C);
        Wall2D w1 = new Wall2D(C);
        w1.addPoint(E);
        Wall2D w2 = new Wall2D(G);
        w2.addPoint(H);
        Wall2D w3 = new Wall2D(H);
        w3.addPoint(I);
        Wall2D w4 = new Wall2D(I);
        w4.addPoint(J);
        Wall2D w5 = new Wall2D(J);
        w5.addPoint(F);
        Wall2D w6 = new Wall2D(F);
        w6.addPoint(D);
        Wall2D w7 = new Wall2D(D);
        w7.addPoint(B);
        testList.add(w0);
        testList.add(w1);
        testList.add(w2);
        testList.add(w3);
        testList.add(w4);
        testList.add(w5);
        testList.add(w6);
        testList.add(w7);
        return testList;
    }
    protected ArrayList<Double> testOrientation1(){
        ArrayList<Double> testOrientation = new ArrayList<>();
        Double x = 1.0;
        Double y = -2.0;
        Double z = 0.0;

        Double o1 = 0.85;
        Double o2 = 0.62;
        Double o3 = 0.42;
        Double o4 = 0.56;

        testOrientation.add(x);
        testOrientation.add(y);
        testOrientation.add(z);
        testOrientation.add(o1);
        testOrientation.add(o2);
        testOrientation.add(o3);
        testOrientation.add(o4);

        return testOrientation;
    }
    protected ArrayList<Double> testOrientation2(){
        ArrayList<Double> testOrientation = new ArrayList<>();
        Double x = 2.0;
        Double y = -2.0;
        Double z = 3.0;

        Double o1 = 0.95;
        Double o2 = 0.72;
        Double o3 = 0.52;
        Double o4 = 0.66;

        testOrientation.add(x);
        testOrientation.add(y);
        testOrientation.add(z);
        testOrientation.add(o1);
        testOrientation.add(o2);
        testOrientation.add(o3);
        testOrientation.add(o4);

        return testOrientation;
    }
}
