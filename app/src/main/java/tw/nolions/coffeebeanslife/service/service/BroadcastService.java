package tw.nolions.coffeebeanslife.service.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import tw.nolions.coffeebeanslife.Const;

public class BroadcastService extends Service {
    private CountDownTimer cdt;
    private final IBinder mIBinder = new LocalBinder();
    private Handler mHandler = new Handler();
    private ArrayList<Integer> mTempList;
    private int timeSec = 0;
    private int timeRange = 2;
    private int i = 0;

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mIBinder;
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public void startCountDownTimer() {
        i = 0;
        cdt = new CountDownTimer(timeSec * 1000, timeRange * 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (i < mTempList.size()) {
                    int temp = mTempList.get(i);
                    i += 1;
                    Message msg = mHandler.obtainMessage(Const.BROADCAST_SERVICE_Time_RUINNER_SET_DATA);
                    msg.obj = temp;
                    msg.sendToTarget();
                }
            }

            @Override
            public void onFinish() {
                Message msg = mHandler.obtainMessage(Const.BROADCAST_SERVICE_Time_RUINNER_END);
                msg.sendToTarget();
            }
        };

        cdt.start();
    }

    public void stop() {
        if (cdt != null) {
            cdt.cancel();
        }
    }

    public void setTimeSec(int sec) {
        timeSec = sec;
    }

    public void setTimeRange(int range) {
        timeRange = range;
    }

    public void setTempMap(ArrayMap<Integer, Integer> map) {
        mTempList = new ArrayList<>();
        Map<Integer, Integer> param = new TreeMap<>(map);
        for (Integer key : param.keySet()) {
            mTempList.add(param.get(key));
        }
    }

    public class LocalBinder extends Binder {
        public BroadcastService getInstance() {
            return BroadcastService.this;
        }
    }
}
