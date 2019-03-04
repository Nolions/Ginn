package tw.nolions.coffeebeanslife;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import tw.nolions.coffeebeanslife.fragment.MainFragment;

public class MainActivity extends AppCompatActivity {

    static public final String TAG = "CoffeeBeansLife_Android";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }
    }
}
