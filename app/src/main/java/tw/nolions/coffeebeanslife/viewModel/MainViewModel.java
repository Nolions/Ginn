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
import tw.nolions.coffeebeanslife.MainApplication;
import tw.nolions.coffeebeanslife.R;
import tw.nolions.coffeebeanslife.callback.ViewModelCallback;

public class MainViewModel extends ViewModel {

    public final ObservableField<String> mBeansTemp, mStoveTemp, mEnvironmentTemp, mTargetTemp, mFirstCrackTime, mSecondCrackTime, mRunTime;
    public final ObservableBoolean mIsImport, mIsFirstCrack, mIsSecondCrack;

    private ViewModelCallback mViewModelCallback;

    private Activity mActivity;

    private String mTag;

    public MainViewModel(Fragment fragment, MainApplication app) {
        mActivity = fragment.getActivity();

        try {
            mViewModelCallback = (ViewModelCallback) fragment;
        } catch (Exception e) {
            Log.e(getTag(), e.getMessage());
        }

        mBeansTemp = new ObservableField<>();
        mStoveTemp = new ObservableField<>();
        mEnvironmentTemp = new ObservableField<>();
        mFirstCrackTime = new ObservableField<>();
        mSecondCrackTime = new ObservableField<>();
        mTargetTemp = new ObservableField<>();
        mRunTime = new ObservableField<>();

        mIsImport = new ObservableBoolean(false);
        mIsFirstCrack = new ObservableBoolean(true);
        mIsSecondCrack = new ObservableBoolean(true);

        setTag(app.TAG());
        init();
    }

    private void init() {
        this.setTargetTemp("0");
    }

    private void setTag(String tag) {
        mTag = tag;
    }

    private String getTag() {
        return mTag;
    }

    public void setIsImport(boolean status) {
        this.mIsImport.set(status);
    }

    public boolean getIsImport() {
        return mIsImport.get();
    }

    public void setIsFirstCrack(boolean status) {
        this.mIsFirstCrack.set(status);
    }

    public void setIsSecondCrack(boolean status) {
        this.mIsSecondCrack.set(status);
    }

    public void setTargetTemp(String temp) {
        this.mTargetTemp.set(temp);
    }

    public String getTargetTemp() {
        return this.mTargetTemp.get();
    }

    public void setBeansTemp(String temp) {
        this.mBeansTemp.set(temp + mActivity.getResources().getString(R.string.tempUnit));
    }

    public void setStoveTemp(String temp) {
        this.mStoveTemp.set(temp + mActivity.getResources().getString(R.string.tempUnit));
    }

    public void setEnvironmentTemp(String temp) {
        this.mEnvironmentTemp.set(temp + mActivity.getResources().getString(R.string.tempUnit));
    }

    public void setFirstCrackTime(int seconds) {
        String time = "";
        if (seconds >= 0) {
            time = Convert.SecondConversion(seconds);
        }
        this.mFirstCrackTime.set(time);
    }

    public void setSecondCrackTime(int seconds) {
        String time = "";
        if (seconds >= 0) {
            time = Convert.SecondConversion(seconds);
        }
        this.mSecondCrackTime.set(time);
    }

    public void setRunTime(int seconds) {
        String time = "";
        if (seconds >= 0) {
            time = Convert.SecondConversion(seconds);
        }
        this.mRunTime.set(time);
    }

    /**
     * 更新View Model上溫度資訊
     *
     * @param data
     */
    public void updateTemp(HashMap data) {
        setBeansTemp(String.valueOf(data.get("b")));
        setStoveTemp(String.valueOf(data.get("s")));
        setEnvironmentTemp(String.valueOf(data.get("e")));
    }

    /**
     * 變更目標溫度
     *
     * @param seekBar
     * @param progressValue
     * @param fromUser
     */
    public void onTargetTempChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
        Log.d(mTag, "MainViewModel::onTargetTempChanged(), progress:" + progressValue);
        this.setTargetTemp(String.valueOf(progressValue));
    }

    /**
     * 更新目標溫度
     *
     * @param seekBar
     */
    public void onSendTargetTemp(SeekBar seekBar) {
        Log.d(getTag(), "MainViewModel::onSendTargetTemp(), now Target temp :" + getTargetTemp());
        mViewModelCallback.updateTargetTemp(this.getTargetTemp());
    }

    /**
     * 下豆按鈕
     *
     * @return bool
     */
    public boolean startBeansClick() {
        Log.d(getTag(), "MainViewModel::startBeansClick()");
        mViewModelCallback.actionBean(true);
        return false;
    }

    /**
     * 出豆按鈕
     *
     * @return bool
     */
    public boolean stopBeansClick() {
        Log.d(getTag(), "MainViewModel::stopBeansClick()");
        mViewModelCallback.actionBean(false);
        return true;
    }

    /**
     * 一爆按鈕
     */
    public void onFirstCrack() {
        Log.d(getTag(), "MainViewModel::onFirstCrack()");
        mViewModelCallback.firstCrack();
    }

    /**
     * 二爆按鈕
     */
    public void onSecondCrack() {
        Log.d(getTag(), "MainViewModel::onSecondCrack()");
        mViewModelCallback.secondCrack();
    }

    /**
     * 重設溫度資訊列時間資訊
     */
    public void refresh() {
        this.setRunTime(-1);
        this.setFirstCrackTime(-1);
        this.setSecondCrackTime(-1);
    }
}
