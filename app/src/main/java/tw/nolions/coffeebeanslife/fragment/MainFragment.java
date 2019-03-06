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
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;

import tw.nolions.coffeebeanslife.R;
import tw.nolions.coffeebeanslife.viewmodel.MainViewModel;

public class MainFragment extends Fragment implements OnChartValueSelectedListener {

    private LineChart mLineChart;

    private MainViewModel mMainViewModel;
    private FragmentMainBinding mBinding;


    private int[] mColors = new int[]{
            Color.parseColor("#5abdfc"),    //蓝色
            Color.parseColor("#eb73f6")    //紫色
    };

    protected String[] mMonths = new String[]{
            "1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"
    };

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
//        View v = inflater.inflate(R.layout.fragment_main, container, false);

        mLineChart = (LineChart) v.findViewById(R.id.lineChart);


        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mMainViewModel = new MainViewModel(getActivity());
        mBinding.setMainViewModel(mMainViewModel);

        this.setLineChart();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    private void setLineChart() {
        mLineChart.setOnChartValueSelectedListener(this);
        mLineChart.setDrawGridBackground(false);
        mLineChart.getDescription().setEnabled(false);
        mLineChart.setNoDataText("No chart data available. Use the menu to add entries and data sets!");

        mLineChart.setDrawGridBackground(false);
        mLineChart.setDescription(null);    //右下角说明文字
        mLineChart.setDrawBorders(true);    //四周是不是有边框
        mLineChart.setBorderWidth(0.1f);
        mLineChart.getAxisRight().setEnabled(false);
        mLineChart.invalidate();

        this.setChartTouchGestures();

        this.setXAxis();

        this.addChartEntry();
    }

    private LineDataSet createSet() {

//        int color = mColors[count % mColors.length];
        LineDataSet set = new LineDataSet(null, "DataSet 1");
//        set.setLineWidth(2.5f);
//        set.setCircleRadius(4.5f);
//        set.setColor(Color.rgb(240, 99, 99));
//        set.setCircleColor(Color.rgb(240, 99, 99));
//        set.setHighLightColor(Color.rgb(190, 190, 190));
//        set.setAxisDependency(YAxis.AxisDependency.LEFT);
//        set.setValueTextSize(10f);

        set.setColor(Color.parseColor("#5abdfc"));
        set.setCircleColor(Color.parseColor("#5abdfc"));
        set.setHighLightColor(Color.parseColor("#5abdfc"));
        set.setValueTextSize(10f);
//        set.setDrawValues(false);    //节点不显示具体数值
        set.setValueTextColor(Color.parseColor("#5abdfc"));
        set.enableDashedHighlightLine(10f, 5f, 0f);    //选中某个点的时候高亮显示只是线
        set.setDrawFilled(true);     //填充折线图折线和坐标轴之间
        set.setFillColor(Color.parseColor("#5abdfc"));

//            set.setDrawVerticalHighlightIndicator(false);//取消纵向辅助线
        set.setDrawHorizontalHighlightIndicator(false);

        return set;
    }

    /**
     * enable touch gestures
     *
     */
    private void setChartTouchGestures(){
        // enable touch gestures
        mLineChart.setTouchEnabled(false);
        // if disabled, scaling can be done on x- and y-axis separately
        //禁止x轴y轴同时进行缩放
        mLineChart.setPinchZoom(false);
        // enable scaling and dragging
        mLineChart.setDragEnabled(true);
        mLineChart.setScaleEnabled(true);
    }

    private void setXAxis() {
        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);    //x轴是在上边显示还是显示在下边
        xAxis.enableGridDashedLine(10f, 10f, 0f);    //背景用虚线表格来绘制  给整成虚线
        xAxis.setAxisMinimum(0);//设置轴的最小值。这样设置将不会根据提供的数据自动计算。
        xAxis.setGranularityEnabled(true);    //粒度
        xAxis.setGranularity(1f);    //缩放的时候有用，比如放大的时候，我不想把横轴的月份再细分

//        xAxis.setValueFormatter(new IAxisValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                return mMonths[(int) value % mMonths.length];
//            }
//        });


        xAxis.setDrawAxisLine(false);    //是否显示坐标轴那条轴
        xAxis.setDrawLabels(true);    //是不是显示轴上的刻度
//        xAxis.setLabelCount(mMonths.length);    //强制有多少个刻度
//        xAxis.setTextColor(Color.parseColor("#b3b3b3"));
    }

    public void addChartEntry() {

        LineData data = mLineChart.getLineData();

        if (data == null) {
            data = new LineData();
            mLineChart.setData(data);
        }

        ILineDataSet set = data.getDataSetByIndex(0);
        if (set == null) {
            set = createSet();
            data.addDataSet(set);
        }

        // choose a random dataSet
        int randomDataSetIndex = (int) (Math.random() * data.getDataSetCount());
        ILineDataSet randomSet = data.getDataSetByIndex(randomDataSetIndex);
        float value = (float) (Math.random() * 50) + 50f * (randomDataSetIndex + 1);

        data.addEntry(new Entry(randomSet.getEntryCount(), value), randomDataSetIndex);
        data.notifyDataChanged();

        // let the chart know it's data has changed
        mLineChart.notifyDataSetChanged();
//
        mLineChart.setVisibleXRangeMaximum(6);
        mLineChart.moveViewTo(data.getEntryCount() - 7, 50f, YAxis.AxisDependency.LEFT);
    }
}
