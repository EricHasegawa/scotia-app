package com.example.scotia_app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import com.example.scotia_app.R;
import com.example.scotia_app.data.model.User;
import com.example.scotia_app.database.OutgoingRequest;
import com.example.scotia_app.ui.invoices.DetailedInvoiceActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavArgument;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class BottomNavigationActivity extends AppCompatActivity {

    private User user;

    private BroadcastReceiver notificationTokenUpdatedHandler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String url = intent.getStringExtra("url");
            new OutgoingRequest(BottomNavigationActivity.this).execute(url);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);

        // Retrieves the current user, passed from the MainActivity
        this.user = getIntent().getParcelableExtra("user");

        LocalBroadcastManager.getInstance(this).registerReceiver(notificationTokenUpdatedHandler,
                new IntentFilter(getString(R.string.notification_token_updated)));

        setNavGraph();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationTokenUpdatedHandler);
    }

    /**
     * Helper method for onCreate which sets the navigation controller's nav graph
     */
    private void setNavGraph() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        NavInflater navInflater = navController.getNavInflater();
        NavGraph navGraph = navInflater.inflate(R.navigation.mobile_navigation);

        passUserToFragments(navGraph, navController);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_invoices, R.id.navigation_notifications, R.id.navigation_profile)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    /**
     * Helper method to setNavGraph which passes the current user to all the fragments for the
     * initial load and then every time the fragment is changed
     */
    private void passUserToFragments(NavGraph navGraph, NavController navController) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);

        setPassUserOnChange(navGraph, navController);

        navGraph.setStartDestination(R.id.navigation_profile);
        navController.setGraph(navGraph, bundle);
        navGraph.setStartDestination(R.id.navigation_invoices);
        navController.setGraph(navGraph, bundle);
        navGraph.setStartDestination(R.id.navigation_notifications);
        navController.setGraph(navGraph, bundle);
    }

    /**
     * Helper method to setNavGraph which passes the current user to all the fragments whenever the
     * destination is changed since the selected fragment is recreated on each destination change
     */
    private void setPassUserOnChange(NavGraph navGraph, final NavController navController) {
        final NavArgument userArgument = new NavArgument.Builder().setDefaultValue(user).build();
        navGraph.addArgument("user", userArgument);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                destination.addArgument("user", userArgument);
            }
        });
    }

    public void logout(View view) {
        Intent login = new Intent(this, LoginActivity.class);
        startActivity(login);
        FirebaseAuth.getInstance().signOut();
        finish();
    }

    public void changeNameDialog(View view) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Please enter your new display name: ");

        final EditText input = new EditText(view.getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        final Activity context = this;

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString();
                newName = newName.replaceAll(" " , "%20");
                new OutgoingRequest(context).execute("https://us-central1-scotiabank-app.cloudfunctions.net/update-username?id=" + firebaseUser.getUid() + "username=" + newName);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void sendInvoice(View view) {
        new OutgoingRequest(this).execute("https://us-central1-scotiabank-app.cloudfunctions.net/generate-random-invoice");
    }

}