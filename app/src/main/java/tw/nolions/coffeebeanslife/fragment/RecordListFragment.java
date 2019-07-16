package tw.nolions.coffeebeanslife.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import tw.nolions.coffeebeanslife.MainActivity;
import tw.nolions.coffeebeanslife.MainApplication;
import tw.nolions.coffeebeanslife.R;
import tw.nolions.coffeebeanslife.databinding.FragmentRecordListBinding;
import tw.nolions.coffeebeanslife.model.entity.RecordEntity;
import tw.nolions.coffeebeanslife.widget.RecordListAdapter;

public class RecordListFragment extends Fragment {
    private View mView;

    private RecordListAdapter mListViewAdapter;

    private FragmentRecordListBinding mBinding;

    private List<RecordEntity> mRecordList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_record_list,
                container,
                false);
        mView = mBinding.getRoot();

        initView();

        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void init() {
        mRecordList = new ArrayList<>();
        mListViewAdapter = new RecordListAdapter(getActivity());
    }

    private void initView() {
        mBinding.recordListListView.setAdapter(mListViewAdapter);

        Toolbar toolbar = (Toolbar) mView.findViewById(R.id.recordList_toolbar);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        loadRecords();
    }

    private void loadRecords() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mRecordList = ((MainApplication) getActivity().getApplication()).recordDao().getAll();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListViewAdapter.setData(mRecordList);
                        mListViewAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }
}
