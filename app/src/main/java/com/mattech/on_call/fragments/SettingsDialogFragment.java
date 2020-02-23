package com.mattech.on_call.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mattech.on_call.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsDialogFragment extends DialogFragment {

    @BindView(R.id.web_service_ip)
    EditText webServiceIp;

    @BindView(R.id.web_service_port)
    EditText webServicePort;

    @BindView(R.id.team_name)
    EditText teamName;

    @BindView(R.id.save_btn)
    Button saveBtn;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_settings, null);
        ButterKnife.bind(this, view);
        saveBtn.setOnClickListener(v -> dismiss());
        builder.setView(view);
        return builder.create();
    }
}
