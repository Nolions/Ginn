package tw.nolions.coffeebeanslife.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import androidx.annotation.Nullable;
import tw.nolions.coffeebeanslife.MainActivity;
import tw.nolions.coffeebeanslife.databinding.FragmentMainBinding;

import com.github.mikephil.charting.charts.LineChart;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import tw.nolions.coffeebeanslife.R;
import tw.nolions.coffeebeanslife.service.BluetoothAcceptService;
import tw.nolions.coffeebeanslife.service.BluetoothSingleton;
import tw.nolions.coffeebeanslife.viewmodel.MainViewModel;
import tw.nolions.coffeebeanslife.widget.BluetoothDeviceAdapter;
import tw.nolions.coffeebeanslife.widget.MPChart;

public class MainFragment extends Fragment implements Toolbar.OnCreateContextMenuListener{
    private String TAG;
    private LineChart mLineChart;
    private Toolbar mToobBar;


    private MainViewModel mMainViewModel;
    private FragmentMainBinding mBinding;

//    private BluetoothAcceptService mBluetoothAcceptService;
    private MPChart mChart;


    private BluetoothAdapter mBluetoothAdapter= null;
    private BluetoothDevice mBluetootgDevice = null;
    private BluetoothSocket mBluetoothSocket = null;

    private OutputStream mOutputStream;
    private InputStream mInputStream;

    private ArrayList<BluetoothDevice> mDevices;

    private ListView mDeviceListView;
    private BluetoothDeviceAdapter mDeviceListAdapter;
    private Set<BluetoothDevice> mPairedDevices;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String value = (String) msg.obj;
            Toast.makeText(getContext(), value, Toast.LENGTH_LONG).show();


            if (value.matches("[-+]?[0-9]*\\.?[0-9]+")) {

            }


            mChart.addEntry(0, 1);
        }
    };

    @NonNull
    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BluetoothSingleton.getInstance().getAdapter();

        mDeviceListAdapter = new BluetoothDeviceAdapter(getContext());

        TAG = getResources().getString(R.string.app_name);

        if (BluetoothSingleton.getInstance().getAdapter() == null) {
            Toast.makeText(getContext(), getResources().getString(R.string.noSupportBluetooth), Toast.LENGTH_LONG).show();
        }

        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);


        mDevices = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);

        View v = mBinding.getRoot();


        mLineChart = (LineChart) v.findViewById(R.id.lineChart);
        mToobBar = (Toolbar) v.findViewById(R.id.toolbar);
        ((MainActivity) getActivity()).setSupportActionBar(mToobBar);
        setHasOptionsMenu(true);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!BluetoothSingleton.getInstance().getAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BluetoothAcceptService.REQUEST_ENABLE_BT);
        }

        mBluetootgDevice = BluetoothSingleton.getInstance().getDevic();
        mBluetoothSocket = BluetoothSingleton.getInstance().getSocket();

        getPairedDevices();
        registerBroadcastReceiver();

