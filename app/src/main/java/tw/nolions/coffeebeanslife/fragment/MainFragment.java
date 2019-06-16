package tw.nolions.coffeebeanslife.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import tw.nolions.coffeebeanslife.R;
import tw.nolions.coffeebeanslife.model.Temperature;
import tw.nolions.coffeebeanslife.service.Application.MainApplication;
import tw.nolions.coffeebeanslife.service.BluetoothAcceptService;
import tw.nolions.coffeebeanslife.service.Service.BluetoothService;
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

    private MainApplication mAPP;

    // widget
    private MPChart mChart;
    private BluetoothDeviceAdapter mDeviceListAdapter;
    private AutoTempAdapter mAutoTempAdapter;
    private SmallProgressDialogUtil mDeviceConnectionDialog;

    private DrawerLayout mDrawerLayout;

    // data
    private int mStartTime = 0;
    private HashMap<Integer, JSONObject> mTempRecord;
    private ArrayList<Temperature> mTemperatureList;
    private String mNowTemp = "";
    private Boolean mActionStart = false;
    private String mModel;
    private int mScreenHeight;
    private int mScreenWidth;

    private Context mContext;
    private Activity mActivity;

    private BluetoothService mBluetoothService;

    private Boolean mIsBound = false;

    private Handler mConnHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(info.TAG(), "what:" + msg.what+ " obj:" + msg.obj);
            switch (msg.what) {
                case 0:
                case 1:
                case 2:
                    alert((String) msg.obj);
                    break;
                case 3:
                    updateTemp((String) msg.obj);
                    break;
                case 4:
                    break;

            }
        }
    };

    @NonNull
    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        super.onDetach();
        if (mIsBound) {
            mActivity.unbindService(mConnection);
            mIsBound = false;
            stopBluetoothConnect();
        }
        mContext.stopService(new Intent(mActivity, BluetoothService.class));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mIsBound) {
            mActivity.unbindService(mConnection);
            mIsBound = false;
            stopBluetoothConnect();
        }
        mContext.stopService(new Intent(mActivity, BluetoothService.class));
    }

    private void stopBluetoothConnect() {
        if (mBluetoothService != null) {
            mBluetoothService.stop();
            Log.d(mAPP.TAG(), "bluetooth stop connection");
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mBluetoothService = ((BluetoothService.LocalBinder)binder).getInstance();
            mBluetoothService.setHandler(mConnHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void init() {
        mContext = this.getContext();
        mActivity = this.getActivity();
        mAPP = (MainApplication) mActivity.getApplication();

        mModel = "m";

        mDeviceListAdapter = new BluetoothDeviceAdapter(this.mContext);
        mAutoTempAdapter = new AutoTempAdapter(this.mContext);
        mTempRecord = new HashMap<>();
        mTemperatureList = new ArrayList<>();

        // 檢查裝置是否支援藍牙
        bluetoothSupport();
        // 請求權限
        grantedPermission();
        // 啟動Service
        onStartService();
    }

    private void grantedPermission() {
        ActivityCompat.requestPermissions(mActivity,
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, mAPP.PermissionsRequestAccessLocationCode()
        );
    }

    private void bluetoothSupport() {
        if (Singleton.getInstance().getBLEAdapter() == null) {
            alert(getString(R.string.no_support_bluetooth));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        mView = mBinding.getRoot();

        initView();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mScreenHeight = displayMetrics.heightPixels;
        mScreenWidth = displayMetrics.widthPixels;

        return mView;
    }

    private void onStartService() {
        Intent intent = new Intent(mActivity.getBaseContext(), BluetoothService.class);
        intent.putExtra("TAG", mAPP.TAG());
        intent.putExtra("UUID", mAPP.BluetoothUUID());
        mActivity.startService(intent);
        mActivity.bindService(new Intent(mActivity, BluetoothService.class), mConnection, Context.BIND_AUTO_CREATE);
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

        submenu.add(getString(R.string.versionNameLabel) + mAPP.VersionName()).setCheckable(false);

        navigationView.invalidate();

        // 啟動/關閉
        SwitchCompat statusDrawerSwitch = (SwitchCompat) navigationView.getMenu().findItem(R.id.nav_OnOff).getActionView();
        statusDrawerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
//                if (Singleton.getInstance().getBLEDevice() != null) {
                if (mBluetoothService.getState() == 2) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("action", "status");
                            if (isChecked) {
                                map.put("start", true);
                            } else {
                                map.put("start", false);
                            }


                            bluetoothWrite(new JSONObject(map));

                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 控制接收溫度是否顯示在線圖上
                                    if (isChecked) {
                                        Log.d(mAPP.TAG(), "MainFragment::initNavigationView(), statusDrawerSwitch: action start");
                                        mStartTime = (int) System.currentTimeMillis()/1000;
                                        setActionStart(true);

                                    } else {
                                        Log.e(mAPP.TAG(), "MainFragment::initNavigationView(), statusDrawerSwitch: action stop");
                                        mMainViewModel.setIsFirstCrack(false);
                                        mMainViewModel.setIsSecondCrack(false);
                                        setActionStart(false);

                                        mChart.refresh();
                                        mMainViewModel.refresh();
                                    }
                                }
                            });
                        }
                    });

                    t.start();

                } else if(mBluetoothService.getBluetoothDevice() == null) {
                    alert(getString(R.string.no_device_connection));
                } else if(!getActionStart()) {
                    alert(getString(R.string.no_action_start));
                }
            }
        });

        // 切換手/自動模式
        SwitchCompat modelDrawerSwitch = (SwitchCompat) navigationView.getMenu().findItem(R.id.nav_model_sel).getActionView();
        modelDrawerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (Singleton.getInstance().getBLEDevice() != null) {
                    setLineChart();

                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("action", "model");
                            if (isChecked) {
                                map.put("auto", true);
                            } else {
                                map.put("auto", false);
                            }

                            bluetoothWrite(new JSONObject(map));

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
                } else if(mBluetoothService.getBondedDevices() == null) {
                    Log.d(mAPP.TAG(), "MainFragment::modelDrawerSwitch::onCheckedChanged()" + getString(R.string.no_device_connection));
                    alert(getString(R.string.no_device_connection));
                } else if(!getActionStart()) {
                    Log.d(mAPP.TAG(), "MainFragment::modelDrawerSwitch::onCheckedChanged()" + getString(R.string.no_action_start));
                    alert(getString(R.string.no_action_start));
                }
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

        mMainViewModel = new MainViewModel(this, mAPP);

        mBinding.setMainViewModel(mMainViewModel);

        setLineChart();
    }

    private void setLineChart()
    {
        String str = mActivity.getString(R.string.temp_stove);
        if (mModel.equals("m")) {
            str = mActivity.getString(R.string.temp_stove);
        } else if(mModel.equals("a")) {
            str = mActivity.getString(R.string.temp_beans);
        }

        String[] names = new String[]{str};

        mChart = new MPChart(mLineChart,  names);
        mChart.init();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.toolbar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        Log.d(mAPP.TAG(), "MainFragment::onOptionsItemSelected(), onClick Options menu item...");
        switch (menuItem.getItemId()) {
            case R.id.action_conn:
                if (!mBluetoothService.isSupport()) {
                    Log.e(mAPP.TAG(), "MainFragment::actionBean(), " + getString(R.string.no_support_bluetooth));
                    alert(getString(R.string.no_support_bluetooth));
                    return false;
                } else if(!mBluetoothService.isEnable()) {
                    Log.e(mAPP.TAG(), "MainFragment::actionBean(), " + getString(R.string.no_enable_bluetooth));
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d(mAPP.TAG(), "MainFragment::onNavigationItemSelected(), onClick Navigation menu item...");
        switch (item.getItemId()) {
//            case R.id.nav_record:
//                Log.d(mAPP.TAG(), "MainFragment::onNavigationItemSelected(), onClick nav Record item");
//                break;
//            case R.id.nav_save:
//                Log.d(mAPP.TAG(), "MainFragment::onNavigationItemSelected(), onClick nav Save item");
//                break;
            case R.id.nav_export:
                Log.d(mAPP.TAG(), "MainFragment::onNavigationItemSelected(), onClick nav Export item");
                Date date = new Date(System.currentTimeMillis());
                String filename = new SimpleDateFormat("yyyyMMddhhmmss").format(date);
                mChart.saveToImage(filename);

                new ExportToCSVAsyncTask(mContext, filename).execute(mTempRecord);

                break;
            case R.id.nav_stopConnect:
                if(getActionStart()) {
                    alert(getString(R.string.need_action_stop));
                } else {
                    mToolBar.setTitle(getString(R.string.app_name));
                    mChart.description(getString(R.string.device_connect_wait));
                    stopBluetoothConnect();
                    alert(getString(R.string.ble_device_stop_connecting));
                }

                break;
            case R.id.nav_exit:
                Log.d(mAPP.TAG(), "MainFragment::onNavigationItemSelected(), onClick nav Exit item");
                exitAPP();
                break;
//            case R.id.nav_share:
//                Log.d(mAPP.TAG(), "MainFragment::onNavigationItemSelected(), onClick nav Share item");
//                break;
//            case R.id.nav_send:
//                Log.d(mAPP.TAG(), "MainFragment::onNavigationItemSelected(), onClick nav Send item");
//                break;
            default:
                return false;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void updateTargetTemp(final String temp) {
        Log.d(mAPP.TAG(), "updateTargetTemp:" +  temp);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, Object> map = new HashMap<>();
                map.put("action", "temp");
                map.put("target", Integer.valueOf(temp));
                bluetoothWrite(new JSONObject(map));
            }
        });

        t.start();
    }

    @Override
    public void firstCrack() {
        mMainViewModel.setIsFirstCrack(true);
        if (System.currentTimeMillis()/1000 - mStartTime != 0) {
            int sec = (int) System.currentTimeMillis()/1000 - mStartTime;
            mMainViewModel.setFirstCrackTime(sec);
            mChart.addEntry(0, Float.parseFloat(mNowTemp), sec);
        }
        mChart.addXAxisLimitLine(getString(R.string.first_crack));
    }

    @Override
    public void secondCrack() {
        mMainViewModel.setIsSecondCrack(true);
        if (System.currentTimeMillis()/1000 - mStartTime != 0) {
            int sec = (int)System.currentTimeMillis()/1000 - mStartTime;
            mMainViewModel.setSecondCrackTime(sec);
            mChart.addEntry(0, Float.parseFloat(mNowTemp), sec);
        }
        mChart.addXAxisLimitLine(getString(R.string.second_crack));


    }

    @Override
    public void actionBean(final boolean action) {
        if (mBluetoothService.getState() == 2 && this.getActionStart()) {
            Log.d(mAPP.TAG(), "MainFragment::actionBean(), action: " + action);
            mMainViewModel.setIsImport(action);

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("action", "inBean");
                    map.put("in", action);

                    bluetoothWrite(new JSONObject(map));

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String msg = getString(R.string.exit_beans);
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
                            alert(msg);
                        }
                    });
                }
            });

            t.start();
        } else if(mBluetoothService.getBluetoothDevice() == null) {
            Log.e(mAPP.TAG(), "MainFragment::actionBean(), " + getString(R.string.no_device_connection));
            alert(getString(R.string.no_device_connection));
        } else if(!this.getActionStart()) {
            Log.e(mAPP.TAG(), "MainFragment::actionBean(), " + getString(R.string.no_action_start));
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

    private void updateTemp(String data) {
        try {
            JSONObject jsonObject = new JSONObject(data);

            HashMap<String, Object>  map = Convert.toMap(jsonObject);
            mMainViewModel.updateTemp(map);
            Log.d(mAPP.TAG(), "sec:" + data);

            int sec = 1;
            if (System.currentTimeMillis()/1000 - mStartTime != 0) {
                sec = (int)System.currentTimeMillis()/1000 - mStartTime;
            }

            if (this.getActionStart()) {
                mTempRecord.put(sec, jsonObject);
                mNowTemp = String.valueOf(map.get("s"));
                if (mModel.equals("a")) {
                    mNowTemp = String.valueOf(map.get("b"));
                }

                Temperature model = new Temperature(Float.parseFloat(mNowTemp), sec);
                mTemperatureList.add(model);
                mChart.addEntry(0, Float.parseFloat(mNowTemp), sec);
                mMainViewModel.setRunTime(sec);
            }
        } catch (JSONException e) {
            Log.e(mAPP.TAG(), "MainFragment::updateTemp(), error :  " + e.getMessage());
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(mAPP.TAG(), "MainFragment::mReceiver, BLE Connection stat:" + action);

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
        mDeviceListAdapter.setData(BluetoothDeviceAdapter.PAIRED_ITEM_TYPE, mBluetoothService.pairedDevices());
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

            BluetoothDevice device = mDeviceListAdapter.getDevice(position);
            mBluetoothService.setBluetoothDevice(device);
            mIsBound = true;

            // TODO move to handle
            mDeviceConnectionDialog.dismiss();
            mToolBar.setTitle(mContext.getString(R.string.app_name) +  " " + device.getName() + " 連線中...");
        }
    };

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

        ArrayList<Temperature> temperatures = new ArrayList<>();
        temperatures.add(new Temperature(100, 20));
        temperatures.add(new Temperature(20, 30));
        temperatures.add(new Temperature(200, 40));
        temperatures.add(new Temperature(20, 50));
        temperatures.add(new Temperature(250, 300));
        mAutoTempAdapter.setData(temperatures);

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setIcon(R.drawable.ic_temp);
        builder.setTitle(R.string.settingTemp);
        builder.setCancelable(false);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("action", "jobs");

                try {
                    for (int i = 0; i < mAutoTempAdapter.getData().size(); i++) {
                        Temperature t = mAutoTempAdapter.getData().get(i);
                        Log.d(info.TAG(), "time: " + t.getSeconds() + ", temp:" + t.getTemp());
                        map.put("sec", t.getSeconds());
                        map.put("temp", t.getTemp());
                        bluetoothWrite(new JSONObject(map));
                        Thread.sleep(3000);
                    }
                } catch (Exception e) {
                    e.getLocalizedMessage();
                }
                dialog.cancel();
            }
        });
        builder.setView(view);
        mAlertDialog = builder.create();
        mAlertDialog.show();
        mAlertDialog.getWindow().setLayout(mScreenWidth/10*9, mScreenHeight/10*9);
        mAlertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }

    /**
     * 透過藍芽傳送資料
     *
     * @param jsonObject 欲傳送的資料內容
     */
    private void bluetoothWrite(final JSONObject jsonObject)
    {
        mBluetoothService.write(jsonObject.toString().getBytes());
    }

    /**
     * Alert Message
     * @param msg String
     */
    private void alert(String msg) {
        try {
            Snackbar.make(mView, msg, Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {

        }

    }
}
