package com.mattech.on_call.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.mattech.on_call.R;
import com.mattech.on_call.models.Update;
import com.mattech.on_call.utils.DateTimeUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private final TimeTickListener timeTickListener = new TimeTickListener();
    @SuppressLint("DefaultLocale")
    private final NumberPicker.Formatter timePickerFormatter = i -> String.format("%02d", i);
    private final SimpleDateFormat exactDateFormat = new SimpleDateFormat(Update.DATE_FORMAT, Locale.getDefault());
    private ArrayList<String> phoneNumberList = new ArrayList<>();
    private TextView[] dayViews;
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

    @BindView(R.id.reactor_phone_number)
    AutoCompleteTextView phoneNumber;

    @BindView(R.id.cancel_btn)
    Button cancelBtn;

    @BindView(R.id.ok_btn)
    Button okBtn;

    public interface OnFragmentInteractionListener {
        void updateCreated(@NonNull Update update);

        void updateEdited(@NonNull Update update);

        void windowDisappeared();
    }

    private class TimeTickListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Date date = getDateFromUserInput();
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                if (DateTimeUtil.isMomentInPast(date)) {
                    initiallyDateSetToToday = true;
                    currentlyDateSetToToday = false;
                    changeExactDate(Day.TOMORROW);
                } else if (hour == 0 && minute == 0 && DateTimeUtil.isMomentToday(date)) {
                    initiallyDateSetToToday = true;
                    currentlyDateSetToToday = true;
                }
            } catch (ParseException e) {
                Log.e(getClass().getSimpleName(), "Could not check if the update should be rescheduled to tomorrow because parsing user input to date failed", e);
            }
        }
    }

    private enum Day {
        TODAY(0),
        TOMORROW(1);

        int shift;

        Day(int shift) {
            this.shift = shift;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_update, null);
        ButterKnife.bind(this, view);
        if (savedInstanceState != null) {
            readStateFromBundle(savedInstanceState);
        } else if (updateToEdit != null) {
            presetEditUpdateDialog();
        } else {
            presetCreateUpdateDialog();
        }
        presetPhoneNumberAutoCompletion();
        presetDatePicker();
        if (!displayDays) {
            displayExactDateLayout();
        }
        presetDayViews();
        updateTypeSwitch.setOnClickListener(v -> onUpdateTypeChanged());
        okBtn.setOnClickListener(v -> onOkClick());
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
        if (!displayDays) {
            getContext().unregisterReceiver(timeTickListener);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int resultCode : grantResults) {
            if (resultCode != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        readContacts();
    }

    private void onOkClick() {
        if (listener != null) {
            if (isEdit) {
                listener.updateEdited(createUpdateFromInput());
            } else {
                listener.updateCreated(createUpdateFromInput());
            }
        }
        dismiss();
    }

    private void onUpdateTypeChanged() {
        if (displayDays) {
            displayExactDateLayout();
        } else {
            getContext().unregisterReceiver(timeTickListener);
            boolean isAnyActiveDay = false;
            exactDateView.setVisibility(View.GONE);
            days.setVisibility(View.VISIBLE);
            for (int i = 0; i < activeDays.length && i < dayViews.length; i++) {
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
    }

    @NonNull
    private Update createUpdateFromInput() {
        Update update = new Update();
        if (isEdit) {
            update.setId(editUpdateId);
        }
        update.setEnabled(true);
        update.setOneTimeUpdate(!displayDays);
        update.setTime(hourPicker.getValue() + ":" + minutePicker.getValue());
        update.setExactDate(exactDateView.getText().toString());
        update.setRepetitionDays(activeDays);
        update.setPreconfiguredPhoneNumber(phoneNumber.getText().toString());
        return update;
    }

    private class DayClickListener implements View.OnClickListener {
        private int index;

        DayClickListener(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View view) {
            activeDays[index] = !activeDays[index];
            if (activeDays[index]) {
                displayDayViewAsActive((TextView) view);
            } else {
                displayDayViewAsInactive((TextView) view);
                if (!isAnyDayActive()) {
                    displayDays = false;
                    displayExactDateLayout();
                }
            }
        }
    }

    private boolean isAnyDayActive() {
        for (boolean activeDay : activeDays) {
            if (activeDay) {
                return true;
            }
        }
        return false;
    }

    private void presetPhoneNumberAutoCompletion() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_CONTACTS}, 100);
        } else {
            readContacts();
        }
    }

    private void readContacts() {
        ContentResolver contentResolver = requireContext().getContentResolver();
        Cursor contactCursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (contactCursor != null && contactCursor.moveToFirst()) {
            do {
                if (contactCursor.getInt(contactCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    String id = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID));
                    Cursor phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                            new String[]{id},
                            null);
                    if (phoneCursor != null && phoneCursor.moveToFirst()) {
                        do {
                            phoneNumberList.add(phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                        } while (phoneCursor.moveToNext());
                        phoneCursor.close();
                    }
                }
            } while (contactCursor.moveToNext());
            contactCursor.close();
            ArrayAdapter<String> phoneNumberAdapter = new ArrayAdapter<>(requireContext(), R.layout.simple_text_item, phoneNumberList);
            phoneNumber.setAdapter(phoneNumberAdapter);
        }
    }

    private void presetTimePickers(int hour, int minute) {
        presetHourPicker(hour);
        presetMinutePicker(minute);
    }

    private void presetHourPicker(int hour) {
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(23);
        hourPicker.setValue(hour);
        hourPicker.setFormatter(timePickerFormatter);
        hourPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (initiallyDateSetToToday) {
                int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                if (currentlyDateSetToToday && newVal < currentHour) {
                    currentlyDateSetToToday = false;
                    changeExactDate(Day.TOMORROW);
                } else if (newVal == currentHour) {
                    int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);
                    if (currentlyDateSetToToday && minutePicker.getValue() <= currentMinute) {
                        currentlyDateSetToToday = false;
                        changeExactDate(Day.TOMORROW);
                    } else if (!currentlyDateSetToToday && minutePicker.getValue() > currentMinute) {
                        currentlyDateSetToToday = true;
                        changeExactDate(Day.TODAY);
                    }
                } else if (!currentlyDateSetToToday && newVal > currentHour) {
                    currentlyDateSetToToday = true;
                    changeExactDate(Day.TODAY);
                }
            }
        });
    }

    private void presetMinutePicker(int minute) {
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        minutePicker.setValue(minute);
        minutePicker.setFormatter(timePickerFormatter);
        minutePicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (initiallyDateSetToToday && hourPicker.getValue() == Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);
                if (currentlyDateSetToToday && newVal <= currentMinute) {
                    currentlyDateSetToToday = false;
                    changeExactDate(Day.TOMORROW);
                } else if (!currentlyDateSetToToday && newVal > currentMinute) {
                    currentlyDateSetToToday = true;
                    changeExactDate(Day.TODAY);
                }
            }
        });
    }

    private void presetDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (exactDate != null) {
            exactDateView.setText(exactDate);
        } else {
            if ((calendar.get(Calendar.HOUR_OF_DAY) > hourPicker.getValue())
                    || ((calendar.get(Calendar.HOUR_OF_DAY) == hourPicker.getValue()
                    && calendar.get(Calendar.MINUTE) >= minutePicker.getValue()))) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                currentlyDateSetToToday = false;
            }
            exactDateView.setText(exactDateFormat.format(calendar.getTime()));
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
                Toast.makeText(getContext(), getString(R.string.past_update_rescheduled_warning), Toast.LENGTH_SHORT).show();
            } else if (calendar.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR) && calendar.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
                initiallyDateSetToToday = true;
                currentlyDateSetToToday = true;
            } else {
                initiallyDateSetToToday = false;
                currentlyDateSetToToday = false;
            }
            exactDateView.setText(exactDateFormat.format(calendar.getTime()));
        };
        exactDateView.setOnClickListener(v -> {
            try {
                Date date = exactDateFormat.parse(exactDateView.getText().toString());
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                DatePickerDialog dialog = new DatePickerDialog(getContext(), dateSetListener,
                        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                dialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
                dialog.show();
            } catch (ParseException e) {
                Log.e(getClass().getSimpleName(), "Could not display date picker because parsing currently set exact date failed", e);
            }
        });
    }

    private void presetDayViews() {
        dayViews = new TextView[]{monday, tuesday, wednesday, thursday, friday, saturday, sunday};
        for (int i = 0; i < dayViews.length && i < activeDays.length; i++) {
            dayViews[i].setOnClickListener(new DayClickListener(i));
            if (displayDays && activeDays[i]) {
                displayDayViewAsActive(dayViews[i]);
            }
        }
    }

    private void displayDayViewAsActive(@NonNull TextView dayTextView) {
        dayTextView.setTextColor(getResources().getColor(R.color.enabledActive, null));
        dayTextView.setBackground(getResources().getDrawable(R.drawable.round_day_toggle_enabled, null));
    }

    private void displayDayViewAsInactive(@NonNull TextView dayTextView) {
        dayTextView.setTextColor(getResources().getColor(R.color.disabledInactive, null));
        dayTextView.setBackground(getResources().getDrawable(R.drawable.round_day_toggle_disabled_inactive, null));
    }

    private void displayExactDateLayout() {
        try {
            Date date = getDateFromUserInput();
            if (DateTimeUtil.isMomentInPast(date)) {
                initiallyDateSetToToday = true;
                changeExactDate(Day.TOMORROW);
                currentlyDateSetToToday = false;
            } else if (!currentlyDateSetToToday && DateTimeUtil.isMomentToday(date)) {
                initiallyDateSetToToday = true;
                currentlyDateSetToToday = true;
            }
        } catch (ParseException e) {
            Log.e(getClass().getSimpleName(), "Could not check if the exact date is not set to a moment in the past because parsing user input to date failed", e);
        }
        requireContext().registerReceiver(timeTickListener, new IntentFilter(Intent.ACTION_TIME_TICK));
        days.setVisibility(View.GONE);
        exactDateView.setVisibility(View.VISIBLE);
        updateTypeSwitch.setImageDrawable(getResources().getDrawable(R.drawable.repeat, null));
    }

    @SuppressLint("DefaultLocale")
    private Date getDateFromUserInput() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH mm " + Update.DATE_FORMAT, Locale.getDefault());
        String userInput = String.format("%d %d %s", hourPicker.getValue(), minutePicker.getValue(), exactDateView.getText());
        return dateFormat.parse(userInput);
    }

    private void changeExactDate(@NonNull Day dayToSet) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, dayToSet.shift);
        exactDateView.setText(exactDateFormat.format(calendar.getTime()));
    }

    private void readStateFromBundle(@NonNull Bundle bundle) {
        displayDays = bundle.getBoolean(DISPLAY_DAYS_TAG);
        presetTimePickers(bundle.getInt(HOUR_TAG), bundle.getInt(MINUTE_TAG));
        activeDays = bundle.getBooleanArray(ACTIVE_DAYS_TAG);
        exactDate = bundle.getString(EXACT_DATE_TAG);
        isEdit = bundle.getBoolean(IS_EDIT_TAG);
        editUpdateId = bundle.getInt(EDIT_UPDATE_ID_TAG);
        initiallyDateSetToToday = bundle.getBoolean(INIT_DATE_SET_TO_TODAY_TAG);
        currentlyDateSetToToday = bundle.getBoolean(CURR_DATE_SET_TO_TODAY_TAG);
    }

    private void presetEditUpdateDialog() {
        displayDays = !updateToEdit.isOneTimeUpdate();
        activeDays = Arrays.copyOf(updateToEdit.getRepetitionDays(), updateToEdit.getRepetitionDays().length);
        exactDate = updateToEdit.getExactDate();
        phoneNumber.setText(updateToEdit.getPreconfiguredPhoneNumber());
        try {
            presetTimePickers(updateToEdit.get(Update.TIME.HOUR), updateToEdit.get(Update.TIME.MINUTE));
        } catch (ParseException e) {
            Log.e(getClass().getSimpleName(), "Time string retrieved from Update object has wrong format: " + updateToEdit.getTime());
            presetTimePickers(12, 0);
        }
    }

    private void presetCreateUpdateDialog() {
        int tomorrowIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1; // In Calendar API Sunday is the first day and days are indexed from 1 to 7
        activeDays[tomorrowIndex] = true;
        presetTimePickers(12, 0);
    }

    public void setUpdateToEdit(@NonNull Update updateToEdit) {
        this.updateToEdit = updateToEdit;
        isEdit = true;
        editUpdateId = updateToEdit.getId();
    }

    public void setListener(OnFragmentInteractionListener listener) {
        this.listener = listener;
    }
}
