package com.mattech.on_call.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.mattech.on_call.R;
import com.mattech.on_call.databinding.UpdateItemBinding;
import com.mattech.on_call.models.Update;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UpdatesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Update> updates = new ArrayList<>();
    private UpdateListener listener;
    private volatile boolean clickEnabled = true;
    private static final long ADD_ITEM_ID = -2;

    public interface UpdateListener {
        void addUpdate();

        void editUpdate(Update updateToEdit);

        void updateEnableStatusChanged(Update update);
    }

    class UpdateHolder extends RecyclerView.ViewHolder {
        private UpdateItemBinding binding;

        @BindView(R.id.main_container)
        RelativeLayout mainContainer;

        @BindView(R.id.update_time)
        TextView time;

        @BindView(R.id.update_date)
        TextView date;

        @BindView(R.id.monday)
        TextView monday;

        @BindView(R.id.tuesday)
        TextView tuesday;

        @BindView(R.id.wednesday)
        TextView wednesday;

        @BindView(R.id.thursday)
        TextView thursday;

        @BindView(R.id.friday)
        TextView friday;

        @BindView(R.id.saturday)
        TextView saturday;

        @BindView(R.id.sunday)
        TextView sunday;

        @BindView(R.id.enable_switch)
        Switch enabled;

        UpdateHolder(@NonNull UpdateItemBinding binding) {
            super(binding.getRoot());
            ButterKnife.bind(this, itemView);
            this.binding = binding;
            mainContainer.setOnClickListener(v -> itemClicked(getAdapterPosition()));
        }

        void bind(Update update) {
            binding.setUpdate(update);
            binding.executePendingBindings();
        }
    }

    class AddHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.add_update_btn)
        ImageButton addBtn;

        AddHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            addBtn.setOnClickListener(v -> itemClicked(getAdapterPosition()));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        switch (viewType) {
            case 0:
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_item, parent, false);
                viewHolder = new AddHolder(view);
                break;
            default:
                UpdateItemBinding binding = UpdateItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                viewHolder = new UpdateHolder(binding);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() != 0) {
            Update update = updates.get(position - 1);
            UpdateHolder updateHolder = (UpdateHolder) holder;
            updateHolder.enabled.setOnCheckedChangeListener(null);
            updateHolder.bind(update);
            updateHolder.enabled.setOnCheckedChangeListener((v, b) -> {
                if (v.isChecked() != update.isEnabled() && listener != null) {
                    Update changedUpdate = new Update(update);
                    changedUpdate.setEnabled(v.isChecked());
                    listener.updateEnableStatusChanged(changedUpdate);
                }
            });
            try {
                updateHolder.time.setText(update.getFormattedTime());
            } catch (ParseException e) {
                Log.e(getClass().getSimpleName(), "Time string retrieved from Update object has wrong format: " + update.getTime());
                updateHolder.time.setText(update.getTime());
            }
        }
    }

    private synchronized void itemClicked(int position) {
        if (clickEnabled) {
            if (listener != null) {
                if (position == 0) {
                    listener.addUpdate();
                } else if (position != RecyclerView.NO_POSITION) {
                    listener.editUpdate(updates.get(position - 1));
                }
            }
            clickEnabled = false;
        }
    }

    @Override
    public long getItemId(int position) {
        return position == 0 ? ADD_ITEM_ID : updates.get(position - 1).getId();
    }

    @Override
    public int getItemCount() {
        return updates.size() + 1;
    }

    public Update getUpdateAt(int position) {
        return updates.get(position);
    }

    public void setUpdates(List<Update> updates) {
        if (this.updates.size() == 0) {
            this.updates = updates;
            notifyDataSetChanged();
        } else if (updates.size() < this.updates.size()) {
            int removedItemIndex = Update.getRemovedItemIndex(this.updates, updates);
            if (removedItemIndex != -1) {
                this.updates.remove(removedItemIndex);
                notifyItemRemoved(removedItemIndex + 1);
                notifyItemRangeChanged(removedItemIndex + 1, updates.size() - removedItemIndex);
            }
        } else if (updates.size() > this.updates.size()) {
            int insertedItemIndex = Update.getInsertedItemIndex(this.updates, updates);
            if (insertedItemIndex != -1) {
                this.updates.add(updates.get(insertedItemIndex));
                notifyItemInserted(insertedItemIndex + 1);
            }
        } else {
            int changedItemIndex = Update.getEditedItemIndex(this.updates, updates);
            if (changedItemIndex != -1) {
                this.updates.set(changedItemIndex, updates.get(changedItemIndex));
                notifyItemChanged(changedItemIndex + 1);
            }
        }
    }

    public void setListener(UpdateListener listener) {
        this.listener = listener;
    }

    public void setClickEnabled(boolean clickEnabled) {
        this.clickEnabled = clickEnabled;
    }
}
