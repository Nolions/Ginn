package tw.nolions.coffeebeanslife.service.Application;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.UUID;

public class MainApplication extends Application {
    private static Context mContext;
    private static MainApplication mAPP;
    private PackageInfo mPackagepInfo;

    final private String uuid = "00001101-0000-1000-8000-00805F9B34FB";
    final private String tag = "CoffeeBeansLife";
    final private int accessCoarseLocationCode = 1;

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
        return tag;
    }

    public UUID BluetoothUUID() {
        return UUID.fromString(uuid);
    }

    public int VersionCode() {
        int code = 0;

        try {
            if (mPackagepInfo == null) {
                mPackagepInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            }
            code = mPackagepInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {

        }

        return code;
    }

    public String VersionName() {
        String verName = "";

        try {
            if (mPackagepInfo == null) {
                mPackagepInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(),0);
            }
            verName = mPackagepInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(tag, "info::VersionName(), error:" + e.getMessage());
        }

        return verName;
    }

    public int PermissionsRequestAccessLocationCode() {
        return accessCoarseLocationCode;
    }
}
