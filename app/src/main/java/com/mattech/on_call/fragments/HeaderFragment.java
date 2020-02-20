package com.mattech.on_call.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mattech.on_call.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HeaderFragment extends Fragment {

    @BindView(R.id.settings_btn)
    ImageView settings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_header, container, false);
        ButterKnife.bind(this, view);
        return view;
    }
}
