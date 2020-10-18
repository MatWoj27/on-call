package com.mattech.on_call.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;

import com.mattech.on_call.R;
import com.mattech.on_call.databinding.DialogSettingsBinding;
import com.mattech.on_call.models.WebApiSettings;
import com.mattech.on_call.view_models.SettingsViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsDialogFragment extends DialogFragment {

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
            viewModel.updateSettings(requireContext());
            dismiss();
        });
        builder.setView(view);
        return builder.create();
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
