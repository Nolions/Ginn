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
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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
import tools.Convert;
import tools.info;
import tw.nolions.coffeebeanslife.MainActivity;
import tw.nolions.coffeebeanslife.Singleton;
import tw.nolions.coffeebeanslife.databinding.FragmentMainBinding;

import com.github.mikephil.charting.charts.LineChart;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import tw.nolions.coffeebeanslife.R;
import tw.nolions.coffeebeanslife.service.BluetoothAcceptService;
import tw.nolions.coffeebeanslife.viewModel.MainViewModel;
import tw.nolions.coffeebeanslife.widget.BluetoothDeviceAdapter;
import tw.nolions.coffeebeanslife.widget.MPChart;

public class MainFragment extends Fragment implements
        Toolbar.OnCreateContextMenuListener,
        NavigationView.OnNavigationItemSelectedListener {
    // UI Widget
    private View mView;
    private Toolbar mToolBar;
    private LineChart mLineChart;
    private ListView mDeviceListView;
    private AlertDialog alertDialog;

    // view model
    private MainViewModel mMainViewModel;
    private FragmentMainBinding mBinding;

    // bluetooth
//    private OutputStream mOutputStream;
    private InputStream mInputStream;

    // widget
    private MPChart mChart;
    private BluetoothDeviceAdapter mDeviceListAdapter;


    // data
    private Set<BluetoothDevice> mPairedDevices;

    // handler
    private Handler mConnHandler;
    private Handler mReadHandler;

    private DrawerLayout mDrawerLayout;

    @NonNull
    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
    }

    private void init() {
        mDeviceListAdapter = new BluetoothDeviceAdapter(getContext());

        initBluetooth();

        initHandler();
    }

    private void initBluetooth() {
        if (Singleton.getInstance().getBLEAdapter() == null) {
            Toast.makeText(getContext(), getResources().getString(R.string.noSupportBluetooth), Toast.LENGTH_LONG).show();
        }

        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                info.PermissionsRequestAccessLocationCode()
        );
    }

    private void initHandler() {
        // handler bluetooth device connection
        mConnHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String data = (String) msg.obj;
                Toast.makeText(getContext(), data, Toast.LENGTH_LONG).show();
                if (checkBluetoothConn()) {
                    alertDialog.cancel();
                    BluetoothDevice device = Singleton.getInstance().getBLEDevice();
                    mToolBar.setTitle(getContext().getString(R.string.app_name) +  " " + device.getName() + " 連線中...");
                    mChart.description("裝置連線，等待資料中...");


                    Log.d(info.TAG(), data);
                    read();
                } else {

                }
            }
        };

        // handler read data form bluetooth device
        mReadHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    String data = (String) msg.obj;
                    JSONObject jsonObject = new JSONObject(data);

                    HashMap<String, Object>  map = tools.Convert.toMap(jsonObject);
                    updateTemp(map);
                } catch (JSONException e) {
                    Log.e(info.TAG(), "error :  " + e.getMessage());
                }

            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        mView = mBinding.getRoot();

        initView();

        return mView;
    }

    private void initView() {

        initToolbar();
        initLineChart();
    }

    private void initLineChart() {
        mLineChart = (LineChart) mView.findViewById(R.id.lineChart);
    }

    private void initToolbar() {
        mToolBar = (Toolbar) mView.findViewById(R.id.toolbar);
        ((MainActivity) getActivity()).setSupportActionBar(mToolBar);
        setHasOptionsMenu(true);

        mDrawerLayout = (DrawerLayout) mView.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                (MainActivity)getActivity(),
                mDrawerLayout,
                mToolBar,
                R.string.navigation_drawer_close,
                R.string.navigation_drawer_open
        );
