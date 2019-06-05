package tw.nolions.coffeebeanslife.widget;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.HashMap;

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
        return this.mTemperatureList.get(arg0);
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ItemTempBinding mBinding;
        if (convertView == null) {
            mBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_temp, parent, false);
            convertView = mBinding.getRoot();
        } else {
            mBinding = DataBindingUtil.getBinding(convertView);
        }

        TempItemViewModel viewModel = new TempItemViewModel();
        mBinding.setTempItemViewModel(viewModel);

        mBinding.editTemp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.e(tools.info.TAG(), "item :"+ position + " temp :" + mBinding.editTemp.getText());

                Temperature t = mTemperatureList.get(position);
                String nowTemp = mBinding.editTemp.getText().toString();
                if (nowTemp.matches("")) {
                    t.setTemp(0);
                } else {
                    t.setTemp(Float.parseFloat(nowTemp));
                }

                mTemperatureList.set(position, t);
            }
        });

        mBinding.editTimeMinute.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.e(tools.info.TAG(), "item :"+ position + " minute :" + mBinding.editTemp.getText());
                Temperature t = mTemperatureList.get(position);
                String minuteStr = mBinding.editTimeSeconds.getText().toString();
                String secondsStr = mBinding.editTimeSeconds.getText().toString();
                int minute = 0;
                int seconds = 0;
                if (!minuteStr.matches("")) {
                    minute = Integer.valueOf(minuteStr);
                }
                if (!secondsStr.matches("")) {
                    seconds = Integer.valueOf(minuteStr);
                }

                seconds = minute * 60 + seconds;
                t.setSeconds(Long.valueOf(seconds));
                mTemperatureList.set(position, t);
            }
        });

        mBinding.editTimeSeconds.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.e(tools.info.TAG(), "item :"+ position + " time seconds :" + mBinding.editTemp.getText());
                Temperature t = mTemperatureList.get(position);
                String minuteStr = mBinding.editTimeSeconds.getText().toString();
                String secondsStr = mBinding.editTimeSeconds.getText().toString();
                int minute = 0;
                int seconds = 0;
                if (!minuteStr.matches("")) {
                    minute = Integer.valueOf(minuteStr);
                }
                if (!secondsStr.matches("")) {
                    seconds = Integer.valueOf(minuteStr);
                }

                seconds = minute * 60 + seconds;
                t.setSeconds(Long.valueOf(seconds));
            }
        });

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
