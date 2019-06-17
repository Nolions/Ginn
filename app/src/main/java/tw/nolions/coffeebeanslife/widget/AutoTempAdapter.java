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
import tw.nolions.coffeebeanslife.MainApplication;
import tw.nolions.coffeebeanslife.R;
import tw.nolions.coffeebeanslife.databinding.ItemTempBinding;
import tw.nolions.coffeebeanslife.model.Temperature;
import tw.nolions.coffeebeanslife.viewModel.TempItemViewModel;

public class AutoTempAdapter extends BaseAdapter {

    //    private ItemTempBinding mBinding;
    private LayoutInflater mInflater;
    private ArrayList<Temperature> mTemperatureList;

    private Context mContext;
    final private int TYPE_Count = 2;
    final public static int PAIRED_ITEM_TYPE = 0, NoPAIRED_ITEM_TYPE = 1;
    private String mTag;

    public AutoTempAdapter(Context context, MainApplication app) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mTemperatureList = new ArrayList<>();
        setTag(app.TAG());
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
                Log.d(getTag(), "item :" + position + " temp :" + mBinding.editTemp.getText());

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
                Temperature t = mTemperatureList.get(position);
                String minuteStr = mBinding.editTimeMinute.getText().toString();
                String secondsStr = mBinding.editTimeSeconds.getText().toString();
                int minute = 0;
                int seconds = 0;
                if (!minuteStr.matches("")) {
                    minute = Integer.valueOf(minuteStr);
                }
                if (!secondsStr.matches("")) {
                    seconds = Integer.valueOf(secondsStr);
                }

                seconds = minute * 60 + seconds;
                t.setSeconds(seconds);
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
                Temperature t = mTemperatureList.get(position);
                String minuteStr = mBinding.editTimeMinute.getText().toString();
                String secondsStr = mBinding.editTimeSeconds.getText().toString();
                int minute = 0;
                int seconds = 0;
                if (!minuteStr.matches("")) {
                    minute = Integer.valueOf(minuteStr);
                }
                if (!secondsStr.matches("")) {
                    seconds = Integer.valueOf(secondsStr);
                }

                seconds = minute * 60 + seconds;
                t.setSeconds(seconds);
            }
        });

        Temperature model = this.mTemperatureList.get(position);
        HashMap<String, Integer> map = Convert.SecondToTimeMap(model.getSeconds());
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

    private void setTag(String tag) {
        mTag = tag;
    }

    private String getTag() {
        return mTag;
    }
}
