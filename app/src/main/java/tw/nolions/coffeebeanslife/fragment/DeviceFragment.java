package tw.nolions.coffeebeanslife.fragment;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import tw.nolions.coffeebeanslife.MainActivity;
import tw.nolions.coffeebeanslife.R;
import tw.nolions.coffeebeanslife.service.BluetoothSingleton;
import tw.nolions.coffeebeanslife.widget.BluetoothDeviceAdapter;

public class DeviceFragment extends Fragment{

    private Toolbar mToolBar;
    private ListView mDeviceListView;

    private Boolean mScanning = true;
    private String TAG;
    private BluetoothDeviceAdapter mDeviceListAdapter;
    private Set<BluetoothDevice> mPairedDevices;

    private static final long SCAN_PERIOD = 10000;

    private Handler mHandler;

    private ArrayList<BluetoothDevice> mScanDevices;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast.makeText(getContext(), (String) msg.obj, Toast.LENGTH_LONG).show();
        }
    };


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND))  //收到bluetooth狀態改變
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    Log.d("TEST", device.getAddress());

//                    mScanDevices.add(device);
                    mDeviceListAdapter.addItem(BluetoothDeviceAdapter.NoPAIRED_ITEM_TYPE, device);
                    mDeviceListAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = getResources().getString(R.string.app_name);
        mHandler = new Handler();
        mScanDevices = new ArrayList<>();

        BluetoothSingleton.getInstance().getAdapter();

        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_device_list, container, false);

//        mToolBar = (Toolbar) v.findViewById(R.id.device_list_toolbar);
        mDeviceListView = (ListView) v.findViewById(R.id.device_ListView);

        setToolBar();
        setListView();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        registerBroadcastReceiver();
        scanDevice(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu");
        menu.clear();
        inflater.inflate(R.menu.menu_device_list, menu);

        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "toolbar menu click...");
        switch(item.getItemId()) {
            case android.R.id.home:
                Log.d(TAG, "Back pressure fragment");
                this.scanDevice(false);
                getFragmentManager().popBackStack();
                return true;
            case R.id.menu_scan:
                Log.d(TAG, "scan");
                scanDevice(true);
                break;
            case R.id.menu_stop:
                Log.d(TAG, "stop");
                scanDevice(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void registerBroadcastReceiver() {
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);
        getContext().registerReceiver(mReceiver, intent);

    }

    private void setToolBar() {
        ((MainActivity) getActivity()).setSupportActionBar(mToolBar);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);

        setHasOptionsMenu(true);
    }

    private void setListView() {
        mDeviceListAdapter = new BluetoothDeviceAdapter(getContext());
        getPairedDevices();
        mDeviceListView.setAdapter(mDeviceListAdapter);
        mDeviceListView.setOnItemClickListener(listener);
    }

    private ListView.OnItemClickListener listener = new ListView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
            Log.d(TAG, "item :" + position);
            final int p = position;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = Message.obtain();
                    UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                    BluetoothDevice device = mDeviceListAdapter.getDevice(p);

                    try {
                        BluetoothSocket bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                        bluetoothSocket.connect();
                        BluetoothSingleton.getInstance().setSocket(bluetoothSocket);
                        BluetoothSingleton.getInstance().setDevice(device);
                        msg.obj = "device " + device.getName() + " Connection success";
                        handler.sendMessage(msg);
                    } catch (IOException IOE) {
                        Log.e(TAG, "error : " + IOE.getMessage());
                        msg.obj = "device " + device.getName() + " Connection fail";
                        handler.sendMessage(msg);
                    }
                }
            });

            t.start();
        }
    };


    private void  getPairedDevices() {
        mPairedDevices = BluetoothSingleton.getInstance().getAdapter().getBondedDevices();
        Log.e(TAG, ""+mPairedDevices.size());
        ArrayList<BluetoothDevice> list = new ArrayList<>();
        for(BluetoothDevice device : mPairedDevices) {
            list.add(device);
        }

        mDeviceListAdapter.setData(BluetoothDeviceAdapter.PAIRED_ITEM_TYPE, list);
    }

    private void scanDevice(final boolean enable) {

        if (enable) {
            getPairedDevices();
//            mDeviceListAdapter.clearData(BluetoothDeviceAdapter.PAIRED_ITEM_TYPE);
//            mDeviceListAdapter.clearData(BluetoothDeviceAdapter.NoPAIRED_ITEM_TYPE);

//            BluetoothSingleton.getInstance().getAdapter().startDiscovery();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;

                    getActivity().invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);
            mScanning = true;
        } else {
            mScanning = false;
        }
        getActivity().invalidateOptionsMenu();

    }
}
