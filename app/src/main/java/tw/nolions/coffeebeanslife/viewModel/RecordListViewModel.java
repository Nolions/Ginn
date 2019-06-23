package tw.nolions.coffeebeanslife.viewModel;

import android.app.Activity;
import android.arch.lifecycle.ViewModel;
import android.support.v4.app.Fragment;

import tw.nolions.coffeebeanslife.MainApplication;

public class RecordListViewModel extends ViewModel {

    private Activity mActivity;

    private String mTag;

    public RecordListViewModel(Fragment fragment, MainApplication app) {
        mActivity = fragment.getActivity();
    }
}
