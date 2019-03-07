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

public class MPChart implements OnChartValueSelectedListener {

    private int[] mColors = new int[]{
            Color.parseColor("#5abdfc"),    //蓝色
            Color.parseColor("#eb73f6")    //紫色
    };

    private LineChart mLineChart;

    public MPChart(LineChart lineChart) {
        this.mLineChart = lineChart;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    public void init() {

        this.mLineChart.setOnChartValueSelectedListener(this);

        this.description();
        this.border();
        this.touchGestures();
        this.xAxis();
        this.yAxis();


        this.refresh();


    }

    public void description() {
        this.mLineChart.setNoDataText("No chart data available. Use the menu to add entries and data sets!");

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
        xAxis.setGranularityEnabled(true);    //粒度
        xAxis.setGranularity(1f);    //缩放的时候有用，比如放大的时候，我不想把横轴的月份再细分

        // TODO
        // format XAxis Value
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.valueOf(value);
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

    private LineDataSet initLineDataSet(String name) {
        //        int color = mColors[count % mColors.length];
        LineDataSet set = new LineDataSet(null, "name");
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

    public void addEntry(String name, int lineIndex, float value) {
        LineData data = this.mLineChart.getLineData();

        if (data == null) {
            data = new LineData();
            this.mLineChart.setData(data);
        }

        ILineDataSet set = data.getDataSetByIndex(lineIndex);
        if (set == null) {
            set = this.initLineDataSet(name);
            data.addDataSet(set);
        }

        // choose a random dataSet
        // TODO
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
