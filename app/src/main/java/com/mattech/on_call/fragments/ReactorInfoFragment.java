package com.mattech.on_call.fragments;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mattech.on_call.R;
import com.mattech.on_call.ReactorViewModel;
import com.mattech.on_call.models.Reactor;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReactorInfoFragment extends Fragment {
    private ReactorViewModel viewModel;

    @BindView(R.id.reactor_name)
    TextView reactorName;

    @BindView(R.id.reactor_phone_num)
    TextView reactorPhoneNumber;

    @BindView(R.id.reactor_mail)
    TextView reactorMail;

    @BindView(R.id.update_now_btn)
    ImageButton updateNowBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reactor_info, container, false);
        ButterKnife.bind(this, view);
        updateNowBtn.setOnClickListener(v -> viewModel.updateReactor());
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(ReactorViewModel.class);
        viewModel.getReactor().observe(getActivity(), this::updateUI);
    }

    private void updateUI(Reactor reactor) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED) && reactor != null) {
            reactorName.setText(reactor.getName());
            reactorPhoneNumber.setText(reactor.getPhoneNumber());
            reactorMail.setText(reactor.getMail());
        }
    }
}
