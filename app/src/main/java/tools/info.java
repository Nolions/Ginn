package tools;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.UUID;

public class info {

    public static String TAG() {
        return "CoffeeBeansLife_Android";
    }

    public static UUID BluetoothUUID(){
        String uuid = "00001101-0000-1000-8000-00805F9B34FB";
        return UUID.fromString(uuid);
    }

    public static int PermissionsRequestAccessLocationCode() {
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        return MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION;
    }

    public static String VersionName(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo;
        String verName = "";

        try {
            packageInfo = pm.getPackageInfo(context.getPackageName(),0);
            verName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(info.TAG(), "info::VersionName(), error:" + e.getMessage());
        }

        return verName;
    }
}
