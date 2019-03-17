package tw.nolions.coffeebeanslife.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BluetoothSingleton {
    private static BluetoothSingleton mInstance;
    private BluetoothAdapter mAdapter = null;
    private BluetoothSocket mSocket = null;
    private BluetoothDevice mDevice = null;

    private BluetoothSingleton() {

    }

    public static BluetoothSingleton getInstance(){
        if (mInstance == null) {
            mInstance = new BluetoothSingleton();
        }

        return mInstance;
    }

    public void setAdapter(BluetoothAdapter adapter){
        this.mAdapter = adapter;
    }

    public BluetoothAdapter getAdapter(){
        return this.mAdapter;
    }

    public void setSocket(BluetoothSocket bluetoothSocket) {
        this.mSocket = bluetoothSocket;
    }

    public BluetoothSocket getSocket() {
        return this.mSocket;
    }

    public void setDevice(BluetoothDevice device) {
        this.mDevice = device;
    }

    public BluetoothDevice getDevic(){
        return this.mDevice;
    }
}
