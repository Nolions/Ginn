package tw.nolions.coffeebeanslife.widget;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import tools.Convert;
import tw.nolions.coffeebeanslife.R;
import tw.nolions.coffeebeanslife.model.Temperature;
import tw.nolions.coffeebeanslife.viewModel.TempItemViewModel;
import tw.nolions.coffeebeanslife.databinding.ItemTempBinding;

public class AutoTempAdapter extends BaseAdapter {

//    private ItemTempBinding mBinding;
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
        ItemTempBinding mBinding;
        if (convertView == null) {
            mBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_temp, parent, false);
            convertView = mBinding.getRoot();
        } else {
            mBinding = DataBindingUtil.getBinding(convertView);
        }

        TempItemViewModel viewModel = new TempItemViewModel();
        mBinding.setTempItemViewModel(viewModel);

        Temperature model = this.mTemperatureList.get(position);
        HashMap<String, Integer> map = Convert.SecondToTimeMap(model.getSeconds().intValue());
        viewModel.setTimeMinute(map.get("m"));
        viewModel.setTimeSecond(map.get("s"));
        viewModel.setTemp(Math.round(model.getTemp()));
        return convertView;
    }

    public void setData(ArrayList<Temperature> list) {
        this.mTemperatureList = list;
    }

    public ArrayList<Temperature> getData() {

        return this.mTemperatureList;
    }
}
