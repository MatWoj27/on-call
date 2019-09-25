package com.mattech.on_call.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UpdatesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<Update> updates = new ArrayList<>();
    private UpdateListener listener;

    public interface UpdateListener {
        void addUpdate();

        void editUpdate(int i);

        void changeUpdateEnableStatus(Update update);
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
                    if (listener != null) {
                        listener.editUpdate(position - 1);
                    }
                });
                updateHolder.enabled.setOnClickListener(v -> {
                    update.setEnabled(!update.isEnabled());
                    if (listener != null) {
                        listener.changeUpdateEnableStatus(update);
                    }
                    if (update.isOneTimeUpdate()) {
                        updateHolder.date.setTextColor(context.getResources().getColor(update.isEnabled() ? R.color.disabledActive :
                                R.color.disabledInactive, null));
                    } else {
                        applyColorsToDayViews(updateHolder, update);
                    }
                    updateHolder.time.setTextColor(update.isEnabled() ? Color.BLACK : context.getColor(R.color.disabledActive));
                });
                updateHolder.time.setText(update.getTime());
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
                    applyColorsToDayViews(updateHolder, update);
                }
                break;
        }
    }

    private void applyColorsToDayViews(UpdateHolder holder, Update update) {
        TextView[] dayViews = holder.getDayViewsArray();
        for (int i = 0; i < 7; i++) {
            int colorId, backgroundDrawableId;
            if (update.getRepetitionDays()[i] && update.isEnabled()) {
                colorId = R.color.enabledActive;
                backgroundDrawableId = R.drawable.round_day_toggle_enabled;
            } else if (update.getRepetitionDays()[i]) {
                colorId = R.color.disabledActive;
                backgroundDrawableId = R.drawable.round_day_toggle_disabled_active;
            } else {
                colorId = R.color.disabledInactive;
                backgroundDrawableId = R.drawable.round_day_toggle_disabled_inactive;
            }
            dayViews[i].setTextColor(context.getResources().getColor(colorId, null));
            dayViews[i].setBackground(context.getResources().getDrawable(backgroundDrawableId, null));
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

    public void setListener(UpdateListener listener) {
        this.listener = listener;
    }
}
