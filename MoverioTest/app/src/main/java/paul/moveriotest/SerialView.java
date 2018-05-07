package paul.moveriotest;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.usb.UsbManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.Map;

public class SerialView extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private Spinner dropdown;
    private static String[] items;
    private UsbManager manager;
    private Map<String, UsbDevice> devices;
    private UsbDevice mDevice;
    private UsbDeviceConnection connection;
    private UsbEndpoint endpoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_view);

        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        devices = manager.getDeviceList();

        dropdown = (Spinner)findViewById(R.id.spinner1);
        items = devices.keySet().toArray(new String[devices.size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id){
        String selection = items[position]; //hopefully this works
        mDevice = devices.get(selection);
        connection = manager.openDevice(mDevice);
        endpoint = mDevice.getInterface(0).getEndpoint(0);

        connection.claimInterface(mDevice.getInterface(0), true);
        String dataS = "Hello World\n";
        byte[] data = dataS.getBytes();
        connection.bulkTransfer(endpoint, data, data.length, 5000);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
