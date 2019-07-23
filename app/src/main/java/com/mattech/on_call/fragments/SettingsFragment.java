package com.mattech.on_call.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mattech.on_call.R;
import com.mattech.on_call.models.OnCallPerson;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsFragment extends Fragment {
    private ActionPerformedListener listener;

    @BindView(R.id.back)
    Button backBtn;

    public interface ActionPerformedListener {
        void goToMain();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ActionPerformedListener) {
            listener = (ActionPerformedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " has to implement SettingsFragment.ActionPerformedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);
        backBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.goToMain();
            }
        });
        return view;
    }

}
