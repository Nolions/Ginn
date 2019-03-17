package tw.nolions.coffeebeanslife.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class BluetoothAcceptService extends Thread {
    private final String TAG = "CoffeeBeansLife";
    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothServerSocket mServerSocket;
    private BluetoothSocket mSocket;
    private InputStream mInputStream;
    private Handler mHandler;

    public final static int REQUEST_ENABLE_BT = 1;

    public BluetoothAcceptService(BluetoothAdapter adapter, Handler handler) {
        mBluetoothAdapter = adapter;

        mHandler = handler;
    }

    public void conn(String deviceName, UUID deviceUUID) {
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(deviceName, deviceUUID);
        } catch (IOException IOE) {
            Log.e(TAG, IOE.getMessage());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        mServerSocket = tmp;
    }

    public void run() {
        try {
            mSocket = mServerSocket.accept();
            Log.d(TAG, "BlueTooth Socket: " + mSocket);
            mInputStream = mSocket.getInputStream();
            this.receive();
        } catch (IOException ioe) {
            Log.e(TAG, "Error: " + ioe.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }

    private void receive() {
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                byte[] buffer = new byte[128];
                int count = mInputStream.read(buffer);
                Message msg = new Message();
                msg.obj = new String(buffer, 0, count, "utf-8");
                Log.d("Coffee Beans Life", (String) msg.obj);
                mHandler.sendMessage(msg);

            } catch (IOException ioe) {
                Log.e(TAG, "Error: " + ioe.getMessage());
                continue;
            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
                continue;
            }
        }
    }

    public void send(String data){

    }

    public void cancel() {
        try {
            mServerSocket.close();
        } catch (IOException ioe) {
            Log.e(TAG, "Error: " + ioe.getMessage());

        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }
}
