package tools;

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
}
