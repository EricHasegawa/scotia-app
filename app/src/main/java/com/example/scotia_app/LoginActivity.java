package com.example.scotia_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);

        List<AuthUI.IdpConfig> providers  = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                123);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                new UserFetcher(this).execute("http://us-central1-scotiabank-app.cloudfunctions.net/get-user-by-id?id=" + user.getUid());
                finish();
            } else {
                Toast.makeText(this, "" + response.getError().getMessage(), Toast.LENGTH_LONG).show();
            }
        }

    }

    /**
     * Fetches and parses the attributes of the logged-in User.
     */
    static private class UserFetcher extends DataFetcher {

        /**
         * Initialize a new UserFetcher, which runs in the given context.
         *
         * @param context The context in which this UserFetcher runs.
         */
        UserFetcher(Activity context) {
            super(context);
        }

        /**
         * Returns a JSONObject with which to populate the User.
         *
         * @param rawJson The raw json string to be parsed
         * @return A List of JSONObjects, each corresponding to a raw json string.
         */
        private JSONObject createJSONObject(String rawJson) {
            try {
                return new JSONObject(rawJson);
            } catch (org.json.JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After super.doInBackground is finished executing, populate a user with the retrieved JSON
         *
         * @param rawJsons The list of raw json strings whose first element is the user data
         */
        @Override
        protected void onPostExecute(ArrayList<String> rawJsons) {
            JSONObject userData = createJSONObject(rawJsons.get(0));
            AppCompatActivity context = (AppCompatActivity) super.getActivityWeakReference().get();

            Intent switchToBottomNavigationView = new Intent(context,
                    BottomNavigationActivity.class);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            try {
                switch (userData.getString("persona")) {
                    case "customer":
                        switchToBottomNavigationView.putExtra("user", new Customer(userData));
                        break;
                    case "driver":
                        switchToBottomNavigationView.putExtra("user", new Driver(userData));
                        break;
                    case "supplier":
                        switchToBottomNavigationView.putExtra("user", new Supplier(userData));
                        break;
                    default:
                        switchToBottomNavigationView.putExtra("user", new Customer(userData));
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                switchToBottomNavigationView.putExtra("user", new Customer(user.getDisplayName(), user.getUid()));
            } catch (NullPointerException e) {
                e.printStackTrace();
                switchToBottomNavigationView.putExtra("user", new Customer(user.getDisplayName(), user.getUid()));
            }

            // Starts the loading icon
            ProgressBar spinner = getActivityWeakReference().get().findViewById(R.id.progressBar);
            spinner.setVisibility(View.GONE);
            context.startActivity(switchToBottomNavigationView);
        }
    }

}
