package com.example.scotia_app.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.scotia_app.DataFetcher;
import com.example.scotia_app.NotificationFetcher;
import com.example.scotia_app.R;
import com.example.scotia_app.data.model.Customer;
import com.example.scotia_app.data.model.Driver;
import com.example.scotia_app.data.model.Supplier;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import androidx.annotation.NonNull;
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
                initializeUser(user.getUid());
                sendNotificationTokenToServer(user.getUid());
                finish();
            } else {
                Toast.makeText(this, "" + response.getError().getMessage(), Toast.LENGTH_LONG).show();
            }
        }

    }

    private void initializeUser(String user_id) {
        String url = "https://us-central1-scotiabank-app.cloudfunctions.net/";
        url += "get-user-by-id?id=" + user_id;

        new UserFetcher(this).execute(url);
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    private void sendNotificationTokenToServer(final String user_id) {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("Notification Error", "getInstanceId failed",
                                    task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        String url = "https://us-central1-scotiabank-app.cloudfunctions.net/";
                        url += "register-device-id?uid=" + user_id + "&device_id=" + token;

                        new NotificationFetcher(LoginActivity.this).execute(url);
                    }
                });
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
