package tw.nolions.coffeebeanslife.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import tw.nolions.coffeebeanslife.viewModel.mainViewModel;
//import tw.nolions.coffeebeanslife.databinding.Fragment.

import com.github.mikephil.charting.charts.LineChart;

import tw.nolions.coffeebeanslife.R;

public class MainFragment extends Fragment {

    private TextView mTempBeans, mTempStove, mTempEnvironment;
    private LineChart mChart;

//    private mainViewModel mViewModel;
//    private Main binding ;

    //private MainFragmentBinding binding ;
    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
//
//        View view = mBinding.getRoot();

        View v = inflater.inflate(R.layout.fragment_main, container, false);

//        mTempBeans = (TextView) v.findViewById(R.id.temp_beans);
//        mTempStove = (TextView) v.findViewById(R.id.temp_stove);
//        mTempEnvironment = (TextView) v.findViewById(R.id.temp_environment);
//        mChart = (LineChart) v.findViewById(R.id.chart);

        return v;
    }
}
