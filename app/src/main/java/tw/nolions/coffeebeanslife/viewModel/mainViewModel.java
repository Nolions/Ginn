package tw.nolions.coffeebeanslife.viewModel;

import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.util.Log;

import java.util.HashMap;

import tools.Convert;
import tw.nolions.coffeebeanslife.R;

public class MainViewModel extends ViewModel {
    private final String TAG;
    private final int TEMP_RANGE = 5;

    public final ObservableField<String> mBeansTemp, mStoveTemp, mEnvironmentTemp, mTargetTemp;
    public final ObservableBoolean isImport;

    private Context mContext;

    private int mNowSetTemp = 0;

    public MainViewModel(Context context) {
        mContext = context;
        TAG = mContext.getResources().getString(R.string.app_name);

        mBeansTemp = new ObservableField<>();
        mStoveTemp = new ObservableField<>();
        mEnvironmentTemp = new ObservableField<>();
        mTargetTemp = new ObservableField<>();

        this.setTargetTemp("0");
        isImport = new ObservableBoolean(false);
    }

    public void setNowSetTemp(int temp) {
        this.mNowSetTemp = temp;
    }

    public int getNowSetTemp() {
        return this.mNowSetTemp;
    }

    public void setTargetTemp(String temp) {
        this.mTargetTemp.set(temp);
    }

    public void setBeansTemp(String temp) {
        this.mBeansTemp.set(temp +  mContext.getResources().getString(R.string.tempUnit));
    }

    public void setStoveTemp(String temp) {
        this.mStoveTemp.set(temp +  mContext.getResources().getString(R.string.tempUnit));
    }

    public void setEnvironmentTemp(String temp) {
        this.mEnvironmentTemp.set(temp +  mContext.getResources().getString(R.string.tempUnit));
    }

    public void onClickTempPlusActionButton() {
        Log.d(TAG, "onclick TempPlusActionButton...");

        this.addTemp();
        this.setTargetTemp("" + this.getNowSetTemp());
    }

    public void onClickTempLessActionButton() {
        Log.d(TAG, "onclick TempLessActionButton...");

        this.lessTemp();
        this.setTargetTemp("" + this.getNowSetTemp());
    }

    public void onClickBeanImportActionButton() {
        Log.d(TAG, "onclick BeanImportActionButton...");

        if (isImport.get() == true) {
            isImport.set(false);
            setTargetTemp("0");
        } else {
            isImport.set(true);
        }
    }

    public void onClickOneBoom() {
        Log.d(TAG, "One Boom...");
    }

    public void onClickTwoBoom() {
        Log.d(TAG, "Two Boom...");
    }

    private void addTemp() {
        int temp = this.getNowSetTemp();

        if (temp <= 95) {
            temp = temp + TEMP_RANGE;
        }

        this.setNowSetTemp(temp);
    }

    private void lessTemp() {
        int temp = this.getNowSetTemp();

        if (temp >= 5) {
            temp = temp - TEMP_RANGE;
        }

        this.setNowSetTemp(temp);
    }

    public void updateTemp(HashMap data) {
        String bean = Convert.DecimalPoint((Double)data.get("b"));
        String stove = Convert.DecimalPoint((Double) data.get("s"));
        String environment = Convert.DecimalPoint((Double) data.get("e"));

        this.updateTempLabel(bean, stove, environment);
    }

    public void updateTempLabel(String beanTemp, String stoveTemp, String environmentTemp) {
        setBeansTemp(beanTemp);
        setStoveTemp(stoveTemp);
        setEnvironmentTemp(environmentTemp);
    }
}