//        if (this.checkBluetoothConn()) {
//            mToobBar.setTitle(getContext().getString(R.string.app_name) +  " " + mBluetootgDevice.getName() + " 連線中...");
//            mChart.description("裝置連線，等待資料中...");
//
//            try {
//                mInputStream = BluetoothSingleton.getInstance().getSocket().getInputStream();
//                this.read();
//            } catch (IOException IOE) {
//                Log.e(TAG, "Error : " + IOE.getMessage());
//            }
//
//
//        } else {
//            mChart.description("沒有連線裝置...");
//        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mMainViewModel = new MainViewModel(getActivity());
        mBinding.setMainViewModel(mMainViewModel);

        String[] names = new String[]{
                "豆溫"
        };

        String description = "No chart data available. Use the menu to add entries and data sets!";
        mChart = new MPChart(mLineChart, description, names);
        mChart.init();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        Log.e(TAG, "onClick menu item");
        switch (menuItem.getItemId()) {
            case R.id.action_conn:
                View view = getLayoutInflater().inflate(R.layout.fragment_device_list, null);
                mDeviceListView = (ListView) view.findViewById(R.id.device_ListView);
                mDeviceListView.setAdapter(mDeviceListAdapter);

                mDeviceListView.setOnItemClickListener(listener);


                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setIcon(R.drawable.ic_bluetooth_black_24dp);
                builder.setTitle(R.string.selectBluetoothDevice);
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setNeutralButton(R.string.search, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getPairedDevices();
                    }
                });
                builder.setView(view);
                builder.show();
                break;
            case R.id.action_record:
                Log.e(TAG, "onClick Record menu item");
                break;
            case R.id.action_exit:
                Log.e(TAG, "onClick Record exit item");


                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setTitle("Exit Application?");
                alertDialogBuilder
                        .setMessage("Click yes to exit!")
                        .setCancelable(false)
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        android.os.Process.killProcess(android.os.Process.myPid());
                                        System.exit(1);
                                    }
                                })

                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                break;
        }

        return false;
    }

    private boolean checkBluetoothConn() {
        if (mBluetootgDevice == null && mBluetoothSocket == null) {
            Log.d(TAG, "no device connection");
            return false;
        }

        return true;
    }

    private void switchFragment(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(fragment.getClass().getName());
        transaction.commit();
    }

    private void updateTemp(HashMap data) {
        mMainViewModel.updateTemp(data);

        String bean = DecimalPoint(Double.valueOf((String) data.get("bean")));
        mChart.addEntry(0, Float.parseFloat(bean));
    }

    private void read() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Log.d(TAG, "readData");
                        try {
                            byte[] buffer = new byte[128];
                            int count = mInputStream.read(buffer);
                            Message msg = new Message();
                            msg.obj = new String(buffer, 0, count, "utf-8");
                            Log.d(TAG, (String) msg.obj);
                            handler.sendMessage(msg);
                        } catch (IOException e) {
                            Log.d(TAG, "Error: " + e.getMessage());
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error: " + e.getMessage());
                }
            }
        });

        t.start();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            Toast.makeText(getContext(), (String) msg.obj, Toast.LENGTH_LONG).show();
            try {
                String data = (String) msg.obj;
                Toast.makeText(getContext(), data, Toast.LENGTH_LONG).show();
                JSONObject jsonObject = new JSONObject(data);


                HashMap<String, Object>  map = tools.Convert.toMap(jsonObject);
                updateTemp(map);
            } catch (JSONException JSONE) {
                Log.e(TAG, "error :  " + JSONE.getMessage());
            }

        }
    };


    public static String DecimalPoint(Double data) {
        DecimalFormat df=new DecimalFormat("#.##");
        return df.format(data);
    }

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

    private void registerBroadcastReceiver() {
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);
        getContext().registerReceiver(mReceiver, intent);

    }

    private void  getPairedDevices() {
        mPairedDevices = BluetoothSingleton.getInstance().getAdapter().getBondedDevices();
        Log.e(TAG, ""+mPairedDevices.size());
        ArrayList<BluetoothDevice> list = new ArrayList<>();
        for(BluetoothDevice device : mPairedDevices) {
            list.add(device);
        }

        mDeviceListAdapter.setData(BluetoothDeviceAdapter.PAIRED_ITEM_TYPE, list);
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

                        try {
                            bluetoothSocket.connect();
                            msg.obj = "device " + device.getName() + " Connection success";
                            handler.sendMessage(msg);
                        } catch (IOException ioe) {
                            try {
                                bluetoothSocket =(BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);
                                bluetoothSocket.connect();
                                msg.obj = "device " + device.getName() + " Connection success";
                        handler.sendMessage(msg);
                                Log.e("","Connected");
                            } catch (Exception e) {
                                Log.e(TAG, "error : " + e.getMessage());
                                msg.obj = "device " + device.getName() + " Connection fail";
                                handler.sendMessage(msg);
                            }
                        }
                    } catch (IOException ioe) {
                        Log.e(TAG, "error : " + ioe.getMessage());
                        msg.obj = "device " + device.getName() + " Connection fail";
                        handler.sendMessage(msg);
                    }
                }
            });

            t.start();
        }
    };
}

