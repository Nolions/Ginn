package tw.nolions.coffeebeanslife.fragment;

//import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import tw.nolions.coffeebeanslife.databinding.FragmentMainBinding;

import com.github.mikephil.charting.charts.LineChart;

import tw.nolions.coffeebeanslife.R;
import tw.nolions.coffeebeanslife.viewmodel.MainViewModel;
import tw.nolions.coffeebeanslife.widget.MPChart;

public class MainFragment extends Fragment {

    private LineChart mLineChart;

    private MainViewModel mMainViewModel;
    private FragmentMainBinding mBinding;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);

        View v = mBinding.getRoot();

        mLineChart = (LineChart) v.findViewById(R.id.lineChart);


        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mMainViewModel = new MainViewModel(getActivity());
        mBinding.setMainViewModel(mMainViewModel);

        MPChart chart = new MPChart(mLineChart);
        chart.init();

        float value = (float) (Math.random() * 50) + 1;
        chart.addEntry("testData",0,0);
        chart.addEntry("testData",0, value);

    }
}
