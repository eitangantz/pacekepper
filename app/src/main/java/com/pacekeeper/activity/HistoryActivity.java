package com.pacekeeper.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pacekeeper.R;
import com.pacekeeper.adapter.HistoryAdapter;
import com.pacekeeper.model.RunningSession;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class HistoryActivity extends BaseActivity {
    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new HistoryAdapter());
    }

    @OnClick(R.id.btn_back)
    void back() {
        onBackPressed();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_history;
    }
}
