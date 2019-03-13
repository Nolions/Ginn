package tw.nolions.coffeebeanslife.service;

import android.bluetooth.BluetoothAdapter;

public class BluetoothSingleton {
    private static BluetoothSingleton mInstance;
    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothSingleton() {

    }

    public static BluetoothSingleton getInstance(){
        if (mInstance == null) {
            mInstance = new BluetoothSingleton();
        }

        return mInstance;
    }

    public void setBluetoothAdapter(BluetoothAdapter adapter){
        mBluetoothAdapter = adapter;
    }

    public BluetoothAdapter getBluetoothAdapter(){
        return mBluetoothAdapter;
    }
}
