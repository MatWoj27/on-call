package com.mattech.on_call.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.mattech.on_call.R;
import com.mattech.on_call.models.Update;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UpdatesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Update> updates = new ArrayList<>();
    private AddUpdateListener listener;

    public interface AddUpdateListener {
        void addUpdate();
    }

    class UpdateHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.update_time)
        TextView time;

        @BindView(R.id.enable_switch)
        Switch enabled;

        public UpdateHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class AddHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.add_update_btn)
        ImageButton addBtn;

        public AddHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        View view;
        switch (viewType) {
            case 0:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_item, parent, false);
                viewHolder = new AddHolder(view);
                break;
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.update_item, parent, false);
                viewHolder = new UpdateHolder(view);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 0:
                AddHolder addHolder = (AddHolder) holder;
                addHolder.addBtn.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.addUpdate();
                    }
                });
                break;
            default:
                Update update = updates.get(position - 1);
                UpdateHolder updateHolder = (UpdateHolder) holder;
                updateHolder.time.setText(update.getTime());
                updateHolder.enabled.setChecked(update.isEnabled());
                break;
        }
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public int getItemCount() {
        return updates.size() + 1;
    }

    public void setUpdates(List<Update> updates) {
        this.updates = updates;
        notifyDataSetChanged();
    }

    public void setListener(AddUpdateListener listener) {
        this.listener = listener;
    }
}
