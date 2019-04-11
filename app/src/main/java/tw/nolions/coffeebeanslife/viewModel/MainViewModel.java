package tw.nolions.coffeebeanslife.viewModel;

import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import tools.Convert;
import tools.info;
import tw.nolions.coffeebeanslife.R;

public class MainViewModel extends ViewModel {
    private final String TAG;
    private final int TEMP_RANGE = 5;
    private String relayStatus = "c";

    public final ObservableField<String> mBeansTemp, mStoveTemp, mEnvironmentTemp, mTargetTemp;
    public final ObservableBoolean isImport;

    private Context mContext;
    private Handler mHandler;
    private OutputStream mOutputStream;

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

        init();
    }

    private void init() {
        initHandler();
    }

    private void initHandler() {
        // handler read data form bluetooth device
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String data = (String) msg.obj;
                Log.d(info.TAG(), "Send msg is " + data);
                try {
//                    mOutputStream.write(data.getBytes());

                    if (data.equals("c") || data.equals("o")) {
                        if (isImport.get() == true) {
                            mOutputStream.write("o".getBytes());
                            isImport.set(false);
                            setTargetTemp("0");
                        } else {
                            mOutputStream.write("c".getBytes());
                            isImport.set(true);
                        }
                    } else if(data.equals("add")) {
                        addTemp();
                        String temp = ""+ getNowSetTemp();
                        mOutputStream.write(temp.getBytes());
                        setTargetTemp("" + getNowSetTemp());
                    } else if(data.equals("less")) {
                        lessTemp();
                        String temp = ""+ getNowSetTemp();
                        mOutputStream.write(temp.getBytes());
                        setTargetTemp("" + getNowSetTemp());
                    }
                } catch (IOException e) {
                    Log.e(info.TAG(), "error :  " + e.getMessage());
                }

            }
        };
    }

    public void setOutputStream(OutputStream outputStream) {
        this.mOutputStream = outputStream;
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

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                msg.obj = "add";

                mHandler.sendMessage(msg);
            }
        });

        t.start();
    }

    public void onClickTempLessActionButton() {
        Log.d(TAG, "onclick TempLessActionButton...");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                msg.obj = "less";

                mHandler.sendMessage(msg);
            }
        });

        t.start();
    }

    public void onClickBeanImportActionButton() {
        Log.d(TAG, "onclick BeanImportActionButton...");

        if (isImport.get() == true) {
            relayStatus = "c";
        } else {
            relayStatus = "o";
        }

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                msg.obj = relayStatus;

                mHandler.sendMessage(msg);
            }
        });

        t.start();
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
