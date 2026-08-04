package com.uccd3223.p1_chai_boon_hong_2206806;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private final List<HistoryRecord> records;

    public HistoryAdapter(List<HistoryRecord> records) {
        this.records = records;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_history,
                parent,
                false
        );
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryRecord record = records.get(position);
        switch (record.getGameName()) {
            case "Association":
                holder.tvGameName.setText(R.string.number_association_title);
                break;
            case "PlaceValue":
                holder.tvGameName.setText(R.string.place_value_title);
                break;
            case "Recognition":
                holder.tvGameName.setText(R.string.number_recognition_title);
                break;
            case "Sequence":
                holder.tvGameName.setText(R.string.number_sequence_title);
                break;
            default:
                holder.tvGameName.setText(record.getGameName());
                break;
        }

        if ("FUN".equals(record.getMode())) {
            holder.chipMode.setVisibility(View.GONE);
        } else {
            holder.chipMode.setVisibility(View.VISIBLE);
            if ("TIME_ATTACK".equals(record.getMode())) {
                holder.chipMode.setText(R.string.mode_time_attack);
            } else if ("SCORE_ATTACK".equals(record.getMode())) {
                holder.chipMode.setText(R.string.mode_score_attack);
            } else {
                holder.chipMode.setText(record.getMode());
            }
        }

        if ("SCORE_ATTACK".equals(record.getMode())) {
            holder.tvScore.setText(holder.itemView.getContext().getString(
                    R.string.round_reached,
                    record.getScore()
            ));
        } else {
            holder.tvScore.setText(holder.itemView.getResources().getQuantityString(
                    R.plurals.points_earned,
                    record.getScore(),
                    record.getScore()
            ));
        }

        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
        holder.tvDate.setText(format.format(new Date(record.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvGameName;
        final Chip chipMode;
        final TextView tvScore;
        final TextView tvDate;

        ViewHolder(View view) {
            super(view);
            tvGameName = view.findViewById(R.id.tvGameName);
            chipMode = view.findViewById(R.id.chipMode);
            tvScore = view.findViewById(R.id.tvScore);
            tvDate = view.findViewById(R.id.tvDate);
        }
    }
}
