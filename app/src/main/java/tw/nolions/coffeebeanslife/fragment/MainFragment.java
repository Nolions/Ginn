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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.ArrayMap;
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

import com.github.mikephil.charting.charts.LineChart;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import tools.Convert;
import tw.nolions.coffeebeanslife.Const;
import tw.nolions.coffeebeanslife.MainActivity;
import tw.nolions.coffeebeanslife.MainApplication;
import tw.nolions.coffeebeanslife.R;
import tw.nolions.coffeebeanslife.Singleton;
import tw.nolions.coffeebeanslife.callback.ViewModelCallback;
import tw.nolions.coffeebeanslife.databinding.FragmentMainBinding;
import tw.nolions.coffeebeanslife.library.SlopeTemperature;
import tw.nolions.coffeebeanslife.model.Temperature;
import tw.nolions.coffeebeanslife.model.entity.RecordEntity;
import tw.nolions.coffeebeanslife.model.recordDao;
import tw.nolions.coffeebeanslife.service.service.BluetoothService;
import tw.nolions.coffeebeanslife.service.service.BroadcastService;
import tw.nolions.coffeebeanslife.viewModel.MainViewModel;
import tw.nolions.coffeebeanslife.widget.AutoTempAdapter;
import tw.nolions.coffeebeanslife.widget.BluetoothDeviceAdapter;
import tw.nolions.coffeebeanslife.widget.MPChart;
import tw.nolions.coffeebeanslife.widget.SmallProgressDialogUtil;

