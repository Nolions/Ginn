package tw.nolions.coffeebeanslife.widget;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import tools.Convert;
import tw.nolions.coffeebeanslife.R;
import tw.nolions.coffeebeanslife.model.Temperature;

public class AutoTempAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private ArrayList<Temperature> mTemperatureList;

    private Context mContext;
    final private int TYPE_Count = 2;
    final public static int PAIRED_ITEM_TYPE = 0, NoPAIRED_ITEM_TYPE =1;

    public AutoTempAdapter(Context c) {
        this.mContext = c;
        this.mInflater = LayoutInflater.from(c);
        this.mTemperatureList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return this.mTemperatureList.size();
    }

    @Override
    public Object getItem(int arg0) {
//        return this.mDeviceList.get(arg0);

        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_Count;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = this.mInflater.inflate(R.layout.item_temp, null);

        TextView secondsTextView = (TextView) v.findViewById(R.id.setTemp_seconds);
        TextView tempTextView = (TextView) v.findViewById(R.id.setTemp_temp);

        Temperature model = this.mTemperatureList.get(position);
        secondsTextView.setText("" + Convert.SecondConversion(model.getSeconds().intValue()));
        Log.e("test", " " + model.getTemp());
        tempTextView.setText(model.getTemp() + mContext.getString(R.string.tempUnit));

        return v;
    }

    public void setData(ArrayList<Temperature> list) {
        this.mTemperatureList = list;
    }
}
