package com.mattech.on_call;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mattech.on_call.adapters.UpdatesAdapter;
import com.mattech.on_call.models.OnCallPerson;
import com.mattech.on_call.models.Update;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements UpdatesAdapter.AddUpdateListener {
    public static final int REQUEST_CALL_PERMISSION_CODE = 1;
    private OnCallPersonViewModel viewModel;

    @BindView(R.id.info_frame)
    LinearLayout infoFrame;

    @BindView(R.id.on_call_person_name)
    TextView onCallPersonName;

    @BindView(R.id.on_call_person_phone_num)
    TextView onCallPersonPhoneNumber;

    @BindView(R.id.on_call_person_mail)
    TextView onCallPersonMail;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        viewModel = ViewModelProviders.of(this).get(OnCallPersonViewModel.class);
        viewModel.getOnCallPerson().observe(this, this::updateUI);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        UpdatesAdapter adapter = new UpdatesAdapter();
        // mocked updates to be changed later
        List<Update> mockedUpdates = new ArrayList<>();
        mockedUpdates.add(new Update(true, "Mon", "10:00", true));
        mockedUpdates.add(new Update(false, "Tue", "11:00", true));
        mockedUpdates.add(new Update(true, "Fri", "12:00", false));
        adapter.setUpdates(mockedUpdates);
        adapter.setListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CALL_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startForwarding();
            } else {
                Toast.makeText(this, "Setting forwarding is not possible without call permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void addUpdate() {
        Toast.makeText(this, "Adding a new update", Toast.LENGTH_SHORT).show();
    }

    public void startForwarding() {
//        if (onCallPerson != null) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                String[] permissions = new String[]{Manifest.permission.CALL_PHONE};
//                requestPermissions(permissions, REQUEST_CALL_PERMISSION_CODE);
//            } else {
//                Intent callForwardingIntent = new Intent(Intent.ACTION_CALL);
//                String callForwardingString = String.format("*21*%s#", String.valueOf(onCallPerson.getPhoneNumber()));
//                Uri gsmCode = Uri.fromParts("tel", callForwardingString, "#");
//                callForwardingIntent.setData(gsmCode);
//                startActivity(callForwardingIntent);
//            }
//        }
    }

    private void updateUI(OnCallPerson onCallPerson) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            onCallPersonName.setText(onCallPerson.getName());
            onCallPersonPhoneNumber.setText(onCallPerson.getPhoneNumber());
            onCallPersonMail.setText(onCallPerson.getMail());
        }
    }
}
