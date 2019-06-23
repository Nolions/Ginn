package tw.nolions.coffeebeanslife.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import tw.nolions.coffeebeanslife.R;

public class SmallProgressDialogUtil {
    private AlertDialog mAlertDialog;
    private TextView mTvTip;

    public SmallProgressDialogUtil(Context context) {
        initView(context, "載入中...");
    }

    public SmallProgressDialogUtil(Context context, String tip) {
        initView(context, tip);
    }

    public void initView(Context context, String tip) {
        if (mAlertDialog == null) {
            mAlertDialog = new AlertDialog.Builder(context, R.style.CustomProgressDialog).create();
        }

        View loadView = LayoutInflater.from(context).inflate(R.layout.small_progress_dialog, null);
        mAlertDialog.setView(loadView, 0, 0, 0, 0);
        mAlertDialog.setCanceledOnTouchOutside(false);

        mTvTip = loadView.findViewById(R.id.tvTip);
        mTvTip.setText(tip);
    }

    public void setText(String text) {
        mTvTip.setText(text);
    }

    public void show() {
        mAlertDialog.show();
    }

    /**
     * 隱藏耗時對話方塊
     */
    public void dismiss() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

}
