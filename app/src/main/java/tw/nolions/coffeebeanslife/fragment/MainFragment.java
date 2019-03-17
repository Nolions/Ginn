package tw.nolions.coffeebeanslife.fragment;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import androidx.annotation.Nullable;
import tw.nolions.coffeebeanslife.MainActivity;
import tw.nolions.coffeebeanslife.databinding.FragmentMainBinding;

import com.github.mikephil.charting.charts.LineChart;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import tw.nolions.coffeebeanslife.R;
import tw.nolions.coffeebeanslife.service.BluetoothAcceptService;
import tw.nolions.coffeebeanslife.service.BluetoothSingleton;
import tw.nolions.coffeebeanslife.viewmodel.MainViewModel;
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

        TAG = getResources().getString(R.string.app_name);

        if (BluetoothSingleton.getInstance().getAdapter() == null) {
            Toast.makeText(getContext(), getResources().getString(R.string.noSupportBluetooth), Toast.LENGTH_LONG).show();
        }


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

        if (this.checkBluetoothConn()) {
            mToobBar.setTitle(getContext().getString(R.string.app_name) +  " " + mBluetootgDevice.getName() + " 連線中...");
            mChart.description("裝置連線，等待資料中...");

            try {
                mInputStream = BluetoothSingleton.getInstance().getSocket().getInputStream();
                this.read();
            } catch (IOException IOE) {
                Log.e(TAG, "Error : " + IOE.getMessage());
            }



        } else {
            mChart.description("沒有連線裝置...");
        }
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
                Fragment mToFragment = new DeviceFragment();
                this.switchFragment(mToFragment);
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
}

