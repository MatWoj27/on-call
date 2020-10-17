package com.mattech.on_call.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mattech.on_call.Constants;
import com.mattech.on_call.R;
import com.mattech.on_call.databinding.DialogSettingsBinding;
import com.mattech.on_call.models.WebApiSettings;
import com.mattech.on_call.utils.WebUtil;
import com.mattech.on_call.view_models.SettingsViewModel;

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
        DialogSettingsBinding binding = DialogSettingsBinding.bind(view);
        SettingsViewModel viewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        viewModel.setSettings(WebApiSettings.getCurrentSettings(requireContext()));
        binding.setViewModel(viewModel);
        ButterKnife.bind(this, view);
        saveBtn.setOnClickListener(v -> {
            if (WebUtil.isValidIPv4(webServiceIp.getText().toString())) {
                updatePreferenceIfChanged(Constants.WEB_API_IP_PREFERENCE_KEY, WebUtil.removeIPv4LeadingZeros(webServiceIp.getText().toString()), false);
            }
            if (WebUtil.isValidPortNumber(webServicePort.getText().toString())) {
                updatePreferenceIfChanged(Constants.WEB_API_PORT_PREFERENCE_KEY, WebUtil.removeLeadingZeros(webServicePort.getText().toString()), true);
            }
            updatePreferenceIfChanged(Constants.WEB_API_TEAM_PREFERENCE_KEY, teamName.getText().toString(), true);
            dismiss();
        });
        builder.setView(view);
        return builder.create();
    }

    private void updatePreferenceIfChanged(String key, @NonNull String newValue, boolean emptyAllowed) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(Constants.WEB_API_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String currentValue = sharedPreferences.getString(key, "");
        if (newValue.trim().isEmpty() == emptyAllowed && !currentValue.equals(newValue)) {
            sharedPreferences.edit().putString(key, newValue.trim()).apply();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getTargetFragment() instanceof DialogInterface.OnDismissListener) {
            DialogInterface.OnDismissListener listener = (DialogInterface.OnDismissListener) getTargetFragment();
            listener.onDismiss(dialog);
        }
    }
}
