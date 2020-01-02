package com.mattech.on_call.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mattech.on_call.R;
import com.mattech.on_call.adapters.UpdatesAdapter;
import com.mattech.on_call.models.Update;
import com.mattech.on_call.view_models.UpdateViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UpdateListFragment extends Fragment implements UpdatesAdapter.UpdateListener, UpdateDialogFragment.OnFragmentInteractionListener {
    private UpdateViewModel viewModel;
    private UpdatesAdapter adapter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new UpdatesAdapter(getActivity());
        adapter.setListener(this);
        adapter.setHasStableIds(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_list, container, true);
        ButterKnife.bind(this, view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                viewModel.deleteUpdate(adapter.getUpdateAt(viewHolder.getAdapterPosition() - 1));
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return viewHolder.getAdapterPosition() == 0 ? 0 : super.getSwipeDirs(recyclerView, viewHolder);
            }
        }).attachToRecyclerView(recyclerView);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(UpdateViewModel.class);
        viewModel.getUpdates().observe(this, adapter::setUpdates);
    }

    @Override
    public void addUpdate() {
        UpdateDialogFragment fragment = new UpdateDialogFragment();
        fragment.setListener(this);
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CardViewTheme);
        fragment.show(getActivity().getSupportFragmentManager(), "add_update");
    }

    @Override
    public void editUpdate(Update updateToEdit) {
        UpdateDialogFragment fragment = new UpdateDialogFragment();
        fragment.setListener(this);
        fragment.setUpdateToEdit(updateToEdit);
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CardViewTheme);
        fragment.show(getActivity().getSupportFragmentManager(), "edit_update");
    }

    @Override
    public void updateEnableStatusChanged(Update update) {
        viewModel.updateEnableStatusChanged(update);
    }

    @Override
    public void updateCreated(Update update) {
        viewModel.addUpdate(update);
    }

    @Override
    public void updateEdited(Update editedUpdate) {
        viewModel.updateUpdate(editedUpdate);
    }

    @Override
    public void windowDisappeared() {
        adapter.setClickEnabled(true);
    }
}
