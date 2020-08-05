package com.mattech.on_call.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mattech.on_call.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HeaderFragment extends Fragment implements DialogInterface.OnDismissListener {
    private static final int TARGET_FRAGMENT_REQUEST_CODE = 0;
    private boolean clickEnabled = true;

    @BindView(R.id.settings_btn)
    ImageView settings;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_header, container, false);
        ButterKnife.bind(this, view);
        settings.setOnClickListener(v -> {
            synchronized (this) {
                if (clickEnabled) {
                    SettingsDialogFragment fragment = new SettingsDialogFragment();
                    fragment.setTargetFragment(this, TARGET_FRAGMENT_REQUEST_CODE);
                    fragment.show(requireActivity().getSupportFragmentManager(), "settings");
                    clickEnabled = false;
                }
            }
        });
        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        clickEnabled = true;
    }
}
