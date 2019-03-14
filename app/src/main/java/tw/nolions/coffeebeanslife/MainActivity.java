package tw.nolions.coffeebeanslife;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import tw.nolions.coffeebeanslife.fragment.MainFragment;
import tw.nolions.coffeebeanslife.service.BluetoothAcceptService;
import tw.nolions.coffeebeanslife.service.BluetoothSingleton;

public class MainActivity extends AppCompatActivity {

    static public final String TAG = "CoffeeBeansLife_Android";
    private BluetoothAdapter mBluetoothAdapter;
    private Bundle mSavedInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        init();
        setBluetooth();
    }

    private void init() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothSingleton.getInstance().setBluetoothAdapter(mBluetoothAdapter);
    }

    public void setBluetooth() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, getResources().getString(R.string.noSupportBluetooth), Toast.LENGTH_LONG).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, BluetoothAcceptService.REQUEST_ENABLE_BT);
            } else {
                this.FragmentTransaction();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothAcceptService.REQUEST_ENABLE_BT){
            if (resultCode == RESULT_OK) {
                this.FragmentTransaction();
            }
        }

    }

    private void FragmentTransaction() {
        if (mSavedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }
    }
}
