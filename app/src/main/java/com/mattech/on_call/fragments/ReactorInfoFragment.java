package com.mattech.on_call.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mattech.on_call.R;
import com.mattech.on_call.databinding.FragmentReactorInfoBinding;
import com.mattech.on_call.view_models.ReactorViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReactorInfoFragment extends Fragment implements DialogInterface.OnDismissListener {
    private static final int TARGET_FRAGMENT_REQUEST_CODE = 0;
    private ReactorViewModel viewModel;
    private boolean clickEnabled = true;

    private FragmentReactorInfoBinding binding;

    @BindView(R.id.update_now_btn)
    ImageButton updateNowBtn;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_reactor_info, container, false);
        binding.setLifecycleOwner(this);
        View view = binding.getRoot();
        ButterKnife.bind(this, view);
        updateNowBtn.setOnClickListener(v -> {
            if (webApiPreferencesSet()) {
                viewModel.updateReactor();
            } else {
                showSettingsFragment();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(ReactorViewModel.class);
        binding.setViewModel(viewModel);
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        clickEnabled = true;
    }

    private boolean webApiPreferencesSet() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(SettingsDialogFragment.WEB_API_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String currentValue = sharedPreferences.getString(SettingsDialogFragment.WEB_API_IP_PREFERENCE_KEY, "");
        return !currentValue.isEmpty();
    }

    private synchronized void showSettingsFragment() {
        if (clickEnabled) {
            SettingsDialogFragment fragment = new SettingsDialogFragment();
            fragment.setTargetFragment(this, TARGET_FRAGMENT_REQUEST_CODE);
            fragment.show(requireActivity().getSupportFragmentManager(), "settings");
            Toast.makeText(getContext(), getString(R.string.web_service_ip_not_configured_warning), Toast.LENGTH_SHORT).show();
            clickEnabled = false;
        }
    }
}