//        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) mView.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!Singleton.getInstance().getBLEAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BluetoothAcceptService.REQUEST_ENABLE_BT);
        }

        registerBroadcastReceiver();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mMainViewModel = new MainViewModel(getActivity());
        mBinding.setMainViewModel(mMainViewModel);

        String[] names = new String[]{
                getActivity().getString(R.string.temp_beans),
        };

        String description = "No chart data available. Use the menu to add entries and data sets!";
        mChart = new MPChart(mLineChart, description, names);
        mChart.init();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.toolbar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        Log.d(info.TAG(), "onClick Options menu item...");
        switch (menuItem.getItemId()) {
            case R.id.action_conn:
                getPairedDevices();
                View view = getLayoutInflater().inflate(R.layout.fragment_device_list, null);
                mDeviceListView = (ListView) view.findViewById(R.id.device_ListView);
                mDeviceListView.setAdapter(mDeviceListAdapter);

                mDeviceListView.setOnItemClickListener(listener);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setIcon(R.drawable.ic_bluetooth_black);
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
                alertDialog = builder.create();
                alertDialog.show();

                break;
        }

        return false;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Log.e(info.TAG(), "onClick Navigation menu item...");
        switch (item.getItemId()) {
            case R.id.nav_record:
                Log.d(info.TAG(), "onClick nav Record item");
                break;
            case R.id.nav_save:
                Log.d(info.TAG(), "onClick nav Save item");
                break;
            case R.id.nav_export:
                Log.d(info.TAG(), "onClick nav Export item");
                break;
            case R.id.nav_exit:
                Log.d(info.TAG(), "onClick nav Exit item");
                exitAPP();
                break;
            case R.id.nav_share:
                Log.d(info.TAG(), "onClick nav Share item");
                break;
            case R.id.nav_send:
                Log.d(info.TAG(), "onClick nav Send item");
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean checkBluetoothConn() {
        if (Singleton.getInstance().getBLEDevice() == null && Singleton.getInstance().getBLESocket() == null) {
            Log.d(info.TAG(), "no device connection");
            return false;
        }

        return true;
    }

    private void updateTemp(HashMap data) {
        mMainViewModel.updateTemp(data);

        String bean = Convert.DecimalPoint((Double)data.get("b"));
        mChart.addEntry(0, Float.parseFloat(bean));
    }

    /**
     * 讀取從藍牙裝置傳送資料
     */
    private void read() {
        Log.d(info.TAG(), "read...");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        try {
                            int bytes;
                            byte[] buffer = new byte[1024];
                            String end = "\n";
                            StringBuilder curMsg = new StringBuilder();


                            while (-1 != (bytes = mInputStream.read(buffer))) {
                                curMsg.append(new String(buffer, 0, bytes, Charset.forName("ISO-8859-1")));
                                int endIdx = curMsg.indexOf(end);
                                if (endIdx != -1) {
                                    String fullMessage = curMsg.substring(0, endIdx + end.length());
                                    curMsg.delete(0, endIdx + end.length());

                                    // Now send fullMessage
                                    // Send the obtained bytes to the UI Activity
                                    Log.d(info.TAG(), fullMessage);
                                    Message msg = new Message();
                                    msg.obj = fullMessage;

                                    mReadHandler.sendMessage(msg);
                                }
                            }
                        } catch (IOException e) {
                            Log.d(info.TAG(), "Error: " + e.getMessage());
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.e(info.TAG(), "Error: " + e.getMessage());
                }
            }
        });

        t.start();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(info.TAG(), "TEST");
            String action = intent.getAction();
            Log.d(info.TAG(), "BLE Connection stat:" + action);

            if (action.equals(BluetoothDevice.ACTION_FOUND))  //收到bluetooth狀態改變
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {

                    mDeviceListAdapter.addItem(BluetoothDeviceAdapter.NoPAIRED_ITEM_TYPE, device);
                    mDeviceListAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    /**
     * register Broadcast's Receiver
     */
    private void registerBroadcastReceiver() {
        IntentFilter intent = new IntentFilter();
//        intent.addAction(BluetoothDevice.ACTION_FOUND);
        intent.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        intent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
//        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
//        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        getContext().registerReceiver(mReceiver, intent);

    }

    /**
     * get device of Paired
     */
    private void  getPairedDevices() {
        mPairedDevices = Singleton.getInstance().getBLEAdapter().getBondedDevices();
        Log.e(info.TAG(), ""+mPairedDevices.size());
        ArrayList<BluetoothDevice> list = new ArrayList<>();
        for(BluetoothDevice device : mPairedDevices) {
            list.add(device);
        }

        mDeviceListAdapter.setData(BluetoothDeviceAdapter.PAIRED_ITEM_TYPE, list);
    }

    /**
     * Bluetooth device of Listview's item ClickListener
     */
    private ListView.OnItemClickListener listener = new ListView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
            Log.d(info.TAG(), "item :" + position);
            final int p = position;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = Message.obtain();
                    BluetoothDevice device= mDeviceListAdapter.getDevice(p);
                    Singleton.getInstance().setBLEDevice(device);
                    try {
                        Singleton.getInstance().setBLESocket(device.createRfcommSocketToServiceRecord(info.BluetoothUUID()));

                        try {
                            Singleton.getInstance().getBLESocket().connect();
                            mInputStream = Singleton.getInstance().getBLESocket().getInputStream();
                            mMainViewModel.setOutputStream(Singleton.getInstance().getBLESocket().getOutputStream());

                            msg.obj = "device " + device.getName() + " Connection success";

                        } catch (IOException ioe) {
                            try {
                                Singleton.getInstance().setBLESocket((BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1));
                                Singleton.getInstance().getBLESocket().connect();

                                mInputStream = Singleton.getInstance().getBLESocket().getInputStream();
                                mMainViewModel.setOutputStream(Singleton.getInstance().getBLESocket().getOutputStream());


                                msg.obj = "device " + device.getName() + " Connection success";
                            } catch (Exception e) {
                                Log.e(info.TAG(), "error : " + e.getMessage());
                                msg.obj = "device " + device.getName() + " Connection fail";
                            }
                        }
                    } catch (IOException ioe) {
                        Log.e(info.TAG(), "error : " + ioe.getMessage());
                        msg.obj = "device " + device.getName() + " Connection fail";
                    }

                    mConnHandler.sendMessage(msg);
                }
            });

            t.start();
        }
    };

    private void exitAPP() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setTitle("Exit Application?");
        alertDialogBuilder.setMessage("Click yes to exit!");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        });

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}