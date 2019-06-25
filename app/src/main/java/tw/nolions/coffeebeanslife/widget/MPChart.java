package tw.nolions.coffeebeanslife.widget;

import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
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

import tools.Convert;


public class MPChart implements OnChartValueSelectedListener {

    private int[] mColors = new int[]{
            Color.parseColor("#eb73f6"),   //紫色
            Color.parseColor("#000000")    //黑色
    };

    private XAxis xAxis;
    private LineChart mLineChart;
    private String[] mDataSetNames;
    private String mDescribe;

    private ArrayList<Integer> mXAixData = new ArrayList<>();

    public MPChart(LineChart lineChart, String describe, String[] names) {
        mLineChart = lineChart;
        mDataSetNames = names;
        mDescribe = describe;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
    }

    @Override
    public void onNothingSelected() {
    }

    /**
     * init
     */
    public void init() {
        mLineChart.setOnChartValueSelectedListener(this);

        description(mDescribe);
        border();
        touchGestures();
        xAxis();
        yAxis();
        refresh();
        mXAixData.add(0);
    }


    public void description(String describe) {
        mLineChart.setNoDataText(describe);

        Description description = new Description();
        description.setTextColor(ColorTemplate.VORDIPLOM_COLORS[2]);
        description.setText(describe);
        mLineChart.setDescription(description); // 圖表說明文字
    }

    /**
     * setting Draw Grid of Background
     */
    public void background() {
        this.mLineChart.setDrawGridBackground(false);
    }

    /**
     * border setting
     */
    public void border() {
        mLineChart.setDrawBorders(true);
        mLineChart.setBorderWidth(0.1f);
    }

    /**
     * touch gestures setting
     */
    public void touchGestures() {
        // enable touch gestures
        mLineChart.setTouchEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mLineChart.setPinchZoom(false);

        // enable scaling and dragging
        mLineChart.setDragEnabled(true);
        mLineChart.setScaleEnabled(true);
    }

    /**
     * xAxis setting
     */
    public void xAxis() {
        xAxis = this.mLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setAxisMinimum(0);

        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);

        xAxis.setDrawAxisLine(false);
        xAxis.setDrawLabels(true);

        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (mXAixData.size() > value) {
                    int sec = mXAixData.get((int) value);
                    return Convert.SecondConversion(sec);
                }
                return "0";
            }
        });
    }

    /**
     * yAxis setting
     */
    public void yAxis() {
        mLineChart.getAxisRight().setEnabled(false);
    }

    /**
     * init LineData's Set
     *
     * @param name
     * @param entries
     * @return LineDataSet
     */
    private LineDataSet initLineDataSet(String name, ArrayList<Entry> entries, int color) {
        //        int color = mColors[count % mColors.length];
        LineDataSet set = new LineDataSet(entries, name);
//        set.setLineWidth(2.5f);
//        set.setCircleRadius(4.5f);
//        set.setAxisDependency(YAxis.AxisDependency.LEFT);
//        set.setValueTextSize(10f);

        set.setColor(color);
        set.setCircleColor(color);
        set.setHighLightColor(color);
        set.setValueTextSize(10f);
//        set.setDrawValues(false);
        set.setValueTextColor(color);
        set.enableDashedHighlightLine(10f, 5f, 0f);
        set.setDrawFilled(true);
        set.setFillColor(color);

//          set.setDrawVerticalHighlightIndicator(false);
        set.setDrawHorizontalHighlightIndicator(false);

        return set;
    }

    public void refresh() {
        Log.e("test", "trtest");


        xAxis.removeAllLimitLines();
        mXAixData = new ArrayList<>();
        mLineChart.clear();
        mLineChart.invalidate();
        mLineChart.fitScreen();
        mLineChart.notifyDataSetChanged();
    }

    public void setChange() {
        mLineChart.notifyDataSetChanged();
    }

    /**
     * Add line chart's entry
     *
     * @param lineIndex
     * @param value
     * @param sec
     */
    public void addEntry(int lineIndex, float value, int sec) {
        LineData lineData = this.mLineChart.getLineData();

        if (lineData == null) {
            lineData = new LineData();
            this.mLineChart.setData(lineData);
        }

        setLineDataSet(lineData, lineIndex, value, mColors[0], sec);

        mXAixData.add(sec);
        lineData.notifyDataChanged();
        setChange();
        mLineChart.setVisibleXRangeMaximum(lineData.getEntryCount());
        mLineChart.moveViewTo(lineData.getEntryCount(), 50f, YAxis.AxisDependency.LEFT);
    }

    public void addEntry(float value1, float value2, int sec) {
        LineData lineData = this.mLineChart.getLineData();

        if (lineData == null) {
            lineData = new LineData();
            this.mLineChart.setData(lineData);
        }

        setLineDataSet(lineData, 0, value1, mColors[0], sec);
        setLineDataSet(lineData, 1, value2, mColors[1], sec);

        mXAixData.add(sec);
        lineData.notifyDataChanged();
        setChange();
        mLineChart.setVisibleXRangeMaximum(lineData.getEntryCount());
        mLineChart.moveViewTo(lineData.getEntryCount(), 50f, YAxis.AxisDependency.LEFT);
    }

    private void setLineDataSet(LineData lineData, int index, float value, int color, int sec) {
        ILineDataSet set = lineData.getDataSetByIndex(index);
        if (set == null) {
            ArrayList<Entry> entries = new ArrayList<>();
            entries.add(new Entry(0, value));
//            mXAixData.add(0);
            String name = (String) Array.get(mDataSetNames, index);
            set = initLineDataSet(name, entries, color);
            lineData.addDataSet(set);
        }


        lineData.addEntry(new Entry(set.getEntryCount(), value), index);
    }

    public void addXAxisLimitLine(String label) {
        LimitLine ll = new LimitLine(mLineChart.getLineData().getEntryCount() / 2, label);
        ll.setLineColor(Color.RED);
        ll.setLineWidth(2f);
        ll.setTextColor(Color.GRAY);
        ll.setTextSize(12f);

        xAxis.addLimitLine(ll);
        xAxis.setDrawLimitLinesBehindData(true);
    }

    public void saveToImage(String fileName) {
        mLineChart.saveToPath(fileName, "");
    }
}
