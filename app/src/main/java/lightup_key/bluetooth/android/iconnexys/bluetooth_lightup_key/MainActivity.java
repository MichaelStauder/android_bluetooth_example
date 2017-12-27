package lightup_key.bluetooth.android.iconnexys.bluetooth_lightup_key;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_ENABLE_BT = 1;

    static final List<String> available_bluetooth_devices = new ArrayList<String>();
    static BluetoothSocket mmSocket;
    BluetoothDevice mmDevice = null;
    Set<BluetoothDevice> pairedDevices;

    public void Button1_OnClick(View v){
        showText("Turning led 1 on");
        Led_1_On();
    }

    public void Button2_OnClick(View v){
        showText("Turning led 2 on");
        Led_2_On();
    }

    public void Button3_OnClick(View v){
        showText("Switching all lights off");
        Led_all_off();
    }

    private void showText(String text) {
        boolean toast = false;
        if (toast) {
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        } else {
            Snackbar.make(findViewById(R.id.list), text, Snackbar.LENGTH_LONG).show();
        }
    }

    public void Button4_OnClick(View v){
        openLocationScreen();
    }

    public void sendBtMsg(String msg2send){

        final UUID MY_SSP_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

        if ( mmDevice == null ) {
           return;
        }

        try {

            mmSocket = mmDevice.createRfcommSocketToServiceRecord(MY_SSP_UUID);
            if (!mmSocket.isConnected()){
                mmSocket.connect();
            }

            String msg = msg2send;
            //msg += "\n";
            OutputStream mmOutputStream = mmSocket.getOutputStream();
            mmOutputStream.write(msg.getBytes());

            mmSocket.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    // candidate: command factory?
    public void Led_all_off() {
        sendBtMsg("0");
    }

    public void Led_1_On() {
        sendBtMsg("1");
    }

    public void Led_2_On() {
        sendBtMsg("2");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view, menu);
        return true;
    }

    public void clickMenuLocation(MenuItem menuItem)  {
        openLocationScreen();
    }

    public void clickMenuLights(MenuItem menuItem)  {
        openLightsScreen();
    }

    public void openLocationScreen()  {
        Intent intent = new Intent(this, ProximityAlertActivity.class);
        startActivity(intent);
    }

    public void openLightsScreen()  {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.light_panel);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Verify that the device supports Bluetooth
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            showText("Device doesn't support Bluetooth...");
            return;
        }
        // Verify that the adapter is enabled
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Verify that the paired device is already available.
        pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            List<String> values = new ArrayList<String>();
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                available_bluetooth_devices.add(deviceName);
            }
        }

        ListView listv = findViewById(R.id.list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_devices, available_bluetooth_devices);
        listv.setAdapter( adapter );

        listv.setTextFilterEnabled(true);
        listv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
            // change all item background to transparant
            for (int j = 0; j < parent.getChildCount(); j++)
                parent.getChildAt(j).setBackgroundColor(Color.TRANSPARENT);

            // change the background color of the selected element
            view.setBackgroundColor(Color.LTGRAY);

            int i = 0;
            for (BluetoothDevice device : pairedDevices){
                if ( position == i ) {
                    mmDevice = device;
                }
                ++i;
            }

            Led_all_off();

            view.setSelected(true);

            // When clicked, show a toast with the TextView text
            Toast.makeText(getApplicationContext(),
                    ((TextView) view).getText(), Toast.LENGTH_SHORT).show();


            }
        });

    }
}
