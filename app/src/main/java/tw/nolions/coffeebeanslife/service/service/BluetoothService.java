package tw.nolions.coffeebeanslife.service.service;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import tw.nolions.coffeebeanslife.Const;

public class BluetoothService extends Service {
    private Handler mHandler = new Handler();

    private BluetoothDevice mBluetoothDevice;
    private BluetoothAdapter mBluetoothAdapter;


    private final IBinder mIBinder = new LocalBinder();
    private UUID mUUID;

    public ConnectThread mConnectThread;
    public ConnectedThread mConnectedThread;

    private int mState;
    private String mTag;
    private String mDeviceName;
    private String mDeviceAddress;

    // CONST
    final private UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            setTag((String) intent.getExtras().get("TAG"));
            setUUID((UUID) intent.getExtras().get("UUID"));
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mHandler != null) {
            mHandler = null;
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mIBinder;
    }


    public boolean isSupport() {
        return mBluetoothAdapter == null ? false : true;
    }

    public boolean isEnable() {
        return mBluetoothAdapter.isEnabled();
    }

    public ArrayList<BluetoothDevice> pairedDevices() {
        ArrayList<BluetoothDevice> list = new ArrayList<>();
        for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices()) {
            list.add(device);
        }

        return list;
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public synchronized void connect(BluetoothDevice device) {
        Log.d(getTag(), "Connecting to: " + getDevcieName() + " - " + getDeviceAddress());

        sendToTarget(Const.BLUETOOTH_SERVICE_WHAT_CONNECTION, "Connecting to: " + getDevcieName() + " - " + getDeviceAddress());
        setState(Const.BLUETOOTH_SERVICE_STATE_CONNECTING);
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    public void setBluetoothDevice(BluetoothDevice device) {
        if (mBluetoothAdapter != null) {
            mBluetoothDevice = device;
            setDeviceName(getBluetoothDevice().getName());
            setDeviceAddress(getBluetoothDevice().getAddress());

            if (getDeviceAddress() != null && getDeviceAddress().length() > 0) {
                connect(getBluetoothDevice());
            } else {
                stopSelf();
            }
        }
    }

    public BluetoothDevice getBluetoothDevice() {
        return mBluetoothDevice;
    }

    private void setDeviceName(String name) {
        mDeviceName = name;
    }

    public String getDevcieName() {
        return mDeviceName;
    }

    private void setDeviceAddress(String device) {
        mDeviceAddress = device;
    }

    public String getDeviceAddress() {
        return mDeviceAddress;
    }


    private void setTag(String tag) {
        mTag = tag;
    }

    private String getTag() {
        if (mTag.equals("")) {
            return "BluetoothService";
        }
        return mTag;
    }

    private void setUUID(UUID uuid) {
        mUUID = uuid;
    }

    private UUID getUUID() {
        if (mUUID == null) {
            return DEFAULT_UUID;
        }
        return mUUID;
    }

    private synchronized void setState(int state) {
        Log.d(getTag(), "setState() " + this.mState + " -> " + state);
        this.mState = state;
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(getTag(), "connected to: " + device.getName());

        cancelConnectThread();
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        sendToTarget(Const.BLUETOOTH_SERVICE_STATE_CONNECTED, "Connected");
        setState(Const.BLUETOOTH_SERVICE_STATE_CONNECTED);
    }

    public synchronized void stop() {
        Log.d(getTag(), "Connection stop");

        cancelConnectThread();
        cancelConnectedThread();
        sendToTarget(Const.BLUETOOTH_SERVICE_WHAT_STOP, "Connection is stop");
        setState(Const.BLUETOOTH_SERVICE_STATE_NONE);
    }

    private void connectionFailed() {
        Log.e(getTag(), "Connection Failed");

        sendToTarget(Const.BLUETOOTH_SERVICE_WHAT_ERROR, "Unable to connect");
        setState(Const.BLUETOOTH_SERVICE_STATE_ERROR);
        cancelConnectThread();
    }

    private void connectionLost() {
        Log.e(getTag(), "Connection Lost");

        sendToTarget(Const.BLUETOOTH_SERVICE_WHAT_ERROR, "Connection was lost");
        setState(Const.BLUETOOTH_SERVICE_STATE_ERROR);
        cancelConnectedThread();
    }

    private void cancelConnectThread() {
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
    }

    private void cancelConnectedThread() {
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    private void sendToTarget(int what, Object obj) {
        Message msg = mHandler.obtainMessage(what);
        msg.obj = obj;
        msg.sendToTarget();
    }


    public Set<BluetoothDevice> getBondedDevices() {
        return mBluetoothAdapter.getBondedDevices();
    }

    public void write(byte[] data) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != Const.BLUETOOTH_SERVICE_STATE_CONNECTED) {
                Log.e(getTag(), "Trying to send but not connected");
                return;
            }
            r = mConnectedThread;
        }

        r.write(data);
    }

    public class LocalBinder extends Binder {
        public BluetoothService getInstance() {
            return BluetoothService.this;
        }
    }

    public class ConnectThread extends Thread {
        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;

        private ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mDevice = device;

            try {
                if (getUUID() == null) {
                    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                    tmp = device.createRfcommSocketToServiceRecord(uuid);
                } else {
                    tmp = device.createRfcommSocketToServiceRecord(getUUID());
                }


            } catch (IOException e) {
                Log.e(getTag(), "Create Bluetooth Socket failed ", e);
            }

            mSocket = tmp;
        }

        public void run() {
            try {
                mSocket.connect();
            } catch (IOException connectException) {
                Log.e(getTag(), "Unable to connect ", connectException);

                try {
                    mSocket.close();
                } catch (IOException closeException) {
                    Log.e(getTag(), "Unable to close() socket during connection failure ", closeException);
                    connectionFailed();
                }

                return;
            }

            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // Do work to manage the connection (in a separate thread)
            connected(mSocket, mDevice);
        }

        private void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(getTag(), "Close() socket failed", e);
            }
        }
    }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mInStream;
        private final OutputStream mOutStream;

        private ConnectedThread(BluetoothSocket socket) {
            mSocket = socket;
            InputStream in = null;
            OutputStream out = null;

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(getTag(), "Temp sockets not created", e);
            }

            mInStream = in;
            mOutStream = out;
        }

        public void run() {
            Log.d(getTag(), "Begin connectedThread");

            while (true) {
                try {
                    int bytes;
                    byte[] buffer = new byte[1024];
                    String end = "\n";
                    StringBuilder curMsg = new StringBuilder();

                    while (-1 != (bytes = mInStream.read(buffer))) {
                        curMsg.append(new String(buffer, 0, bytes, Charset.forName("ISO-8859-1")));
                        int endIdx = curMsg.indexOf(end);
                        if (endIdx != -1) {
                            String fullMessage = curMsg.substring(0, endIdx + end.length());
                            curMsg.delete(0, endIdx + end.length());

                            Log.d(getTag(), "Read Message:" + fullMessage);
                            mHandler.obtainMessage(Const.BLUETOOTH_SERVICE_WHAT_READ, bytes, -1, fullMessage).sendToTarget();
                        }
                    }
                } catch (IOException ioe) {
                    Log.e(getTag(), "Connection Lost", ioe);
                    connectionLost();
                    break;
                }
            }
        }

        private void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(getTag(), "close() of connect socket failed", e);
            }
        }

        private void write(byte[] bytes) {
            try {
                mOutStream.write(bytes);
                mHandler.obtainMessage(Const.BLUETOOTH_SERVICE_WHAT_WRITE, -1, -1, bytes).sendToTarget();
            } catch (IOException e) {
                Log.e(getTag(), "Exception during write", e);
            }
        }
    }


}
