package com.example.scotia_app.ui.profile;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.scotia_app.R;
import com.example.scotia_app.data.model.Persona;
import com.example.scotia_app.data.model.User;

public class ProfileFragment extends Fragment {

    private BroadcastReceiver notificationHandler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");

            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(title);
            builder.setMessage(message);

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(this.requireActivity()).registerReceiver(notificationHandler,
                new IntentFilter(getString(R.string.notification_received)));
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this.requireActivity()).registerReceiver(notificationHandler,
                new IntentFilter(getString(R.string.notification_received)));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this.requireActivity()).unregisterReceiver(notificationHandler);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        setUser(root);

        return root;
    }

    private void setUser(View root) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            User user = bundle.getParcelable("user");
            if (user != null && user.getPersona() != Persona.customer) {
                Button button = root.findViewById(R.id.generate_random_invoice);
                button.setVisibility(View.GONE);
            }
        }
    }
}