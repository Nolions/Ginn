package tw.nolions.coffeebeanslife.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import tw.nolions.coffeebeanslife.MainActivity;
import tw.nolions.coffeebeanslife.R;

public class DeviceFragment extends Fragment {

//    UI
    private Toolbar mToolBar;
    private ListView mDeviceListView;

//    Data
    private String TAG;
    private ArrayList<String> mDevices;
    private ListAdapter mDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> mPairedDevices;

    private final int REQUEST_ENABLE_BT = 1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = getResources().getString(R.string.app_name);
        mDevices = new ArrayList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
//        View v = mBinding.getRoot();

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
        getPairedDevices();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_device_list, menu);
    }

    private void setToolBar() {
        ((MainActivity) getActivity()).setSupportActionBar(mToolBar);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);

        setHasOptionsMenu(true);

//        toolbar.inflateMenu(R.menu.base_toolbar_menu);
    }

    private void setListView() {
        mDeviceListAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mDevices);
        mDeviceListView.setAdapter(mDeviceListAdapter);

//        mDevices.add("lv2410");
//        ((BaseAdapter) mDeviceListAdapter) .notifyDataSetChanged();
    }

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
            mDevices.add(device.getName());
        }

        this.changeDeviceList();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "toolbar menu click...");
        switch(item.getItemId()) {
            case android.R.id.home:
                Log.d(TAG, "Back pressure fragment");
                getFragmentManager().popBackStack();
                return true;
            case R.id.bluetooth_search:
                Log.d(TAG, "on click toolbar's bluetooth seaach button...");
        }
        return super.onOptionsItemSelected(item);
    }
}
