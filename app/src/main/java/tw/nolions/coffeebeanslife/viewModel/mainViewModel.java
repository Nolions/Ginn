package tw.nolions.coffeebeanslife.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.util.Log;

import java.util.HashMap;

import tw.nolions.coffeebeanslife.R;

public class MainViewModel extends ViewModel {
    private final String TAG;
    private final float TEMP_RANGE = 5;

    public final ObservableField<String> mBeansTemp, mStoveTemp, mEnvironmentTemp;
    public final ObservableBoolean isPowerOn, isImport;

    private Context mContext;

    private float mNowStoveTemp = 0;

    public MainViewModel(Context context) {
        mContext = context;
        TAG = mContext.getResources().getString(R.string.app_name);

        mBeansTemp = new ObservableField<>();
        mStoveTemp = new ObservableField<>();
        mEnvironmentTemp = new ObservableField<>();

        isPowerOn = new ObservableBoolean(false);
        isImport = new ObservableBoolean(true);
    }

    public void setMNowStoveTemp(float temp) {
        this.mNowStoveTemp = temp;
    }

    public float getmNowStoveTemp() {
        return mNowStoveTemp;
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
        this.setStoveTemp();
    }

    public void onClickTempLessActionButton() {
        Log.d(TAG, "onclick TempLessActionButton...");

        this.lessTemp();
        this.setStoveTemp();
    }

    public void onClickBeanImportActionButton() {
        Log.d(TAG, "onclick BeanImportActionButton...");

        if (isImport.get() == true) {
            isImport.set(false);
        } else {
            isImport.set(true);
        }
    }

    public void onClickBoomActionButton() {
        Log.d(TAG, "onclick BoomActionButton...");


    }

    public void onChangedPowerOnSwitch(Boolean isChecked) {
        Log.d(TAG, "on change PowerOnSwitch...");
        Log.d(TAG, "PowerOnSwitch status is " + isChecked);

        isPowerOn.set(isChecked);
    }

    private void addTemp() {
        float temp = this.mNowStoveTemp;
                //this.getmNowStoveTemp();

        if (temp <= 95) {
            temp = temp + TEMP_RANGE;
        }

        this.mNowStoveTemp = temp;
    }

    private void lessTemp() {
        float temp = this.mNowStoveTemp;
                //this.getmNowStoveTemp();

        if (temp >= 5) {
            temp = temp - TEMP_RANGE;
        }

        this.mNowStoveTemp = temp;
    }

    private void setStoveTemp() {
        mStoveTemp.set(String.valueOf(this.getmNowStoveTemp() + mContext.getResources().getString(R.string.tempUnit)));
    }

    public void updateTemp(HashMap data) {
        String bean = tools.Convert.DecimalPoint(Double.valueOf((String) data.get("bean")));
        String stove = tools.Convert.DecimalPoint(Double.valueOf((String) data.get("stove")));
        String environment = tools.Convert.DecimalPoint(Double.valueOf((String) data.get("environment")));

        this.updateTempLabel(bean, stove, environment);
    }

    public void updateTempLabel(String beanTemp, String stoveTemp, String environmentTemp) {
        setBeansTemp(beanTemp);
        setStoveTemp(stoveTemp);
        setEnvironmentTemp(environmentTemp);
    }
}
