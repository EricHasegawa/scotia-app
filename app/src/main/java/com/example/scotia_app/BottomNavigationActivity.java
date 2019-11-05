package com.example.scotia_app;

import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);

        // Retrieves the current user, passed from the MainActivity
        this.user = getIntent().getParcelableExtra("user");

        setNavGraph();
    }

    /**
     * Helper method for onCreate which sets the navigation controller's nav graph
     */
    private void setNavGraph() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        NavInflater navInflater = navController.getNavInflater();
        NavGraph navGraph = navInflater.inflate(R.navigation.mobile_navigation);

        passUserToAllFragments(navGraph, navController);

        navGraph.setStartDestination(R.id.navigation_notifications);
        navController.setGraph(navGraph);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_invoices, R.id.navigation_notifications, R.id.navigation_profile)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    /**
     * Helper method to setNavGraph which passes the current user to all the fragments and also
     * passes the user again whenever the destination is changed since the selected fragment is
     * recreated on each destination change
     */
    private void passUserToAllFragments(NavGraph navGraph, final NavController navController) {
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