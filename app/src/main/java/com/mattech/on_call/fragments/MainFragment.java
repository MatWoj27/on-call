package com.mattech.on_call.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mattech.on_call.R;
import com.mattech.on_call.models.OnCallPerson;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainFragment extends Fragment {
    private ActionPerformedListener listener;
    private OnCallPerson onCallPerson;

    @BindView(R.id.on_call_person_name)
    TextView onCallPersonName;

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
}
