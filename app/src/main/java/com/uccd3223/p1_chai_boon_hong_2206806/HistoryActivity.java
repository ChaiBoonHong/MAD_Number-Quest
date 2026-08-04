package com.uccd3223.p1_chai_boon_hong_2206806;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private static final String STATE_SELECTED_MODE = "history_selected_mode";

    private RecyclerView rvHistory;
    private List<HistoryRecord> historyList;
    private TextView tvEmptyHistory;
    private MaterialButton btnHistoryFun;
    private MaterialButton btnHistoryChallenge;
    private int selectedModeIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        rvHistory = findViewById(R.id.rvHistory);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        historyList = HistoryManager.getHistory(this);

        btnHistoryFun = findViewById(R.id.btnHistoryFun);
        btnHistoryChallenge = findViewById(R.id.btnHistoryChallenge);
        btnHistoryFun.setOnClickListener(view -> selectMode(0));
        btnHistoryChallenge.setOnClickListener(view -> selectMode(1));
        selectMode(savedInstanceState == null
                ? 0
                : savedInstanceState.getInt(STATE_SELECTED_MODE, 0));
    }

    private void selectMode(int modeIndex) {
        selectedModeIndex = Math.max(0, Math.min(1, modeIndex));
        btnHistoryFun.setChecked(selectedModeIndex == 0);
        btnHistoryChallenge.setChecked(selectedModeIndex == 1);
        updateList(selectedModeIndex);
    }

    private void updateList(int tabPosition) {
        List<HistoryRecord> filtered = new ArrayList<>();
        for (HistoryRecord record : historyList) {
            boolean funRecord = "FUN".equals(record.getMode());
            if (tabPosition == 0 && funRecord || tabPosition == 1 && !funRecord) {
                filtered.add(record);
            }
        }
        rvHistory.setAdapter(new HistoryAdapter(filtered));
        tvEmptyHistory.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_MODE, selectedModeIndex);
        super.onSaveInstanceState(outState);
    }
}
