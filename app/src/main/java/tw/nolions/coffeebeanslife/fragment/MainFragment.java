package tw.nolions.coffeebeanslife.fragment;

import android.Manifest;
import android.app.Activity;
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
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;

import androidx.annotation.Nullable;
import tools.Convert;
import tools.info;
import tw.nolions.coffeebeanslife.MainActivity;
import tw.nolions.coffeebeanslife.Singleton;
import tw.nolions.coffeebeanslife.callback.ViewModelCallback;
import tw.nolions.coffeebeanslife.databinding.FragmentMainBinding;

import com.github.mikephil.charting.charts.LineChart;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import tw.nolions.coffeebeanslife.R;
import tw.nolions.coffeebeanslife.model.Temperature;
import tw.nolions.coffeebeanslife.service.BluetoothAcceptService;
import tw.nolions.coffeebeanslife.service.asyncTask.ExportToCSVAsyncTask;
import tw.nolions.coffeebeanslife.viewModel.MainViewModel;
import tw.nolions.coffeebeanslife.widget.AutoTempAdapter;
import tw.nolions.coffeebeanslife.widget.BluetoothDeviceAdapter;
import tw.nolions.coffeebeanslife.widget.MPChart;
import tw.nolions.coffeebeanslife.widget.SmallProgressDialogUtil;

public class MainFragment extends Fragment implements
        Toolbar.OnCreateContextMenuListener,
        NavigationView.OnNavigationItemSelectedListener,
        ViewModelCallback
{

    // UI Widget
    private View mView;
    private Toolbar mToolBar;
    private LineChart mLineChart;
    private ListView mDeviceListView;
    private ListView mTempListView;
    private AlertDialog mAlertDialog;

    // view model
    private MainViewModel mMainViewModel;
    private FragmentMainBinding mBinding;

    // bluetooth
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    // widget
    private MPChart mChart;
    private BluetoothDeviceAdapter mDeviceListAdapter;
    private AutoTempAdapter mAutoTempAdapter;
    private SmallProgressDialogUtil mDeviceConnectionDialog;

    // handler
    private Handler mConnHandler;
    private Handler mReadHandler;
    private Handler mWriteHandler;

    private DrawerLayout mDrawerLayout;

    // data
    private Set<BluetoothDevice> mPairedDevices;
    private Long mStartTime = 0L;
    private HashMap<Long, JSONObject> mTempRecord;
    private ArrayList<Temperature> mTemperatureList;
    private String mNowTemp = "";
    private Boolean mActionStart = false;
    private String mModel;

    private Context mContext;
    private Activity mActivity;

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
        this.mContext = this.getContext();
        this.mActivity = this.getActivity();

        mModel = "m";

        mDeviceListAdapter = new BluetoothDeviceAdapter(this.mContext);
        mAutoTempAdapter = new AutoTempAdapter(this.mContext);
        mTempRecord = new HashMap<>();
        mTemperatureList = new ArrayList<>();

        // 檢查裝置是否支援藍牙
        bluetoothSupport();
        // 請求權限
        grantedPermission();

        initHandler();
    }

    private void grantedPermission() {
        ActivityCompat.requestPermissions(mActivity,
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                },
                info.PermissionsRequestAccessLocationCode()
        );
    }


    private void bluetoothSupport() {
        if (Singleton.getInstance().getBLEAdapter() == null) {
            alert(getString(R.string.no_support_bluetooth));
        }
    }

    private void initHandler() {
        // handler bluetooth device connection
        mConnHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String data = (String) msg.obj;
                mDeviceConnectionDialog.dismiss();
                alert(data);
                if (checkBluetoothConn()) {
                    mAlertDialog.cancel();
                    BluetoothDevice device = Singleton.getInstance().getBLEDevice();
                    mToolBar.setTitle(mContext.getString(R.string.app_name) +  " " + device.getName() + " 連線中...");
                    mChart.description("裝置連線，等待資料中...");

                    Log.d(info.TAG(), "MainFragment::initHandler, mConnHandler data: " + data);
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
                String data = (String) msg.obj;
                Log.d(info.TAG(), "MainFragment::initHandler, mReadHandler data: " + data);
                updateTemp(data);

            }
        };

        mWriteHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String data = (String) msg.obj;
                Log.d(info.TAG(), "MainFragment::initHandler, mReadHandler data: " + data);
                updateTemp(data);

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
        mDeviceConnectionDialog = new SmallProgressDialogUtil(
                mContext,
                mActivity.getString(R.string.ble_device_connecting)
        );

        initToolbar();
        initNavigationView();
        initLineChart();
    }

    private void initLineChart() {
        mLineChart = (LineChart) mView.findViewById(R.id.lineChart);
    }

    private void initToolbar() {
        mToolBar = (Toolbar) mView.findViewById(R.id.toolbar);
        ((MainActivity) mActivity).setSupportActionBar(mToolBar);
        setHasOptionsMenu(true);

        mDrawerLayout = (DrawerLayout) mView.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                (MainActivity)mActivity,
                mDrawerLayout,
                mToolBar,
                R.string.navigation_drawer_close,
                R.string.navigation_drawer_open
        );