public class MainFragment extends Fragment implements
        Toolbar.OnCreateContextMenuListener,
        NavigationView.OnNavigationItemSelectedListener,
        ViewModelCallback {

    // UI Widget
    private View mView;
    private Toolbar mToolBar;
    private LineChart mLineChart;
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
    private int mRunTime = 0;
    private int mFirstCrackStartTime = 0;
    private int mFirstCrackTime = 0;
    private int mSecondCrackStartTime = 0;
    private int mSecondCrackTime = 0;
    private ArrayMap<Integer, JSONObject> mTempRecord;
    private String mBeanTemp = "0";
    private String mStoveTemp = "0";
    private Boolean mActionStart = false;
    private String mModel = "manual";
    private int mScreenHeight;
    private int mScreenWidth;

    private Context mContext;
    private Activity mActivity;

    // Service
    private BluetoothService mBluetoothService;
    private BroadcastService mBroadcastService;

    private Boolean mIsBound = false;

    private recordDao mRecordDao;

    private RecordListFragment mRecordListFragment;

    private Handler mConnHandler = new MainHandler(this);

    private int mFirstCrack = 0;
    private int mSecondCrack = 0;
    private int mInBean = 0;
    private int mAutoModeRunTimeSec = 0;
    private boolean mIsInBean = false;
    private boolean mIsFirstCrack = false;
    private boolean mIsSecondCrack = false;

    private final int AUTO_MODEL_TIME_RANGE = 2;
    private ArrayMap<Integer, Integer> mAutoTempJobs;

    private SlopeTemperature mSlopeTemperature;


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
            mBluetoothService = ((BluetoothService.LocalBinder) binder).getInstance();
            mBluetoothService.setHandler(mConnHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private ServiceConnection mConnection_1 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mBroadcastService = ((BroadcastService.LocalBinder) binder).getInstance();
            mBroadcastService.setHandler(mConnHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void init() {
        mContext = this.getContext();
        mActivity = this.getActivity();

        if (mActivity != null) {
            mAPP = (MainApplication) mActivity.getApplication();
        }

        mDeviceListAdapter = new BluetoothDeviceAdapter(this.mContext);
        mAutoTempAdapter = new AutoTempAdapter(this.mContext, mAPP);
        mTempRecord = new ArrayMap<>();

        mRecordListFragment = new RecordListFragment();

        mSlopeTemperature = new SlopeTemperature();

        // 檢查裝置是否支援藍牙
        bluetoothSupport();
        // 請求權限
        grantedPermission();
        // 啟動Service
        onStartService();

        initDatabase();
    }

    private void initDatabase() {
        mRecordDao = ((MainApplication) mActivity.getApplication()).recordDao();
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        // BluetoothService
        Intent bluetoothServiceIntent = new Intent(mActivity.getBaseContext(), BluetoothService.class);
        bluetoothServiceIntent.putExtra("TAG", mAPP.TAG());
        bluetoothServiceIntent.putExtra("UUID", mAPP.BluetoothUUID());
        mActivity.startService(bluetoothServiceIntent);
        mActivity.bindService(new Intent(mActivity, BluetoothService.class), mConnection, Context.BIND_AUTO_CREATE);

        // BroadcastService
        Intent broadcastServiceIntent = new Intent(mActivity.getBaseContext(), BroadcastService.class);
        mActivity.startService(broadcastServiceIntent);
        mActivity.bindService(new Intent(mActivity, BroadcastService.class), mConnection_1, Context.BIND_AUTO_CREATE);
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
        mLineChart = mView.findViewById(R.id.lineChart);
    }

    private void initToolbar() {
        mToolBar = mView.findViewById(R.id.toolbar);
        ((MainActivity) mActivity).setSupportActionBar(mToolBar);
        setHasOptionsMenu(true);

        mDrawerLayout = mView.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                mActivity,
                mDrawerLayout,
                mToolBar,
                R.string.navigation_drawer_close,
                R.string.navigation_drawer_open
        );

        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    public void initNavigationView() {
        NavigationView navigationView = mView.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Menu menu = navigationView.getMenu();
        Menu submenu = menu.addSubMenu(getString(R.string.appInfo));

        submenu.add(getString(R.string.versionNameLabel) + mAPP.VersionName()).setCheckable(false);
        navigationView.invalidate();

        // 切換手/自動模式
        SwitchCompat modelDrawerSwitch = (SwitchCompat) navigationView.getMenu().findItem(R.id.nav_model_sel).getActionView();
        modelDrawerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (mBluetoothService.getState() == Const.BLUETOOTH_SERVICE_STATE_CONNECTED) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("action", "model");
                    if (isChecked) {
                        mModel = "auto";
                        map.put("auto", true);
                        setAutoModeTempAlertView();
                    } else {
                        mModel = "manual";
                        map.put("auto", false);
                    }
                    bluetoothWrite(new JSONObject(map));
                } else if (mBluetoothService.getBluetoothDevice() == null) {
                    Log.e(mAPP.TAG(), "MainFragment::modelDrawerSwitch::onCheckedChanged()" + getString(R.string.no_device_connection));
                    alert(getString(R.string.no_device_connection));
                } else if (!getActionStart()) {
                    Log.e(mAPP.TAG(), "MainFragment::modelDrawerSwitch::onCheckedChanged()" + getString(R.string.no_action_start));
                    alert(getString(R.string.no_action_start));
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (!Singleton.getInstance().getBLEAdapter().isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, mAPP.);
//        }

        registerBroadcastReceiver();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mMainViewModel = new MainViewModel(this, mAPP);

        mBinding.setMainViewModel(mMainViewModel);

        setLineChart();
    }

    private void setLineChart() {
        String[] names = new String[]{
                mActivity.getString(R.string.temp_beans),
                mActivity.getString(R.string.temp_stove)
        };

        mChart = new MPChart(mLineChart, "", names);
        mChart.init();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.toolbar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        if (menuItem.getItemId() == R.id.action_conn) {
            if (!mBluetoothService.isSupport()) {
                alert(getString(R.string.no_support_bluetooth));
                return false;
            } else if (!mBluetoothService.isEnable()) {
                alert(getString(R.string.no_enable_bluetooth));
                return false;
            }
            getPairedDevices();
            View view = View.inflate(getContext(), R.layout.fragment_device_list, null);
            ListView mDeviceListView = view.findViewById(R.id.device_ListView);
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
        }

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_On:
                if (mBluetoothService.getState() == 2) {
                    // TODO
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("action", "status");
                            map.put("start", true);
                            bluetoothWrite(new JSONObject(map));

                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mStartTime = (int) System.currentTimeMillis() / 1000;
                                    setActionStart(true);
                                }
                            });
                        }
                    });
                    t.start();
                } else if (mBluetoothService.getBluetoothDevice() == null) {
                    alert(getString(R.string.no_device_connection));
                } else if (!getActionStart()) {
                    alert(getString(R.string.no_action_start));
                }
                break;
            case R.id.nav_record:
                if (getActivity() != null) {
                    FragmentManager fm = (getActivity()).getSupportFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.addToBackStack(mRecordListFragment.getClass().getName());
                    transaction.replace(R.id.container, mRecordListFragment);
                    transaction.commit();
                }

                break;
            case R.id.nav_Clear:
                mIsFirstCrack = false;
                mIsSecondCrack = false;
                mChart.refresh();
                mMainViewModel.refresh();
                break;
            case R.id.nav_stopConnect:
                if (getActionStart()) {
                    alert(getString(R.string.need_action_stop));
                } else {
                    mToolBar.setTitle(getString(R.string.app_name));
                    mChart.description(getString(R.string.device_connect_wait));
                    stopBluetoothConnect();
                    alert(getString(R.string.ble_device_stop_connecting));
                }
                break;
            case R.id.nav_save:
                mDeviceConnectionDialog.setText(getString(R.string.saving));
                mDeviceConnectionDialog.show();
                insertRecord();
                break;
            case R.id.nav_exit:
                exitAPP();
                break;
            default:
                return false;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initTimeInfo() {
        mFirstCrackTime = 0;
        mSecondCrackTime = 0;
        mRunTime = 0;
    }

    private void insertRecord() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Gson gson = new Gson();
                String json = gson.toJson(mTempRecord);
                Date date = new Date();
                RecordEntity entity = new RecordEntity();
                entity.name = Convert.TimestampFormat(date.getTime(), "yyyyMMddHHmmss") + "_record";
                entity.runTime = mRunTime;
                entity.record = json;
                entity.create_at = date.getTime();
                entity.inBeanIndex = mInBean;
                entity.firstCrackIndex = mFirstCrack;
                entity.secondCrackIndex = mSecondCrack;

                mRecordDao.insert(entity);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            Log.e(getTag(), "InternalError error : " + e.getMessage());
                        }

                        mDeviceConnectionDialog.dismiss();
                    }
                });
            }
        }).start();
    }

    @Override
    public void updateTargetTemp(final String temp) {
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
//        if (System.currentTimeMillis() / 1000 - mStartTime != 0) {
//            mFirstCrackStartTime = (int) System.currentTimeMillis() / 1000;
        mFirstCrack = mTempRecord.size();
//        }
        mFirstCrackStartTime = (int) System.currentTimeMillis() / 1000;
        mIsFirstCrack = true;
        mChart.addXAxisLimitLine(getString(R.string.first_crack));
    }

    @Override
    public void secondCrack() {
        mMainViewModel.setIsSecondCrack(true);
        if (System.currentTimeMillis() / 1000 - mStartTime != 0) {
            mSecondCrackStartTime = (int) System.currentTimeMillis() / 1000;
            mSecondCrack = mTempRecord.size();
        }
        mSecondCrackTime = 0;
        mIsSecondCrack = true;
        mChart.addXAxisLimitLine(getString(R.string.second_crack));
    }

    @Override
    public void actionBean(final boolean action) {
        if (mBluetoothService.getState() == 2 && getActionStart()) {
            mMainViewModel.setIsImport(action);

            mIsInBean = action;
            mStartTime = (int) System.currentTimeMillis() / 1000;

            if (action) {
                inBeansAction();
            } else {
                outBeansAction();
                initTimeInfo();
            }

            if (!action) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("action", "status");
                map.put("start", false);
                bluetoothWrite(new JSONObject(map));
                mBroadcastService.stop();
            }

            String msg = getString(R.string.exit_beans);
            if (action) {
                mChart.refresh();
                mMainViewModel.setIsFirstCrack(false);
                mMainViewModel.setIsSecondCrack(false);

                msg = getString(R.string.enter_beans);
                mChart.addEntry(Float.parseFloat(mBeanTemp), Float.parseFloat(mStoveTemp), 0);
                mInBean = mTempRecord.size();
                mChart.addXAxisLimitLine(getString(R.string.enter_beans));

                if (mModel.equals("auto")) {
                    mBroadcastService.setTimeRange(AUTO_MODEL_TIME_RANGE);
                    mBroadcastService.setTimeSec(mAutoModeRunTimeSec);
                    mBroadcastService.setTempMap(mAutoTempJobs);
                    mBroadcastService.startCountDownTimer();
                }

            } else {
                setActionStart(false);
                mMainViewModel.setIsFirstCrack(false);
                mMainViewModel.setIsSecondCrack(false);
                mFirstCrack = 0;
                mSecondCrack = 0;
                mInBean = 0;
            }
            alert(msg);
        } else if (mBluetoothService.getBluetoothDevice() == null) {
            Log.e(mAPP.TAG(), "MainFragment::actionBean(), " + getString(R.string.no_device_connection));
            alert(getString(R.string.no_device_connection));
        } else if (!this.getActionStart()) {
            Log.e(mAPP.TAG(), "MainFragment::actionBean(), " + getString(R.string.no_action_start));
            alert(getString(R.string.no_action_start));
        }
    }

    public void inBeansAction() {

    }

    public void outBeansAction() {

    }

    public void setActionStart(boolean status) {
        this.mActionStart = status;
    }

    public boolean getActionStart() {
        return this.mActionStart;
    }

    private void updateTemp(String data) {
        try {
            JSONObject jsonObject = new JSONObject(data);

            HashMap<String, Object> map = Convert.toMap(jsonObject);
            mMainViewModel.updateTemp(map);
            mBeanTemp = String.valueOf(map.get("b"));
            mStoveTemp = String.valueOf(map.get("s"));

            if ((System.currentTimeMillis() / 1000 - mStartTime) != 0) {
                mRunTime = (int) System.currentTimeMillis() / 1000 - mStartTime;
            }

            if ((System.currentTimeMillis() / 1000 - mSecondCrackStartTime) != 0) {
                mSecondCrackTime = (int) System.currentTimeMillis() / 1000 - mSecondCrackStartTime;
            }

            if ((System.currentTimeMillis() / 1000) - mFirstCrackStartTime != 0) {
                mFirstCrackTime = (int) System.currentTimeMillis() / 1000 - mFirstCrackStartTime;
            }

            if (this.getActionStart()) {
                mChart.addEntry(
                        Float.parseFloat(mBeanTemp),
                        Float.parseFloat(mStoveTemp),
                        mRunTime
                );

                if (mIsInBean) {
                    mTempRecord.put(mRunTime, jsonObject);
                    mMainViewModel.setRunTime(mRunTime);
                    if (mIsFirstCrack) {
                        mMainViewModel.setFirstCrackTime(mFirstCrackTime);
                    }
                    if (mIsSecondCrack) {
                        mMainViewModel.setSecondCrackTime(mSecondCrackTime);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(mAPP.TAG(), "MainFragment::updateTemp(), error :  " + e.getMessage());
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //收到bluetooth狀態改變
            if (intent.getAction() != null) {
                if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {

                        mDeviceListAdapter.addItem(BluetoothDeviceAdapter.NoPAIRED_ITEM_TYPE, device);
                        mDeviceListAdapter.notifyDataSetChanged();
                    }
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
    private void getPairedDevices() {
        mDeviceListAdapter.setData(BluetoothDeviceAdapter.PAIRED_ITEM_TYPE, mBluetoothService.pairedDevices());
    }

    /**
     * Bluetooth device of ListView's item ClickListener
     */
    private ListView.OnItemClickListener listener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
            mAlertDialog.dismiss();
            mDeviceConnectionDialog.show();

            BluetoothDevice device = mDeviceListAdapter.getDevice(position);
            mBluetoothService.setBluetoothDevice(device);
            mIsBound = true;

            mDeviceConnectionDialog.dismiss();
            mToolBar.setTitle(mContext.getString(R.string.app_name) + " " + device.getName() + " 連線中...");
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
        View view = View.inflate(getContext(), R.layout.temp_list, null);
        ListView mTempListView = view.findViewById(R.id.temp_ListView);
        mTempListView.setAdapter(mAutoTempAdapter);

        ArrayList<Temperature> temperatures = new ArrayList<>();

        temperatures.add(new Temperature(0, 0)); // 自動啟始時間&溫度
        for (int i = 1; i <= 5; i++) {
            int timeUnit = 15 * 60 / 5;
            temperatures.add(new Temperature(100, timeUnit * i));
        }
        mAutoTempAdapter.setData(temperatures);

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setIcon(R.drawable.ic_temp);
        builder.setTitle(R.string.settingTemp);
        builder.setCancelable(false);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    ArrayList<Temperature> mTemperatureList = mAutoTempAdapter.getData();
                    mAutoTempJobs = new ArrayMap<>();
                    int firstSec = 0;
                    int firstTemp = (int) mTemperatureList.get(0).getTemp();
                    int addSec = 0;
                    for (int i = 1; i < mTemperatureList.size(); i++) {
                        Temperature t = mTemperatureList.get(i);

                        int timeDiff = t.getSeconds() - firstSec;
                        int tempDiff = (int) t.getTemp() - firstTemp;
                        float slope = mSlopeTemperature.slope(timeDiff, tempDiff);

                        HashMap<Integer, Integer> timeMap = mSlopeTemperature.allTemperature(
                                slope,
                                firstSec,
                                t.getSeconds(),
                                AUTO_MODEL_TIME_RANGE,
                                firstTemp,
                                (int) t.getTemp()
                        );
                        for (Map.Entry<Integer, Integer> entry : timeMap.entrySet()) {
                            int key = entry.getKey();
                            Integer value = entry.getValue();
                            mAutoTempJobs.put(key, value);
                        }

                        firstSec = t.getSeconds();
                        firstTemp = (int) t.getTemp();
                        addSec++;
                        mAutoTempJobs.put(firstSec, firstTemp);
                    }
                    mAutoModeRunTimeSec = firstSec + addSec * AUTO_MODEL_TIME_RANGE;
                } catch (Exception e) {
                    Log.e(mAPP.TAG(), "MainFragment::setAutoModeTempAlertView(), Exception ", e);
                }
                dialog.cancel();
            }
        });
        builder.setView(view);
        mAlertDialog = builder.create();
        mAlertDialog.show();
        if (mAlertDialog.getWindow() != null) {
            mAlertDialog.getWindow().setLayout(mScreenWidth / 10 * 9, mScreenHeight / 10 * 9);
            mAlertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }
    }

    /**
     * 透過藍芽傳送資料
     *
     * @param jsonObject 欲傳送的資料內容
     */
    private void bluetoothWrite(final JSONObject jsonObject) {
        Log.e("test", "test:" + jsonObject.toString());
        mBluetoothService.write(jsonObject.toString().getBytes());
    }

    /**
     * Alert Message
     *
     * @param msg 訊息文字內容
     */
    public void alert(String msg) {
        try {
            Snackbar.make(mView, msg, Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(mAPP.TAG(), "MainFragment::alert(), Exception ", e);
        }

    }

    private static class MainHandler extends Handler {
        private MainFragment mFragment;

        private MainHandler(MainFragment fragment) {
            WeakReference<MainFragment> weakReference = new WeakReference<>(fragment);
            mFragment = weakReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case Const.BLUETOOTH_SERVICE_STATE_NONE:
                case Const.BLUETOOTH_SERVICE_STATE_CONNECTING:
                case Const.BLUETOOTH_SERVICE_STATE_CONNECTED:
                    mFragment.alert((String) msg.obj);
                    break;
                case Const.BLUETOOTH_SERVICE_WHAT_READ:
                    mFragment.updateTemp((String) msg.obj);
                    break;
                case Const.BLUETOOTH_SERVICE_WHAT_WRITE:
                    break;
                case Const.BROADCAST_SERVICE_Time_RUINNER_END:
                    // TODO
                    break;
                case Const.BROADCAST_SERVICE_Time_RUINNER_SET_DATA:
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("action", "temp");
                    map.put("target", msg.obj);
                    mFragment.bluetoothWrite(new JSONObject(map));
                    break;
            }
        }
    }
}
