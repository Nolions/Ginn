package tw.nolions.coffeebeanslife.fragment;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
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

import androidx.annotation.Nullable;
import tw.nolions.coffeebeanslife.MainActivity;
import tw.nolions.coffeebeanslife.databinding.FragmentMainBinding;

import com.github.mikephil.charting.charts.LineChart;

import tw.nolions.coffeebeanslife.R;
import tw.nolions.coffeebeanslife.viewmodel.MainViewModel;
import tw.nolions.coffeebeanslife.widget.MPChart;

public class MainFragment extends Fragment implements Toolbar.OnCreateContextMenuListener{
    private String TAG;
    private LineChart mLineChart;

    private MainViewModel mMainViewModel;
    private FragmentMainBinding mBinding;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = getResources().getString(R.string.app_name);
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mMainViewModel = new MainViewModel(getActivity());
        mBinding.setMainViewModel(mMainViewModel);

        String[] names = new String[]{
                "testData"
        };

        String description = "No chart data available. Use the menu to add entries and data sets!";
        MPChart chart = new MPChart(mLineChart, description, names);
        chart.init();

        float value = 7;
        chart.addEntry(0, value);
        chart.addEntry(0, 2);
        chart.addEntry(0, 22);
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
        return false;
    }
}

