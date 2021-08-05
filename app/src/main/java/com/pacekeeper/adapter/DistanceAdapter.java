package com.pacekeeper.adapter;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pacekeeper.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DistanceAdapter extends RecyclerView.Adapter<DistanceAdapter.ViewHolder> {
    private List<Double> items;
    private DistanceSelectedListener listener;
    private Double selectedItem;
    private int selectedPosition;

    public DistanceAdapter(List<Double> items, int selectedPosition, DistanceSelectedListener listener) {
        this.items = items;
        this.selectedPosition = selectedPosition;
        this.listener = listener;
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        selectedItem = items.get(position);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DistanceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_distance, parent, false);
        return new DistanceAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DistanceAdapter.ViewHolder holder, int position) {
        holder.bind(items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text)
        TextView text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(Double item, int pos) {
            if (selectedPosition == pos) {
                text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34);
                text.setTextColor(Color.parseColor("#DC538D"));
            } else {
                if (pos == selectedPosition - 1 || pos == selectedPosition + 1) {
                    text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
                } else {
                    text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                }
                text.setTextColor(Color.parseColor("#B9B9B9"));
            }
            text.setText(String.valueOf(item));
            itemView.setOnClickListener(v -> listener.distanceSelected(item, pos));
        }
    }

    public interface DistanceSelectedListener {
        void distanceSelected(Double pace, int pos);
    }
}