package lightup_key.bluetooth.android.iconnexys.bluetooth_lightup_key;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class BaseActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String KEY_PREFIX = "lightup_key.bluetooth.android.iconnexys.bluetooth_lightup_key.DEVICE";
    private static final String KEY_DEVICE = KEY_PREFIX +"_NAME";
    private static final String SHARED_PREFERENCES = "SharedPreferences";
    private BluetoothDevice activeBluetoothDevice = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view, menu);
        return true;
    }

    public void clickMenuLocation(MenuItem menuItem) {
        openLocationScreen();
    }

    public void clickMenuLights(MenuItem menuItem) {
        openLightsScreen();
    }

    public void openLocationScreen() {
        Intent intent = new Intent(this, ProximityAlertActivity.class);
        startActivity(intent);
    }

    public void openLightsScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void showText(String text) {
        boolean toast = false;
        if (toast) {
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        } else {
            View container = findViewById(android.R.id.content);
            if (container != null) {
                Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    protected Set<BluetoothDevice> getPairedBluetoothDevices() {
        // Verify that the device supports Bluetooth
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return Collections.EMPTY_SET;
        }
        // Verify that the adapter is enabled
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        // Verify that the paired device is already available.
        return mBluetoothAdapter.getBondedDevices();
    }

    public SharedPreferences getSharedPreferences()  {
        return this.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    public BluetoothDevice getActiveBluetoothDevice() {
        return activeBluetoothDevice;
    }

    public void setActiveDevice(BluetoothDevice device) {
        activeBluetoothDevice = device;
        final SharedPreferences sharedPreferences = getSharedPreferences();
        SharedPreferences.Editor prefs = sharedPreferences.edit();
        // Write the active to SharedPreferences.
        prefs.putString(KEY_DEVICE, device.getName());
        prefs.apply();
    }

    public String getActiveDeviceName()  {
        final SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.getString(KEY_DEVICE, null);
    }

    public BluetoothDevice getActiveDevice()  {
        if (activeBluetoothDevice != null)  {
            return activeBluetoothDevice;
        } else {
            String deviceName = getActiveDeviceName();
            for (BluetoothDevice pairedDevice : getPairedBluetoothDevices()) {
                if (deviceName.equalsIgnoreCase(pairedDevice.getName())) {
                    activeBluetoothDevice = pairedDevice;
                    return activeBluetoothDevice;
                }
            }
        }
        return null;
    }

    public void sendBluetoothMessage(final String bluetoothMessage){
        BluetoothDevice device = getActiveDevice();
        if ( device == null ) {
            return;
        }

        try {
            final UUID MY_SSP_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
            BluetoothSocket bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_SSP_UUID);
            if (!bluetoothSocket.isConnected()){
                bluetoothSocket.connect();
            }
            OutputStream bluetoothStream = bluetoothSocket.getOutputStream();
            bluetoothStream.write(bluetoothMessage.getBytes());
            bluetoothSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
