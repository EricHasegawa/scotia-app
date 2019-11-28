package com.example.scotia_app.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.example.scotia_app.R;
import com.example.scotia_app.data.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
    private BroadcastReceiver notificationHandler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(message);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);

        // Retrieves the current user, passed from the MainActivity
        this.user = getIntent().getParcelableExtra("user");

        // Registers notification handler to enable receiving notifications when app is in foreground
//        LocalBroadcastManager.getInstance(this).registerReceiver(notificationHandler,
//                new IntentFilter(getString(R.string.notification_intent_filter)));

        setNavGraph();
    }

    @Override
    protected void onPause() {
        super.onPause();

//        Unregisters notification handler to prevent memory leaks
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationHandler);
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
}