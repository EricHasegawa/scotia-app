package com.example.scotia_app.ui.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.scotia_app.R;
import com.example.scotia_app.data.model.Persona;
import com.example.scotia_app.data.model.User;
import com.example.scotia_app.database.OutgoingRequest;
import com.example.scotia_app.ui.LoginActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.internal.api.FirebaseNoSignedInUserException;

import java.util.Objects;

public class SettingsFragment extends Fragment {

    private User user;

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
        final View root = inflater.inflate(R.layout.fragment_settings, container, false);

        setUser(root);

        Button logout = root.findViewById(R.id.logout_button);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        final Button changeName = root.findViewById(R.id.change_name);
        changeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeNameDialog(root);
            }
        });

        final Button generateRandomInvoice = root.findViewById(R.id.generate_random_invoice);
        generateRandomInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendInvoice();
            }
        });

        final Button changePassword = root.findViewById(R.id.change_password);
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassword(root);
            }
        });

        return root;
    }

    private void setUser(View root) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            user = bundle.getParcelable("user");
            if (user != null && user.getPersona() != Persona.customer) {
                Button button = root.findViewById(R.id.generate_random_invoice);
                button.setVisibility(View.GONE);
            }
        }
    }

    private void logout() {
        Intent login = new Intent(getActivity(), LoginActivity.class);
        startActivity(login);
        FirebaseAuth.getInstance().signOut();
        Objects.requireNonNull(getActivity()).finish();
    }

    private void changeNameDialog(View view) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Please enter your new display name: ");

        final EditText input = new EditText(view.getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        final Activity context = getActivity();

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString();
                newName = newName.replaceAll(" ", "%20");
                if (firebaseUser != null) {
                    new OutgoingRequest(context).execute("https://us-central1-scotiabank-app.cloudfunctions.net/update-username?id=" + firebaseUser.getUid() + "&name=" + newName);
                } else {
                    new FirebaseNoSignedInUserException("No Firebase user signed in").printStackTrace();
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void sendInvoice() {
        new OutgoingRequest(getActivity()).execute("https://us-central1-scotiabank-app.cloudfunctions.net/generate-random-invoice?id=" + user.getId());
    }

    private void changePassword(View view) {
        String email = null;
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null)
            email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (email == null)
            email = "";
        FirebaseAuth.getInstance().sendPasswordResetEmail(email);

        Snackbar.make(view, "You have been sent a password reset email!",
                Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }
}