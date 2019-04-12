package tw.nolions.coffeebeanslife.widget;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;


public class MPChart implements OnChartValueSelectedListener {

    private Calendar startCal;
    private Calendar nowCal;
    private int[] mColors = new int[]{
            Color.parseColor("#5abdfc"),    //蓝色
            Color.parseColor("#eb73f6")    //紫色
    };

    private LineChart mLineChart;
    private String[] mDataSetNames;
    private String mDescribe;

    private Long mStartTime;
    private ArrayList<Long> mXAixData = new ArrayList<>();

    public MPChart(LineChart lineChart, String describe, String[] names) {
        this.mLineChart = lineChart;
        this.mDataSetNames = names;
        this.mDescribe = describe;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    public void init() {

        this.mLineChart.setOnChartValueSelectedListener(this);

        this.description(this.mDescribe);
        this.border();
        this.touchGestures();
        this.xAxis();
        this.yAxis();


        this.refresh();
    }

    public void description(String describe) {
        this.mLineChart.setNoDataText(describe);

        Description description = new Description();
        description.setTextColor(ColorTemplate.VORDIPLOM_COLORS[2]); description.setText("Chart Data");
        this.mLineChart.setDescription(description);   //右下角说明文字
    }

    public void background() {
        this.mLineChart.setDrawGridBackground(false);
    }

    public void border() {
        this.mLineChart.setDrawBorders(true);    //四周是不是有边框
        this.mLineChart.setBorderWidth(0.1f);
    }

    /**
     * touch gestures setting
     */
    public void touchGestures() {
        // enable touch gestures
        this.mLineChart.setTouchEnabled(false);
        // if disabled, scaling can be done on x- and y-axis separately
        this.mLineChart.setPinchZoom(false);
        // enable scaling and dragging
        this.mLineChart.setDragEnabled(true);
        this.mLineChart.setScaleEnabled(true);
    }

    public void xAxis() {
        XAxis xAxis = this.mLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setAxisMinimum(0);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);

        // format XAxis Value
        String[] a = {"1","2"};

        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return "" + mXAixData.get((int) value);
            }
        });


        xAxis.setDrawAxisLine(false);
        xAxis.setDrawLabels(true);
//        xAxis.setLabelCount(mMonths.length); // setting length of XAxis
//        xAxis.setTextColor(Color.parseColor("#b3b3b3")); // setting color of XAxis
    }


    public void yAxis() {
        this.mLineChart.getAxisRight().setEnabled(false);
    }

    private LineDataSet initLineDataSet(String name, ArrayList<Entry> entries) {
        //        int color = mColors[count % mColors.length];
        LineDataSet set = new LineDataSet(entries, name);
//        set.setLineWidth(2.5f);
//        set.setCircleRadius(4.5f);
//        set.setAxisDependency(YAxis.AxisDependency.LEFT);
//        set.setValueTextSize(10f);

        set.setColor(Color.parseColor("#5abdfc"));
        set.setCircleColor(Color.parseColor("#5abdfc"));
        set.setHighLightColor(Color.parseColor("#5abdfc"));
        set.setValueTextSize(10f);
//        set.setDrawValues(false);
        set.setValueTextColor(Color.parseColor("#5abdfc"));
        set.enableDashedHighlightLine(10f, 5f, 0f);
        set.setDrawFilled(true);
        set.setFillColor(Color.parseColor("#5abdfc"));

//            set.setDrawVerticalHighlightIndicator(false);
        set.setDrawHorizontalHighlightIndicator(false);

        return set;
    }

    public void refresh() {
        this.mLineChart.invalidate();
    }

    public void setChange() {
        this.mLineChart.notifyDataSetChanged();
    }

    public void addEntry(int lineIndex, float value) {
        LineData data = this.mLineChart.getLineData();

        if (data == null) {
            data = new LineData();
            this.mLineChart.setData(data);
        }

        ILineDataSet set = data.getDataSetByIndex(lineIndex);
        if (set == null) {
            ArrayList<Entry> entries = new ArrayList<>();
            entries.add(new Entry(0, 0));

            mStartTime = System.currentTimeMillis()/1000;
            mXAixData.add(0L);

            String name =  (String) Array.get(mDataSetNames, lineIndex);
            set = this.initLineDataSet(name, entries);
            data.addDataSet(set);
        }
        Long sec = 1L;
        if (System.currentTimeMillis()/1000 - mStartTime != 0) {
            sec = System.currentTimeMillis()/1000 - mStartTime;
        }
        mXAixData.add(sec);


        // choose a random dataSet
        int randomDataSetIndex = (int) (Math.random() * data.getDataSetCount());
        ILineDataSet randomSet = data.getDataSetByIndex(randomDataSetIndex);

        data.addEntry(new Entry(randomSet.getEntryCount(), value), randomDataSetIndex);
        data.notifyDataChanged();

        // let the chart know it's data has changed
        this.setChange();

        this.mLineChart.setVisibleXRangeMaximum(6);
        this.mLineChart.moveViewTo(data.getEntryCount() - 7, 50f, YAxis.AxisDependency.LEFT);
    }
}
