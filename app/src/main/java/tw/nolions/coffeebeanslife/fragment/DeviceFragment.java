package tw.nolions.coffeebeanslife.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Set;

import tw.nolions.coffeebeanslife.MainActivity;
import tw.nolions.coffeebeanslife.R;
import tw.nolions.coffeebeanslife.model.DeviceModel;
import tw.nolions.coffeebeanslife.widget.BluetoothDeviceAdapter;

public class DeviceFragment extends Fragment{

    private Toolbar mToolBar;
    private ListView mDeviceListView;

    private Boolean mScanning = true;
    private String TAG;
    private BluetoothDeviceAdapter mDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> mPairedDevices;

    private final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;

    private Handler mHandler;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = getResources().getString(R.string.app_name);
        mHandler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_device_list, container, false);

        mToolBar = (Toolbar) v.findViewById(R.id.device_list_toolbar);
        mDeviceListView = (ListView) v.findViewById(R.id.device_ListView);

        setToolBar();
        setListView();
        setBluetooth();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
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
                getFragmentManager().popBackStack();
                return true;
            case R.id.menu_scan:
                Log.d(TAG, "scan");
                scanDevice(true);
//                invalidateOptionsMenu();
                break;
            case R.id.menu_stop:
                Log.d(TAG, "stop");
                scanDevice(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void setToolBar() {
        ((MainActivity) getActivity()).setSupportActionBar(mToolBar);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);

        setHasOptionsMenu(true);
    }

    private void setListView() {
        mDeviceListAdapter = new BluetoothDeviceAdapter(getContext());
        mDeviceListView.setAdapter(mDeviceListAdapter);
        mDeviceListView.setOnItemClickListener(listener);

    }

    private ListView.OnItemClickListener listener = new ListView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Log.d(TAG, "item :" + arg2);
        }
    };

    private void setBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(getContext(), getResources().getString(R.string.noSupportBluetooth), Toast.LENGTH_LONG).show();
        }

        // check Bluetooth enable
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void changeDeviceList() {
        ((BaseAdapter) mDeviceListAdapter) .notifyDataSetChanged();
    }

    private void  getPairedDevices() {
        mPairedDevices = mBluetoothAdapter.getBondedDevices();

        for(BluetoothDevice device : mPairedDevices) {
            mDeviceListAdapter.addItem(new DeviceModel(device.getName(), device.getAddress()));
        }

        this.changeDeviceList();
    }

    private void scanDevice(final boolean enable) {

        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    getPairedDevices();
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
