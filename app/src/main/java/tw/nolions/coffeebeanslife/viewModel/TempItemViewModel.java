package tw.nolions.coffeebeanslife.viewModel;

import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableField;

public class TempItemViewModel extends ViewModel {
    public ObservableField<String> mTemp, mTimeMinute, mTimeSecond;

    public TempItemViewModel() {
        mTemp = new ObservableField<>();
        mTimeMinute = new ObservableField<>();
        mTimeSecond = new ObservableField<>();

        mTemp.set("0");
    }

    public void setTemp(int temp) {
        this.mTemp.set(String.valueOf(temp));
    }

    public void setTimeMinute(Integer minute) {
        this.mTimeMinute.set(String.valueOf(minute));
    }

    public void setTimeSecond(Integer second) {
        this.mTimeSecond.set(String.valueOf(second));
    }

    public void onAddTempClick() {
        int temp = Integer.valueOf(this.mTemp.get());
        if (temp < 300) {
            temp++;
        }
        this.setTemp(temp);
    }

    public void onLessTempClick() {
        int temp = Integer.valueOf(this.mTemp.get());
        if (temp > 0) {
            temp--;
        }
        this.setTemp(temp);
    }
}
