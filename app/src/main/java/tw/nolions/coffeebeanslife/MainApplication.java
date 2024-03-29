package tw.nolions.coffeebeanslife;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.UUID;

import tw.nolions.coffeebeanslife.model.recordDao;

public class MainApplication extends Application {
    private Context mContext;
    private static MainApplication mAPP;
    private PackageInfo mPackageInfo;
    private AppDatabase mAppDatabase;
    private recordDao mRecordDao;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getInstance() {
        if (mAPP == null) {
            mAPP = new MainApplication();
        }

        return mAPP;
    }

    public String TAG() {
        return mContext.getString(R.string.app_name);
    }

    public UUID BluetoothUUID() {
        return UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    }

    public int VersionCode() {
        int code = 0;

        try {
            if (mPackageInfo == null) {
                mPackageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            }
            code = mPackageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG(), "MainApplication::VersionCode(), PackageManager NameNotFoundException", e);
        }

        return code;
    }

    public String VersionName() {
        String verName = "";

        try {
            if (mPackageInfo == null) {
                mPackageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            }
            verName = mPackageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG(), "MainApplication::VersionName(), PackageManager.NameNotFoundException" + e);
        }

        return verName;
    }

    public int PermissionsRequestAccessLocationCode() {
        return 1;
    }

    public AppDatabase appDatabase() {
        if (mAppDatabase == null) {
            mAppDatabase = Room.databaseBuilder(mContext, AppDatabase.class, "CoffeeBeanLife").build();
        }

        return mAppDatabase;
    }

    public recordDao recordDao() {
        if (mRecordDao == null) {
            mRecordDao = appDatabase().getRecordDao();
        }

        return mRecordDao;
    }
}
