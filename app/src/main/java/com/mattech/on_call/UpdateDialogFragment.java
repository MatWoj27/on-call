package com.mattech.on_call;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UpdateDialogFragment extends DialogFragment {
    private OnFragmentInteractionListener listener;
    private boolean displayDays = true;
    private boolean[] activeDays = new boolean[7];
    private final String DISPLAY_DAYS_TAG = "displayDays";
    private final String HOUR_TAG = "hour";
    private final String MINUTE_TAG = "minute";
    private final String ACTIVE_DAYS_TAG = "activeDays";

    @BindView(R.id.hour_picker)
    NumberPicker hourPicker;

    @BindView(R.id.minute_picker)
    NumberPicker minutePicker;

    @BindView(R.id.days_container)
    LinearLayout days;

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

    @BindView(R.id.exact_date)
    TextView exactDate;

    @BindView(R.id.update_type_switch)
    ImageView updateTypeSwitch;

    @BindView(R.id.cancel_btn)
    Button cancelBtn;

    @BindView(R.id.ok_btn)
    Button okBtn;

    public interface OnFragmentInteractionListener {
        void onOkClick();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_update, null);
        ButterKnife.bind(this, view);
        if (savedInstanceState != null) {
            displayDays = savedInstanceState.getBoolean(DISPLAY_DAYS_TAG);
            presetTimePickers(savedInstanceState.getInt(HOUR_TAG), savedInstanceState.getInt(MINUTE_TAG));
            activeDays = savedInstanceState.getBooleanArray(ACTIVE_DAYS_TAG);
        } else {
            presetTimePickers(12, 0);
        }
        if (!displayDays) {
            days.setVisibility(View.GONE);
            exactDate.setVisibility(View.VISIBLE);
            updateTypeSwitch.setImageDrawable(getResources().getDrawable(R.drawable.repeat, null));
        }
        TextView[] dayViews = {monday, tuesday, wednesday, thursday, friday, saturday, sunday};
        for (int i = 0; i < 7; i++) {
            dayViews[i].setOnClickListener(new DayClickListener(i));
            if (displayDays && activeDays[i]) {
                displayDayViewAsActive(dayViews[i]);
            }
        }
        updateTypeSwitch.setOnClickListener(v -> {
            if (displayDays) {
                days.setVisibility(View.GONE);
                exactDate.setVisibility(View.VISIBLE);
                updateTypeSwitch.setImageDrawable(getResources().getDrawable(R.drawable.repeat, null));
            } else {
                exactDate.setVisibility(View.GONE);
                days.setVisibility(View.VISIBLE);
                for (int i = 0; i < 7; i++) {
                    if (activeDays[i]) {
                        displayDayViewAsActive(dayViews[i]);
                    }
                }
                updateTypeSwitch.setImageDrawable(getResources().getDrawable(R.drawable.calendar, null));
            }
            displayDays = !displayDays;
        });
        okBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOkClick();
                dismiss();
            }
        });
        cancelBtn.setOnClickListener(v -> dismiss());
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(DISPLAY_DAYS_TAG, displayDays);
        outState.putInt(HOUR_TAG, hourPicker.getValue());
        outState.putInt(MINUTE_TAG, minutePicker.getValue());
        outState.putBooleanArray(ACTIVE_DAYS_TAG, activeDays);
    }

    private class DayClickListener implements View.OnClickListener {
        private int index;

        public DayClickListener(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View view) {
            if (activeDays[index]) {
                displayDayViewAsInactive((TextView) view);
            } else {
                displayDayViewAsActive((TextView) view);
            }
            activeDays[index] = !activeDays[index];
        }
    }

    private void presetTimePickers(int hour, int minute) {
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(23);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        hourPicker.setValue(hour);
        minutePicker.setValue(minute);
    }

    private void displayDayViewAsActive(TextView dayTextView) {
        dayTextView.setTextColor(getResources().getColor(R.color.enabled, null));
        dayTextView.setBackground(getResources().getDrawable(R.drawable.round_day_toggle_enabled, null));
    }

    private void displayDayViewAsInactive(TextView dayTextView) {
        dayTextView.setTextColor(getResources().getColor(R.color.disabled, null));
        dayTextView.setBackground(getResources().getDrawable(R.drawable.round_day_toggle_disabled, null));
    }
}
