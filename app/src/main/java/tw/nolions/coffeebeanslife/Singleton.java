package tw.nolions.coffeebeanslife;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class Singleton {
    private static Singleton INSTANCE;

    private BluetoothAdapter mBLEAdapter = null;
    private BluetoothSocket mBLESocket = null;
    private BluetoothDevice mBLEDevice = null;

    private Singleton() {

    }

    public static Singleton getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Singleton();

        }

        return INSTANCE;
    }

    public void setBLEAdapter(BluetoothAdapter adapter) {
        this.mBLEAdapter = adapter;
    }

    public BluetoothAdapter getBLEAdapter() {
        return this.mBLEAdapter;
    }

    public void setBLESocket(BluetoothSocket socket) {
        this.mBLESocket = socket;
    }

    public BluetoothSocket getBLESocket() {
        return this.mBLESocket;
    }

    public void setBLEDevice(BluetoothDevice device) {
        this.mBLEDevice = device;
    }

    public BluetoothDevice getBLEDevice() {
        return this.mBLEDevice;
    }
}
