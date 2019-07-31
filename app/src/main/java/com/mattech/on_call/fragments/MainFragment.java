package com.mattech.on_call.fragments;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mattech.on_call.OnCallPersonViewModel;
import com.mattech.on_call.R;
import com.mattech.on_call.models.OnCallPerson;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainFragment extends Fragment {
    private ActionPerformedListener listener;
    private OnCallPersonViewModel viewModel;

    @BindView(R.id.on_call_person_name)
    TextView onCallPersonName;

    @BindView(R.id.on_call_person_phone_num)
    TextView onCallPersonPhoneNumber;

    @BindView(R.id.on_call_person_mail)
    TextView onCallPersonMail;

    @BindView(R.id.start)
    Button startBtn;

    @BindView(R.id.settings)
    Button settingsBtn;

    public interface ActionPerformedListener {
        void startForwarding();
        void goToSettings();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ActionPerformedListener) {
            listener = (ActionPerformedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " has to implement MainFragment.ActionPerformedListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(OnCallPersonViewModel.class);
        viewModel.getOnCallPersonLiveData().observe(this, this::updateUI);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);
        settingsBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.goToSettings();
            }
        });
        startBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.startForwarding();
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (viewModel.getOnCallPersonLiveData().getValue() != null) {
            updateUI(viewModel.getOnCallPersonLiveData().getValue());
        }
    }

    private void updateUI(OnCallPerson onCallPerson) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            onCallPersonName.setText(onCallPerson.getName());
            onCallPersonPhoneNumber.setText(onCallPerson.getPhoneNumber());
            onCallPersonMail.setText(onCallPerson.getMail());
        }
    }
}
