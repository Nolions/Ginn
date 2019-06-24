package tw.nolions.coffeebeanslife.widget;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.databinding.library.baseAdapters.BR;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import tw.nolions.coffeebeanslife.MainActivity;
import tw.nolions.coffeebeanslife.MainApplication;
import tw.nolions.coffeebeanslife.R;
import tw.nolions.coffeebeanslife.databinding.ItemRecordBinding;
import tw.nolions.coffeebeanslife.fragment.RecordFragment;
import tw.nolions.coffeebeanslife.model.entity.RecordEntity;
import tw.nolions.coffeebeanslife.model.recordDao;

public class RecordListAdapter extends BaseAdapter {

    // CONST
    final private int CLICK_DELETE = 1;
    final private int CLICK_VIEW = 2;

    private Activity mActivity;
    private List<RecordEntity> mRecordList;
    private recordDao mRecordDao;

    public RecordListAdapter(Activity activity) {
        mActivity = activity;
        mRecordDao = ((MainApplication) mActivity.getApplication()).recordDao();
        mRecordList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mRecordList.size();
    }

    @Override
    public Object getItem(int position) {
        return mRecordList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemRecordBinding binding;
        if (convertView == null) {
            binding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.getContext()),
                    R.layout.item_record,
                    parent,
                    false
            );

            convertView = binding.getRoot();
        } else {
            binding = DataBindingUtil.getBinding(convertView);
        }
        RecordEntity record = mRecordList.get(position);
        binding.setVariable(BR.record, record);

        binding.btnDelete.setOnClickListener(new OnClickListener(CLICK_DELETE, position));
        convertView.setOnClickListener(new OnClickListener(CLICK_VIEW, position));

        return convertView;
    }

    private class OnClickListener implements View.OnClickListener {
        private int mPosition;
        private int mTypeCode;

        public OnClickListener(int type, int position) {
            mTypeCode = type;
            mPosition = position;
        }

        @Override
        public void onClick(View v) {

            switch (mTypeCode) {
                case CLICK_DELETE:
                    delete();
                    break;
                case CLICK_VIEW:
                    showRecordHistory();
                    break;
            }
        }

        private void showRecordHistory() {
            RecordEntity record = mRecordList.get(mPosition);
            RecordFragment recordFragment = new RecordFragment();

            Bundle bundle = new Bundle();
            bundle.putInt("recordID" , record.id);
            recordFragment.setArguments(bundle);

            FragmentManager fm = ((MainActivity) mActivity).getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.addToBackStack(recordFragment.getClass().getName());
            transaction.replace(R.id.container, recordFragment);

            transaction.commit();
        }

        private void delete() {
            Log.e("test", "test 11 :" + mPosition);
            final RecordEntity record = mRecordList.get(mPosition);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mRecordDao.delete(record);

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRecordList.remove(mPosition);
                            notifyDataSetChanged();
                        }
                    });
                }
            }).start();
        }
    }

    public void setData(List<RecordEntity> list) {
        mRecordList = list;
    }
}


