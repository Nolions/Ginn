package tw.nolions.coffeebeanslife.fragment;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
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

import java.util.UUID;

import tw.nolions.coffeebeanslife.R;
import tw.nolions.coffeebeanslife.service.BluetoothAcceptService;
import tw.nolions.coffeebeanslife.service.BluetoothSingleton;
import tw.nolions.coffeebeanslife.viewmodel.MainViewModel;
import tw.nolions.coffeebeanslife.widget.MPChart;

public class MainFragment extends Fragment implements Toolbar.OnCreateContextMenuListener{
    private String TAG;
    private LineChart mLineChart;

    private MainViewModel mMainViewModel;
    private FragmentMainBinding mBinding;

    private BluetoothAcceptService mBluetoothAcceptService;
    private MPChart mChart;

    public Handler mHandler;

    @NonNull
    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = getResources().getString(R.string.app_name);

        if (BluetoothSingleton.getInstance().getBluetoothAdapter() == null) {
            Toast.makeText(getContext(), getResources().getString(R.string.noSupportBluetooth), Toast.LENGTH_LONG).show();
        }

        if (mBluetoothAcceptService == null) {
            mBluetoothAcceptService = new BluetoothAcceptService(BluetoothSingleton.getInstance().getBluetoothAdapter(), mHandler);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);

        View v = mBinding.getRoot();

        mLineChart = (LineChart) v.findViewById(R.id.lineChart);
        Toolbar toobBar = (Toolbar) v.findViewById(R.id.toolbar);
        ((MainActivity) getActivity()).setSupportActionBar(toobBar);
        setHasOptionsMenu(true);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        Handler handler = new Handler(){
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

        if (!BluetoothSingleton.getInstance().getBluetoothAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BluetoothAcceptService.REQUEST_ENABLE_BT);
        }


        UUID deviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

            mBluetoothAcceptService.conn("Samsung Galaxy S7 edge", deviceUUID);
            mBluetoothAcceptService.start();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mMainViewModel = new MainViewModel(getActivity());
        mBinding.setMainViewModel(mMainViewModel);

        String[] names = new String[]{
                "testData"
        };

        String description = "No chart data available. Use the menu to add entries and data sets!";
        mChart = new MPChart(mLineChart, description, names);
        mChart.init();

        mChart.addEntry(0, 7);
        mChart.addEntry(0, 2);
        mChart.addEntry(0, 22);
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
//                                        moveTaskToBack(true);
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

        mBluetoothAcceptService.cancel();
        return false;
    }

    private void switchFragment(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(fragment.getClass().getName());
        transaction.commit();
    }
}

