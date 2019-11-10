package com.mattech.on_call;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.TextView;

import com.mattech.on_call.adapters.UpdatesAdapter;
import com.mattech.on_call.models.Reactor;
import com.mattech.on_call.models.Update;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements UpdatesAdapter.UpdateListener, UpdateDialogFragment.OnFragmentInteractionListener {
    private ReactorViewModel viewModel;

    @BindView(R.id.reactor_name)
    TextView reactorName;

    @BindView(R.id.reactor_phone_num)
    TextView reactorPhoneNumber;

    @BindView(R.id.reactor_mail)
    TextView reactorMail;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        viewModel = ViewModelProviders.of(this).get(ReactorViewModel.class);
        viewModel.getReactor().observe(this, this::updateUI);
        UpdatesAdapter adapter = new UpdatesAdapter(this);
        viewModel.getUpdates().observe(this, adapter::setUpdates);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        adapter.setListener(this);
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
    }

    @Override
    public void addUpdate() {
        UpdateDialogFragment fragment = new UpdateDialogFragment();
        fragment.setStyle(R.style.CardViewTheme, R.style.CardViewTheme);
        fragment.show(getSupportFragmentManager(), "add_update");
    }

    @Override
    public void editUpdate(Update updateToEdit) {
        UpdateDialogFragment fragment = new UpdateDialogFragment();
        fragment.setUpdateToEdit(updateToEdit);
        fragment.setStyle(R.style.CardViewTheme, R.style.CardViewTheme);
        fragment.show(getSupportFragmentManager(), "edit_update");
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

    private void updateUI(Reactor reactor) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED) && reactor != null) {
            reactorName.setText(reactor.getName());
            reactorPhoneNumber.setText(reactor.getPhoneNumber());
            reactorMail.setText(reactor.getMail());
        }
    }
}
