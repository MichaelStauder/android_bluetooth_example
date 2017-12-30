package lightup_key.bluetooth.android.iconnexys.bluetooth_lightup_key;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends BaseActivity {

    static final List<String> available_bluetooth_devices = new ArrayList<>();
    Set<BluetoothDevice> pairedDevices;

    public void Button1_OnClick(View v){
        final String message = String.format(getString(R.string.lights_turn_on_led), "1");
        showText(message);
        Led_1_On();
    }

    public void Button2_OnClick(View v){
        final String message = String.format(getString(R.string.lights_turn_on_led), "2");
        showText(message);
        Led_2_On();
    }

    public void Button3_OnClick(View v){
        showText(getString(R.string.lights_turn_off_all_leds));
        Led_all_off();
    }

    public void Button4_OnClick(View v){
        openLocationScreen();
    }



    // candidate: command factory?
    public void Led_all_off() {
        sendBluetoothMessage("0");
    }

    public void Led_1_On() {
        sendBluetoothMessage("1");
    }

    public void Led_2_On() {
        sendBluetoothMessage("2");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.light_panel);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        pairedDevices = getPairedBluetoothDevices();
        if (pairedDevices.size() == 0)  {
            showText(getString(R.string.lights_device_no_bluetooth));
            return;
        } else {
            available_bluetooth_devices.clear();
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                available_bluetooth_devices.add(deviceName);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_devices, available_bluetooth_devices);
        ListView listv = findViewById(R.id.list);
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
                    setActiveDevice(device);
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
