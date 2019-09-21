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
    private final String DISPLAY_DAYS_TAG = "displayDays";
    private final String HOUR_TAG = "hour";
    private final String MINUTE_TAG = "minute";

    @BindView(R.id.hour_picker)
    NumberPicker hour;

    @BindView(R.id.minute_picker)
    NumberPicker minute;

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
        hour.setMinValue(0);
        hour.setMaxValue(23);
        minute.setMinValue(0);
        minute.setMaxValue(59);
        if (savedInstanceState != null) {
            displayDays = savedInstanceState.getBoolean(DISPLAY_DAYS_TAG);
            hour.setValue(savedInstanceState.getInt(HOUR_TAG));
            minute.setValue(savedInstanceState.getInt(MINUTE_TAG));
        } else {
            hour.setValue(12);
            minute.setValue(0);
        }
        if (!displayDays) {
            days.setVisibility(View.GONE);
            exactDate.setVisibility(View.VISIBLE);
            updateTypeSwitch.setImageDrawable(getResources().getDrawable(R.drawable.repeat, null));
        }
        updateTypeSwitch.setOnClickListener(v -> {
            if (displayDays) {
                days.setVisibility(View.GONE);
                exactDate.setVisibility(View.VISIBLE);
                updateTypeSwitch.setImageDrawable(getResources().getDrawable(R.drawable.repeat, null));
            } else {
                exactDate.setVisibility(View.GONE);
                days.setVisibility(View.VISIBLE);
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
        outState.putInt(HOUR_TAG, hour.getValue());
        outState.putInt(MINUTE_TAG, minute.getValue());
    }
}
