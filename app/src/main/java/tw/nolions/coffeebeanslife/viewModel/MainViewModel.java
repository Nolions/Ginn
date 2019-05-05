package tw.nolions.coffeebeanslife.viewModel;

import android.app.Activity;
import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.SeekBar;

import java.util.HashMap;

import tools.Convert;
import tools.info;
import tw.nolions.coffeebeanslife.R;
import tw.nolions.coffeebeanslife.callback.ViewModelCallback;

public class MainViewModel extends ViewModel {

    public final ObservableField<String> mBeansTemp, mStoveTemp, mEnvironmentTemp, mTargetTemp, mFirstCrackTime, mSecondCrackTime;
    public final ObservableBoolean mIsImport;

    private ViewModelCallback mViewModelCallback;

    private Activity mActivity;

    public MainViewModel(Fragment fragment) {
        mActivity = fragment.getActivity();

        try {
            mViewModelCallback = (ViewModelCallback) fragment;
        } catch (Exception e) {
            Log.e(info.TAG(), e.getMessage());
        }

        mBeansTemp = new ObservableField<>();
        mStoveTemp = new ObservableField<>();
        mEnvironmentTemp = new ObservableField<>();
        mFirstCrackTime = new ObservableField<>();
        mSecondCrackTime = new ObservableField<>();
        mTargetTemp = new ObservableField<>();

        mIsImport = new ObservableBoolean(false);

        init();
    }

    private void init() {
        this.setTargetTemp("0");
    }

    public void setIsImport(boolean status)
    {
        this.mIsImport.set(status);
    }

    public boolean getIsImport()
    {
        return mIsImport.get();
    }

    public void setTargetTemp(String temp) {
        this.mTargetTemp.set(temp);
    }

    public String getTargetTemp() {
        return this.mTargetTemp.get();
    }

    public void setBeansTemp(String temp) {
        this.mBeansTemp.set(temp +  mActivity.getResources().getString(R.string.tempUnit));
    }

    public void setStoveTemp(String temp) {
        this.mStoveTemp.set(temp +  mActivity.getResources().getString(R.string.tempUnit));
    }

    public void setEnvironmentTemp(String temp) {
        this.mEnvironmentTemp.set(temp +  mActivity.getResources().getString(R.string.tempUnit));
    }

    public void setFirstCrackTime(int seconds) {
        this.mFirstCrackTime.set(Convert.SecondConversion(seconds));
    }

    public void setSecondCrackTime(int seconds) {
        this.mSecondCrackTime.set(Convert.SecondConversion(seconds));
    }

    /**
     * 更新View Model上溫度資訊
     * @param data
     */
    public void updateTemp(HashMap data) {
        String bean = Convert.DecimalPoint((Double)data.get("b"));
        String stove = Convert.DecimalPoint((Double) data.get("s"));
        String environment = Convert.DecimalPoint((Double) data.get("e"));

        setBeansTemp(bean);
        setStoveTemp(stove);
        setEnvironmentTemp(environment);
    }

    /**
     * 變更目標溫度
     * @param seekBar
     * @param progressValue
     * @param fromUser
     */
    public void onTargetTempChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
        Log.d(info.TAG(), "MainViewModel::onTargetTempChanged(), progress:"+ progressValue );
        this.setTargetTemp(String.valueOf(progressValue));
    }

    /**
     * 更新目標溫度
     * @param seekBar
     */
    public void onSendTargetTemp(SeekBar seekBar) {
        Log.d(info.TAG(), "MainViewModel::onTargetTempChanged(), now Target temp :"+ getTargetTemp());
        mViewModelCallback.updateTargetTemp(this.getTargetTemp());
    }

    /**
     * 下豆按鈕
     * @return bool
     */
    public boolean startBeansClick() {
        Log.d(info.TAG(), "MainViewModel::startBeansClick()");
        mViewModelCallback.startAction(true);
        return false;
    }

    /**
     * 出豆按鈕
     * @return bool
     */
    public boolean stopBeansClick() {
        Log.d(info.TAG(), "MainViewModel::stopBeansClick()");
        mViewModelCallback.startAction(false);
        return true;
    }

    /**
     * 一爆按鈕
     */
    public void onFirstCrack() {
        Log.d(info.TAG(), "MainViewModel::onFirstCrack()");
        mViewModelCallback.firstCrack();
    }

    /**
     * 二爆按鈕
     */
    public void onSecondCrack() {
        Log.d(info.TAG(), "MainViewModel::onSecondCrack()");
        mViewModelCallback.secondCrack();
    }
}