//        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }


    public void initNavigationView() {
        NavigationView navigationView = (NavigationView) mView.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Menu menu = navigationView.getMenu();
        Menu submenu = menu.addSubMenu(getString(R.string.appInfo));

        submenu.add(getString(R.string.versionNameLabel)+info.VersionName(this.mContext)).setCheckable(false);

        navigationView.invalidate();

        SwitchCompat statusDrawerSwitch = (SwitchCompat) navigationView.getMenu().findItem(R.id.nav_OnOff).getActionView();
        statusDrawerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 控制接收溫度是否顯示在線圖上
                if (isChecked) {
                    Log.d(info.TAG(), "MainFragment::initNavigationView(), statusDrawerSwitch: action start");
                    mStartTime = System.currentTimeMillis()/1000;
                    setActionStart(true);

                } else {
                    Log.d(info.TAG(), "MainFragment::initNavigationView(), statusDrawerSwitch: action stop");
                    mMainViewModel.setIsFirstCrack(false);
                    mMainViewModel.setIsSecondCrack(false);

                    mChart.refresh();
                }
            }
        });

        SwitchCompat modelDrawerSwitch = (SwitchCompat) navigationView.getMenu().findItem(R.id.nav_model_sel).getActionView();
        modelDrawerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                final String data;
                // 控制烘豆機為手動或自動模式
                if (!isChecked) {
                    Log.d(info.TAG(), "MainFragment::initNavigationView(), modelDrawerSwitch: Manual model");
                    mModel = "m";
                    data = "m\r";
                } else {
                    Log.d(info.TAG(), "MainFragment::initNavigationView(), modelDrawerSwitch: Auto model");
                    mModel = "a";
                    data = "a\r";
                }
                setLineChart();
                bluetoothWrite(data);
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final String d = data;
                        bluetoothWrite(d);

                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isChecked) {
                                    setAutoModeTempAlertView();
                                }
                            }
                        });

                    }
                });

                t.start();


            }
        });
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

        mMainViewModel = new MainViewModel(this);

        mBinding.setMainViewModel(mMainViewModel);

        setLineChart();
    }

    private void setLineChart()
    {
        String str = mActivity.getString(R.string.temp_stove);
        if (mModel == "m") {
            str = mActivity.getString(R.string.temp_stove);
        } else if( mModel == "a") {
            Log.e("test", "sss");
            str = mActivity.getString(R.string.temp_beans);
        }

        String[] names = new String[]{str};

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
        Log.d(info.TAG(), "MainFragment::onOptionsItemSelected(), onClick Options menu item...");
        switch (menuItem.getItemId()) {
            case R.id.action_conn:
                if (!supportBluetooth()) {
                    alert(getString(R.string.no_device_connection));
                    return false;
                } else if (!enableBluetooht()) {
                    alert(getString(R.string.no_enable_bluetooth));
                    return false;
                }

                getPairedDevices();
                View view = getLayoutInflater().inflate(R.layout.fragment_device_list, null);
                mDeviceListView = (ListView) view.findViewById(R.id.device_ListView);
                mDeviceListView.setAdapter(mDeviceListAdapter);

                mDeviceListView.setOnItemClickListener(listener);

                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
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
                mAlertDialog = builder.create();
                mAlertDialog.show();
                break;
        }

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Log.d(info.TAG(), "MainFragment::onNavigationItemSelected(), onClick Navigation menu item...");
        switch (item.getItemId()) {
//            case R.id.nav_record:
//                Log.d(info.TAG(), "MainFragment::onNavigationItemSelected(), onClick nav Record item");
//                break;
//            case R.id.nav_save:
//                Log.d(info.TAG(), "MainFragment::onNavigationItemSelected(), onClick nav Save item");
//                break;
            case R.id.nav_export:
                Log.d(info.TAG(), "MainFragment::onNavigationItemSelected(), onClick nav Export item");
                Date date = new Date(System.currentTimeMillis());
                String filename = new SimpleDateFormat("yyyyMMddhhmmss").format(date);
                mChart.saveToImage(filename);

                new ExportToCSVAsyncTask(mContext, filename).execute(mTempRecord);

                break;
            case R.id.nav_exit:
                Log.d(info.TAG(), "MainFragment::onNavigationItemSelected(), onClick nav Exit item");
                exitAPP();
                break;
//            case R.id.nav_share:
//                Log.d(info.TAG(), "MainFragment::onNavigationItemSelected(), onClick nav Share item");
//                break;
//            case R.id.nav_send:
//                Log.d(info.TAG(), "MainFragment::onNavigationItemSelected(), onClick nav Send item");
//                break;
            default:
                return false;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void updateTargetTemp(final String temp) {
        Log.d(info.TAG(), "updateTargetTemp:" +  temp);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                bluetoothWrite(temp);
            }
        });

        t.start();
    }

    @Override
    public void firstCrack() {
        mMainViewModel.setIsFirstCrack(true);
        if (System.currentTimeMillis()/1000 - mStartTime != 0) {
            Long sec = System.currentTimeMillis()/1000 - mStartTime;
            mMainViewModel.setFirstCrackTime(sec.intValue());
            mChart.addEntry(0, Float.parseFloat(mNowTemp), sec);
        }
        mChart.addXAxisLimitLine(getString(R.string.first_crack));
    }

    @Override
    public void secondCrack() {
        mMainViewModel.setIsSecondCrack(true);
        if (System.currentTimeMillis()/1000 - mStartTime != 0) {
            Long sec = System.currentTimeMillis()/1000 - mStartTime;
            mMainViewModel.setSecondCrackTime(sec.intValue());
            mChart.addEntry(0, Float.parseFloat(mNowTemp), sec);
        }
        mChart.addXAxisLimitLine(getString(R.string.second_crack));


    }

    @Override
    public void startAction(final boolean action) {
        if (Singleton.getInstance().getBLEDevice() != null && this.getActionStart()) {
            String msg = getString(R.string.exit_beans);
            mMainViewModel.setIsImport(action);
            if (action) {
                mChart.refresh();
                mMainViewModel.setIsFirstCrack(false);
                mMainViewModel.setIsSecondCrack(false);

                int index = 0;
                ArrayList<Temperature> tempTemperatureList = new ArrayList<>();
                for(int i = mTemperatureList.size(); i >= 1; i--) {
                    if (index >= 5) {
                        break;
                    }
                    tempTemperatureList.add(mTemperatureList.get(i-1));
                    index++;
                }

                Collections.reverse(tempTemperatureList);
                for(Temperature model : tempTemperatureList) {
                    mChart.addEntry(0, model.getTemp(), model.getSeconds());
                }

                msg = getString(R.string.enter_beans);
                mChart.addXAxisLimitLine(getString(R.string.enter_beans));
            } else {
                setActionStart(false);
            }

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    String data = "c\r";
                    if (action) {
                        data = "o\r";
                    }

                    bluetoothWrite(data);
                }
            });

            t.start();

            alert(msg);
        } else if(Singleton.getInstance().getBLEDevice() == null) {
            alert(getString(R.string.no_device_connection));
        } else if(!this.getActionStart()) {
            alert(getString(R.string.no_action_start));
        }
    }

    public void setActionStart(boolean status)
    {
        this.mActionStart = status;
    }

    public boolean getActionStart()
    {
        return this.mActionStart;
    }

    private boolean checkBluetoothConn() {
        if (Singleton.getInstance().getBLEDevice() == null && Singleton.getInstance().getBLESocket() == null) {
            Log.d(info.TAG(), "MainFragment::checkBluetoothConn(), no device connection");
            return false;
        }

        return true;
    }

    private void updateTemp(String data) {
        try {
            JSONObject jsonObject = new JSONObject(data);

            HashMap<String, Object>  map = Convert.toMap(jsonObject);
            mMainViewModel.updateTemp(map);

            if (mStartTime == 0L) {
                mStartTime = System.currentTimeMillis()/1000;
            }

            Long sec = 1L;
            if (System.currentTimeMillis()/1000 - mStartTime != 0) {
                sec = System.currentTimeMillis()/1000 - mStartTime;
            }

            if (this.getActionStart()) {
                mTempRecord.put(sec, jsonObject);
                mNowTemp = Convert.DecimalPoint((Double)map.get("s"));
                if (mModel == "a") {
                    mNowTemp = Convert.DecimalPoint((Double)map.get("b"));
                }

                Temperature model = new Temperature(Float.parseFloat(mNowTemp), sec);
                mTemperatureList.add(model);
                mChart.addEntry(0, Float.parseFloat(mNowTemp), sec);
                mMainViewModel.setRunTime(sec.intValue());
            }
        } catch (JSONException e) {
            Log.e(info.TAG(), "MainFragment::updateTemp(), error :  " + e.getMessage());
        }
    }

    /**
     * 讀取從藍牙裝置傳送資料
     */
    private void read() {
        Log.d(info.TAG(), "MainFragment::read()");
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
                                    Log.d(info.TAG(), "MainFragment::read(), data:" + fullMessage);
                                    Message msg = new Message();
                                    msg.obj = fullMessage;

                                    mReadHandler.sendMessage(msg);
                                }
                            }
                        } catch (IOException e) {
                            Log.d(info.TAG(), "MainFragment::read(), IOException Error: " + e.getMessage());
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.e(info.TAG(), "MainFragment::read(), Exception Error: " + e.getMessage());
                }
            }
        });

        t.start();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(info.TAG(), "MainFragment::mReceiver, BLE Connection stat:" + action);

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
        mContext.registerReceiver(mReceiver, intent);

    }

    /**
     * get device of Paired
     */
    private void  getPairedDevices() {
        mPairedDevices = Singleton.getInstance().getBLEAdapter().getBondedDevices();
        Log.d(info.TAG(), "MainFragment::getPairedDevices(), Paired Devices size"+mPairedDevices.size());
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
            Log.d(info.TAG(), "MainFragment::listener, item :" + position);
            mAlertDialog.dismiss();
            mDeviceConnectionDialog.show();
            final int p = position;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = Message.obtain();
                    BluetoothDevice device= mDeviceListAdapter.getDevice(p);
                    Singleton.getInstance().setBLEDevice(device);

                    closeBTEConnection();

                    try {
                        Singleton.getInstance().setBLESocket(device.createRfcommSocketToServiceRecord(info.BluetoothUUID()));

                        try {
                            Singleton.getInstance().getBLESocket().connect();
                            mInputStream = Singleton.getInstance().getBLESocket().getInputStream();
                            mOutputStream = Singleton.getInstance().getBLESocket().getOutputStream();

                            msg.obj = "device " + device.getName() + " Connection success";

                        } catch (IOException ioe) {
                            try {
                                Singleton.getInstance().setBLESocket((BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1));
                                Singleton.getInstance().getBLESocket().connect();

                                mInputStream = Singleton.getInstance().getBLESocket().getInputStream();

                                msg.obj = "device " + device.getName() + " Connection success";
                            } catch (Exception e) {
                                Log.e(info.TAG(), "MainFragment::listener, Exception error : " + e.getMessage());
                                msg.obj = "device " + device.getName() + " Connection fail";
                            }
                        }
                    } catch (IOException ioe) {
                        Log.e(info.TAG(), "MainFragment::listener, IOException error : " + ioe.getMessage());
                        msg.obj = "device " + device.getName() + " Connection fail";
                    }

                    mConnHandler.sendMessage(msg);
                }
            });

            t.start();
        }
    };

    private void closeBTEConnection()
    {
        Log.d(info.TAG(), "MainFragment::closeBTEConnection");

        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (Exception e) {
                Log.e(info.TAG(), "MainFragment::closeBTEConnection, bluetooth InputStream close Exception error : " + e.getMessage());
            }
            mInputStream = null;
        }

        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch(Exception e) {
                Log.e(info.TAG(), "MainFragment::closeBTEConnection, bluetooth OutputStream close Exception error : " + e.getMessage());
            }
            mOutputStream = null;
        }

        if (Singleton.getInstance().getBLESocket() != null) {
            try {
                Singleton.getInstance().getBLESocket().close();
            } catch (Exception e) {
                Log.e(info.TAG(), "MainFragment::closeBTEConnection, bluetooth socket close Exception error : " + e.getMessage());
            }

            Singleton.getInstance().setBLESocket(null);
        }
    }

    private void exitAPP() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(mActivity.getString(R.string.exit_APP));
        alertDialogBuilder.setMessage(mActivity.getString(R.string.exit_APP_describe));
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

        mAlertDialog = alertDialogBuilder.create();
        mAlertDialog.show();
    }

    private void setAutoModeTempAlertView() {
        View view = getLayoutInflater().inflate(R.layout.temp_list, null);
        mTempListView = (ListView) view.findViewById(R.id.temp_ListView);
        mTempListView.setAdapter(mAutoTempAdapter);

        // TODO
        ArrayList<Temperature> temperatures = new ArrayList<>();
        temperatures.add(new Temperature(30, 40L));
        temperatures.add(new Temperature(40, 100L));
        temperatures.add(new Temperature(50, 150L));
        temperatures.add(new Temperature(100, 200L));
        mAutoTempAdapter.setData(temperatures);

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setIcon(R.drawable.ic_temp);
        builder.setTitle(R.string.settingTemp);
        builder.setCancelable(false);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
//        builder.setNeutralButton(R.string.add, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//            }
//        });
        builder.setView(view);
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    /**
     * 透過藍芽傳送資料
     *
     * @param data 欲傳送的資料內容
     */
    private void bluetoothWrite(final String data)
    {
        try {
            mOutputStream.write(data.getBytes());
        } catch (IOException e) {
            Log.e(info.TAG(), "error :  " + e.getMessage());
        }
    }

    /**
     * 是否支援藍牙
     * @return
     */
    private boolean supportBluetooth()
    {
        if (Singleton.getInstance().getBLEAdapter() == null) {
            return false;
        }

        return true;
    }

    /**
     * 藍牙是否啟動
     * @return
     */
    private boolean enableBluetooht()
    {
        if (!Singleton.getInstance().getBLEAdapter().isEnabled()) {
            return false;
        }

        return true;
    }

    private void alert(String msg) {
        Snackbar.make(mView, msg, Snackbar.LENGTH_SHORT).show();
    }
}



