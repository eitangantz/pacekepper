package com.pacekeeper.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pacekeeper.model.Pace;
import com.pacekeeper.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PaceAdapter extends RecyclerView.Adapter<PaceAdapter.ViewHolder> {
    private List<Pace> items;
    private SelectPaceListener listener;
    private Pace selectedItem;

    public PaceAdapter(List<Pace> items, SelectPaceListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pace, parent, false);
        return new PaceAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), position == items.size() - 1);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text)
        TextView text;
        @BindView(R.id.margin)
        View margin;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(Pace item, boolean isLastItem) {
            if (isLastItem) {
                margin.setVisibility(View.VISIBLE);
            } else {
                margin.setVisibility(View.GONE);
            }
            if (selectedItem == null) {
                itemView.setBackgroundResource(R.drawable.bg_main_rounded);
            } else {
                if (selectedItem.getTime() == item.getTime()) {
                    itemView.setBackgroundResource(R.drawable.bg_main_with_stroke);
                } else {
                    itemView.setBackgroundResource(R.drawable.bg_main_rounded);
                }
            }
            text.setText(item.getTimeText());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.paceSelected(item);
                    selectedItem = item;
                }
            });
        }
    }

    public interface SelectPaceListener {
        void paceSelected(Pace pace);
    }
}
