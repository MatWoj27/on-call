package com.mattech.on_call.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.mattech.on_call.R;
import com.mattech.on_call.models.Update;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UpdateDialogFragment extends DialogFragment {
    private OnFragmentInteractionListener listener;
    private Update updateToEdit;
    private boolean isEdit = false;
    private boolean displayDays = true;
    private int editUpdateId;
    private boolean[] activeDays = new boolean[7];
    private String exactDate;
    private boolean initiallyDateSetToToday = true;
    private boolean currentlyDateSetToToday = true;
    private final String DISPLAY_DAYS_TAG = "displayDays";
    private final String HOUR_TAG = "hour";
    private final String MINUTE_TAG = "minute";
    private final String ACTIVE_DAYS_TAG = "activeDays";
    private final String EXACT_DATE_TAG = "exactDate";
    private final String IS_EDIT_TAG = "isEdit";
    private final String EDIT_UPDATE_ID_TAG = "editUpdateID";
    private final String INIT_DATE_SET_TO_TODAY_TAG = "initiallyDateSetToToday";
    private final String CURR_DATE_SET_TO_TODAY_TAG = "currentlyDateSetToToday";

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
    TextView exactDateView;

    @BindView(R.id.update_type_switch)
    ImageView updateTypeSwitch;

    @BindView(R.id.cancel_btn)
    Button cancelBtn;

    @BindView(R.id.ok_btn)
    Button okBtn;

    public interface OnFragmentInteractionListener {
        void updateCreated(Update update);

        void updateEdited(Update update);

        void windowDisappeared();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_update, null);
        ButterKnife.bind(this, view);
        TextView[] dayViews = {monday, tuesday, wednesday, thursday, friday, saturday, sunday};
        if (savedInstanceState != null) {
            displayDays = savedInstanceState.getBoolean(DISPLAY_DAYS_TAG);
            presetTimePickers(savedInstanceState.getInt(HOUR_TAG), savedInstanceState.getInt(MINUTE_TAG));
            activeDays = savedInstanceState.getBooleanArray(ACTIVE_DAYS_TAG);
            exactDate = savedInstanceState.getString(EXACT_DATE_TAG);
            isEdit = savedInstanceState.getBoolean(IS_EDIT_TAG);
            editUpdateId = savedInstanceState.getInt(EDIT_UPDATE_ID_TAG);
            initiallyDateSetToToday = savedInstanceState.getBoolean(INIT_DATE_SET_TO_TODAY_TAG);
            currentlyDateSetToToday = savedInstanceState.getBoolean(CURR_DATE_SET_TO_TODAY_TAG);
        } else if (updateToEdit != null) {
            displayDays = !updateToEdit.isOneTimeUpdate();
            activeDays = Arrays.copyOf(updateToEdit.getRepetitionDays(), updateToEdit.getRepetitionDays().length);
            exactDate = updateToEdit.getExactDate();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            try {
                Date date = simpleDateFormat.parse(updateToEdit.getTime());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                presetTimePickers(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
            } catch (ParseException e) {
                Log.e(getClass().getSimpleName(), "Time string retrieved from Update object has wrong format: " + updateToEdit.getTime());
                presetTimePickers(12, 0);
            }
        } else {
            int tomorrowIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1; // In Calendar API Sunday is the first day and days are indexed from 1 to 7
            activeDays[tomorrowIndex] = true;
            presetTimePickers(12, 0);
        }
        if (!displayDays) {
            displayExactDateLayout();
        }
        for (int i = 0; i < 7; i++) {
            dayViews[i].setOnClickListener(new DayClickListener(i));
            if (displayDays && activeDays[i]) {
                displayDayViewAsActive(dayViews[i]);
            }
        }
        updateTypeSwitch.setOnClickListener(v -> {
            if (displayDays) {
                displayExactDateLayout();
            } else {
                boolean isAnyActiveDay = false;
                exactDateView.setVisibility(View.GONE);
                days.setVisibility(View.VISIBLE);
                for (int i = 0; i < 7; i++) {
                    if (activeDays[i]) {
                        displayDayViewAsActive(dayViews[i]);
                        isAnyActiveDay = true;
                    }
                }
                if (!isAnyActiveDay) {
                    int tomorrowIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
                    activeDays[tomorrowIndex] = true;
                    displayDayViewAsActive(dayViews[tomorrowIndex]);
                }
                updateTypeSwitch.setImageDrawable(getResources().getDrawable(R.drawable.calendar, null));
            }
            displayDays = !displayDays;
        });
        presetDatePicker();
        okBtn.setOnClickListener(v -> {
            if (listener != null) {
                if (isEdit) {
                    listener.updateEdited(createUpdateFromInput());
                } else {
                    listener.updateCreated(createUpdateFromInput());
                }
                dismiss();
            }
        });
        cancelBtn.setOnClickListener(v -> dismiss());
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (listener != null) {
            listener.windowDisappeared();
            listener = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_EDIT_TAG, isEdit);
        outState.putInt(EDIT_UPDATE_ID_TAG, editUpdateId);
        outState.putBoolean(DISPLAY_DAYS_TAG, displayDays);
        outState.putInt(HOUR_TAG, hourPicker.getValue());
        outState.putInt(MINUTE_TAG, minutePicker.getValue());
        outState.putBooleanArray(ACTIVE_DAYS_TAG, activeDays);
        outState.putString(EXACT_DATE_TAG, exactDateView.getText().toString());
        outState.putBoolean(INIT_DATE_SET_TO_TODAY_TAG, initiallyDateSetToToday);
        outState.putBoolean(CURR_DATE_SET_TO_TODAY_TAG, currentlyDateSetToToday);
    }

    private Update createUpdateFromInput() {
        Update update = new Update();
        if (isEdit) {
            update.setId(editUpdateId);
        }
        update.setEnabled(true);
        update.setOneTimeUpdate(!displayDays);
        update.setTime(String.valueOf(hourPicker.getValue()) + ":" + String.valueOf(minutePicker.getValue()));
        update.setExactDate(exactDateView.getText().toString());
        update.setRepetitionDays(activeDays);
        return update;
    }

    private class DayClickListener implements View.OnClickListener {
        private int index;

        public DayClickListener(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View view) {
            activeDays[index] = !activeDays[index];
            if (activeDays[index]) {
                displayDayViewAsActive((TextView) view);
            } else {
                displayDayViewAsInactive((TextView) view);
                for (int i = 0; i < 7; i++) {
                    if (activeDays[i]) {
                        return;
                    }
                }
                displayDays = false;
                displayExactDateLayout();
            }
        }
    }

    private void presetTimePickers(int hour, int minute) {
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(23);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        hourPicker.setValue(hour);
        minutePicker.setValue(minute);
        NumberPicker.Formatter formatter = i -> String.format("%02d", i);
        hourPicker.setFormatter(formatter);
        minutePicker.setFormatter(formatter);
        hourPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (initiallyDateSetToToday) {
                int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                int amountOfDays = 0;
                if (currentlyDateSetToToday && newVal < currentHour) {
                    currentlyDateSetToToday = false;
                    amountOfDays = 1;
                } else if (newVal == currentHour) {
                    int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);
                    if (currentlyDateSetToToday && minutePicker.getValue() <= currentMinute) {
                        currentlyDateSetToToday = false;
                        amountOfDays = 1;
                    } else if (!currentlyDateSetToToday && minutePicker.getValue() > currentMinute) {
                        currentlyDateSetToToday = true;
                        amountOfDays = -1;
                    }
                } else if (!currentlyDateSetToToday && newVal > currentHour) {
                    currentlyDateSetToToday = true;
                    amountOfDays = -1;
                }
                try {
                    changeExactDateOfDays(amountOfDays);
                } catch (ParseException e) {
                    currentlyDateSetToToday = !currentlyDateSetToToday;
                    e.printStackTrace();
                }
            }
        });
        minutePicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (initiallyDateSetToToday && hourPicker.getValue() == Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);
                int amountOfDays = 0;
                if (currentlyDateSetToToday && newVal <= currentMinute) {
                    currentlyDateSetToToday = false;
                    amountOfDays = 1;
                } else if (!currentlyDateSetToToday && newVal > currentMinute) {
                    currentlyDateSetToToday = true;
                    amountOfDays = -1;
                }
                try {
                    changeExactDateOfDays(amountOfDays);
                } catch (ParseException e) {
                    currentlyDateSetToToday = !currentlyDateSetToToday;
                    e.printStackTrace();
                }
            }
        });
    }

    private void presetDatePicker() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault());
        if (exactDate != null) {
            exactDateView.setText(exactDate);
        } else {
            if ((calendar.get(Calendar.HOUR_OF_DAY) > hourPicker.getValue())
                    || ((calendar.get(Calendar.HOUR_OF_DAY) == hourPicker.getValue()
                    && calendar.get(Calendar.MINUTE) >= minutePicker.getValue()))) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                initiallyDateSetToToday = false;
                currentlyDateSetToToday = false;
            }
            exactDateView.setText(dateFormat.format(calendar.getTime()));
        }
        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, hourPicker.getValue());
            calendar.set(Calendar.MINUTE, minutePicker.getValue());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Calendar cal = Calendar.getInstance();
            if (calendar.getTimeInMillis() < cal.getTimeInMillis()) {
                initiallyDateSetToToday = true;
                currentlyDateSetToToday = false;
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                Toast.makeText(getContext(), getContext().getResources().getString(R.string.past_update_rescheduled_warning), Toast.LENGTH_SHORT).show();
            } else if (calendar.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR) && calendar.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
                initiallyDateSetToToday = true;
                currentlyDateSetToToday = true;
            } else {
                initiallyDateSetToToday = false;
                currentlyDateSetToToday = false;
            }
            exactDateView.setText(dateFormat.format(calendar.getTime()));
        };
        exactDateView.setOnClickListener(v -> {
            try {
                Date date = dateFormat.parse(exactDateView.getText().toString());
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                DatePickerDialog dialog = new DatePickerDialog(getContext(), dateSetListener,
                        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                dialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
                dialog.show();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }

    private void displayDayViewAsActive(TextView dayTextView) {
        dayTextView.setTextColor(getResources().getColor(R.color.enabledActive, null));
        dayTextView.setBackground(getResources().getDrawable(R.drawable.round_day_toggle_enabled, null));
    }

    private void displayDayViewAsInactive(TextView dayTextView) {
        dayTextView.setTextColor(getResources().getColor(R.color.disabledInactive, null));
        dayTextView.setBackground(getResources().getDrawable(R.drawable.round_day_toggle_disabled_inactive, null));
    }

    private void displayExactDateLayout() {
        days.setVisibility(View.GONE);
        exactDateView.setVisibility(View.VISIBLE);
        updateTypeSwitch.setImageDrawable(getResources().getDrawable(R.drawable.repeat, null));
    }

    private void changeExactDateOfDays(int amountOfDays) throws ParseException {
        if (amountOfDays != 0) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault());
            Date date = dateFormat.parse(exactDateView.getText().toString());
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_MONTH, amountOfDays);
            exactDateView.setText(dateFormat.format(calendar.getTime()));
        }
    }

    public void setUpdateToEdit(Update updateToEdit) {
        this.updateToEdit = updateToEdit;
        isEdit = true;
        editUpdateId = updateToEdit.getId();
    }

    public void setListener(OnFragmentInteractionListener listener) {
        this.listener = listener;
    }
}
