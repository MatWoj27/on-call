package com.mattech.on_call.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.mattech.on_call.R;
import com.mattech.on_call.models.Update;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UpdatesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<Update> updates = new ArrayList<>();
    private UpdateListener listener;

    public interface UpdateListener {
        void addUpdate();

        void editUpdate(Update updateToEdit);

        void updateEnableStatusChanged(Update update);
    }

    class UpdateHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.main_container)
        RelativeLayout mainContainer;

        @BindView(R.id.update_time)
        TextView time;

        @BindView(R.id.update_date)
        TextView date;

        @BindView(R.id.update_days_list)
        LinearLayout daysContainer;

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

        public UpdateHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        TextView[] getDayViewsArray() {
            TextView[] dayViews = {monday, tuesday, wednesday, thursday, friday, saturday, sunday};
            return dayViews;
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

    public UpdatesAdapter(Context context) {
        this.context = context;
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
                updateHolder.mainContainer.setOnClickListener(v -> {
                    if (listener != null && updateHolder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                        listener.editUpdate(update);
                    }
                });
                updateHolder.enabled.setOnCheckedChangeListener((v, b) -> {
                    if (v.isChecked() != update.isEnabled()) {
                        update.setEnabled(v.isChecked());
                        if (listener != null) {
                            listener.updateEnableStatusChanged(update);
                        }
                    }
                    if (update.isOneTimeUpdate()) {
                        updateHolder.date.setTextColor(context.getResources().getColor(update.isEnabled() ? R.color.disabledActive :
                                R.color.disabledInactive, null));
                    } else {
                        applyColorsToDayViews(updateHolder, update);
                    }
                    updateHolder.time.setTextColor(update.isEnabled() ? Color.BLACK : context.getColor(R.color.disabledActive));
                });
                updateHolder.time.setText(formatTime(update.getTime()));
                if (update.isEnabled()) {
                    updateHolder.enabled.setChecked(true);
                    updateHolder.time.setTextColor(Color.BLACK);
                }
                if (update.isOneTimeUpdate()) {
                    updateHolder.daysContainer.setVisibility(View.GONE);
                    updateHolder.date.setVisibility(View.VISIBLE);
                    updateHolder.date.setText(update.getExactDate());
                    if (update.isEnabled()) {
                        updateHolder.date.setTextColor(context.getResources().getColor(R.color.disabledActive, null));
                    }
                } else {
                    updateHolder.date.setVisibility(View.GONE);
                    updateHolder.daysContainer.setVisibility(View.VISIBLE);
                    applyColorsToDayViews(updateHolder, update);
                }
                break;
        }
    }

    private String formatTime(String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.US);
        try {
            Date date = simpleDateFormat.parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return (String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)));
        } catch (ParseException e) {
            Log.e(getClass().getSimpleName(), "Time string retrieved from Update object has wrong format" + time);
        }
        return null;
    }

    private void applyColorsToDayViews(UpdateHolder holder, Update update) {
        TextView[] dayViews = holder.getDayViewsArray();
        for (int i = 0; i < 7; i++) {
            int colorId;
            if (update.getRepetitionDays()[i] && update.isEnabled()) {
                colorId = R.color.enabledActive;
            } else if (update.getRepetitionDays()[i]) {
                colorId = R.color.disabledActive;
            } else {
                colorId = R.color.disabledInactive;
            }
            dayViews[i].setTextColor(context.getResources().getColor(colorId, null));
        }
    }

    private int getRemovedItemIndex(List<Update> original, List<Update> changed) {
        int removedItemIndex = original.size() == changed.size() ? -1 : original.size() - 1;
        for (int i = 0; i < original.size() && i < changed.size(); i++) {
            if (original.get(i).getId() != changed.get(i).getId()) {
                removedItemIndex = i;
                break;
            }
        }
        return removedItemIndex;
    }

    private int getInsertedItemIndex(List<Update> original, List<Update> changed) {
        int insertedItemIndex = original.size() == changed.size() ? -1 : original.size();
        for (int i = 0; i < original.size() && i < changed.size(); i++) {
            if (original.get(i).getId() != changed.get(i).getId()) {
                insertedItemIndex = i;
                break;
            }
        }
        return insertedItemIndex;
    }

    @Override
    public int getItemCount() {
        return updates.size() + 1;
    }

    public Update getUpdateAt(int position) {
        return updates.get(position);
    }

    public void setUpdates(List<Update> updates) {
        if (updates.size() < this.updates.size()) {
            int removedItemIndex = getRemovedItemIndex(this.updates, updates);
            if (removedItemIndex != -1) {
                this.updates.remove(removedItemIndex);
                notifyItemRemoved(removedItemIndex + 1);
            }
        } else if (updates.size() > this.updates.size()) {
            int insertedItemIndex = getInsertedItemIndex(this.updates, updates);
            if (insertedItemIndex != -1) {
                this.updates.add(updates.get(insertedItemIndex));
                notifyItemInserted(insertedItemIndex + 1);
            }
        } else {
            this.updates = updates;
            notifyDataSetChanged();
        }
    }

    public void setListener(UpdateListener listener) {
        this.listener = listener;
    }
}
