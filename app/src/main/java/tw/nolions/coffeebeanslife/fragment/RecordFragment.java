package tw.nolions.coffeebeanslife.fragment;

import android.databinding.DataBindingUtil;
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

import com.github.mikephil.charting.charts.LineChart;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import androidx.annotation.Nullable;
import tw.nolions.coffeebeanslife.MainActivity;
import tw.nolions.coffeebeanslife.MainApplication;
import tw.nolions.coffeebeanslife.R;
import tw.nolions.coffeebeanslife.databinding.FragmentRecordBinding;
import tw.nolions.coffeebeanslife.model.entity.RecordEntity;
import tw.nolions.coffeebeanslife.model.recordDao;
import tw.nolions.coffeebeanslife.service.asyncTask.ExportToCSVAsyncTask;
import tw.nolions.coffeebeanslife.widget.MPChart;
//import FragmentRe


public class RecordFragment extends Fragment implements Toolbar.OnCreateContextMenuListener{

    private String[] mNames;
    private View mView;
    private FragmentRecordBinding mBind;
    private int mRecordID;
    private MPChart mChart;

    RecordEntity mRecord;

    private recordDao mRecordDao;

    public RecordFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public void onStart() {
        super.onStart();

        Bundle arguments = getArguments();
        mRecordID = arguments.getInt("recordID");

        mNames = new String[]{
                getString(R.string.temp_stove),
                getString(R.string.temp_beans)
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBind = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_record,
                container,
                false);
        mView = mBind.getRoot();

        initView();

        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainApplication app = (MainApplication) getActivity().getApplication();
        mRecordDao = app.appDatabase().getRecordDao();

        initMPChart();
        setData();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.record_toolbar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.export_item:
                Gson gson = new Gson();
                Type type = new TypeToken<HashMap<Integer, JSONObject>>() {
                }.getType();
                HashMap<Integer, JSONObject> mTempRecord = gson.fromJson(mRecord.record, type);

                MainApplication mAPP = (MainApplication) getActivity().getApplication();

                mChart.saveToImage(mRecord.name);
                new ExportToCSVAsyncTask(getContext(), mAPP, mRecord.name).execute(mTempRecord);
                break;
        }
        return true;
    }

    private void init() {

    }

    private void initView() {
        Toolbar toolbar = (Toolbar) mView.findViewById(R.id.record_toolbar);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        setHasOptionsMenu(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
    }

    private void setData() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                mRecord = mRecordDao.find(mRecordID);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setChart(mRecord);
                    }
                });
            }
        }).start();
    }

    private void initMPChart() {
        String[] names = new String[]{
                getString(R.string.temp_stove),
                getString(R.string.temp_beans)
        };

        LineChart lineChart = mView.findViewById(R.id.record_lineChart);
        mChart = new MPChart(lineChart, "", names);
        mChart.init();


    }

    private void setChart(RecordEntity record) {
        Gson gson = new Gson();

        Type type = new TypeToken<HashMap<Integer, JSONObject>>() {
        }.getType();
        HashMap<Integer, JSONObject> mTempRecord = gson.fromJson(record.record, type);

        Map<Integer, JSONObject> param = new TreeMap<>(mTempRecord);
        int i = 0;
        for (Integer key : param.keySet()) {

            JSONObject jsonObject = param.get(key);

            if (i == record.inBeanIndex) {
                mChart.addXAxisLimitLine(getString(R.string.enter_beans));
            } else if (i == record.firstCrackIndex) {
                mChart.addXAxisLimitLine(getString(R.string.first_crack));
            } else if (i == record.secondCrackIndex) {
                mChart.addXAxisLimitLine(getString(R.string.second_crack));
            }

            try {

                mChart.addEntry(
                        Float.parseFloat(jsonObject.getString("b")),
                        Float.parseFloat(jsonObject.getString("s")),
                        key
                );
            } catch (JSONException e) {
                Log.e(getTag(), "SONException error: " + e.getMessage());
            }
            i++;
        }
    }
}
