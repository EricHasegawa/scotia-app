package com.example.scotia_app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.scotia_app.database.DataFetcher;
import com.example.scotia_app.database.OutgoingRequest;
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
import com.google.firebase.auth.GetTokenResult;
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

    private static String idToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(this);
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build()
            );
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setLogo(R.drawable.logo1)
                            .setTheme(R.style.AppTheme_NoActionBar)
                            .setIsSmartLockEnabled(false)
                            .build(),
                    123);
        } else {
            user.getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                idToken = task.getResult().getToken();
                                initializeUser(user.getUid());
                            }
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                user.getIdToken(true)
                        .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                            public void onComplete(@NonNull Task<GetTokenResult> task) {
                                if (task.isSuccessful()) {
                                    idToken = task.getResult().getToken();
                                    initializeUser(user.getUid());
                                }
                            }
                        });
            } else {
                Toast.makeText(this, "" + response.getError().getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initializeUser(String user_id) {
        sendNotificationTokenToServer(user_id);

        String url = "https://us-central1-scotiabank-app.cloudfunctions.net/get-user-by-id?";
        url += "id=" + user_id;

        new UserFetcher(this, idToken).execute(url);
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

                        new OutgoingRequest(LoginActivity.this).execute(url);
                    }
                });
    }

    /**
     * Fetches and parses the attributes of the logged-in User.
     */
    static private class UserFetcher extends DataFetcher {

        private String address;

        UserFetcher(Activity context, String idToken) { super(context, idToken); }

        /**
         * After super.doInBackground is finished executing, populate a user with the retrieved JSON
         *
         * @param rawJsons The list of raw json strings whose first element is the user data
         */
        @Override
        protected void onPostExecute(ArrayList<String> rawJsons) {
            JSONObject userData = createJSONObject(rawJsons.get(0));
            final AppCompatActivity context = (AppCompatActivity) getActivityWeakReference().get();

            final Intent switchToBottomNavigationView = new Intent(context,
                    BottomNavigationActivity.class);

            context.finish();

            try {
                putUserInfo(userData, context, switchToBottomNavigationView);
            } catch (JSONException | NullPointerException e) {
                createUserInfo(context, switchToBottomNavigationView);
            }
        }

        /**
         * Add the currently signed in user's User object to the given Intent.
         *
         * @param userData The JSONObject containing this user's data.
         * @param context The context on which to switch to bottomNavView.
         * @param bottomNavView The Intent to which the user's data is passed and is switched to
         * @throws JSONException if JSONObject contains no field "persona", possibly to network
         * problems or a malformed URL.
         */
        private void putUserInfo(@Nullable JSONObject userData, Context context, Intent bottomNavView) throws JSONException {
            switch (userData.getString("persona")) {
                case "driver":
                    bottomNavView.putExtra("user", new Driver(userData));
                    break;
                case "supplier":
                    bottomNavView.putExtra("user", new Supplier(userData));
                    break;
                case "customer":
                default:
                    bottomNavView.putExtra("user", new Customer(userData));
                    break;
            }
            switchActivities(context, bottomNavView);
        }

        /**
         * Open a dialog prompting the user to choose a persona type, then create an entry for them
         * and save it in the database.
         *
         * @param context The context on which to switch to bottomNavView.
         * @param bottomNavView The Intent to which the user's data is passed and is switched to
         * @throws NullPointerException if user has no display name
         */
        private void createUserInfo(final Context context, final Intent bottomNavView) throws NullPointerException {
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            final String[] personas = {"Customer", "Driver", "Supplier"};

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivityWeakReference().get());
            builder.setItems(personas, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int personaSelection) {
                    switch (personaSelection) {
                        case 1:
                            bottomNavView.putExtra("user", new Driver(user.getDisplayName(), user.getUid()));
                            saveUserInfo(user.getUid(), "driver", user.getDisplayName(), user.getEmail(), null);
                            switchActivities(context, bottomNavView);
                            break;
                        case 2:
                            bottomNavView.putExtra("user", new Supplier(user.getDisplayName(), user.getUid()));
                            saveUserInfo(user.getUid(), "supplier", user.getDisplayName(), user.getEmail(), null);
                            switchActivities(context, bottomNavView);
                            break;
                        case 0:
                        default:
                            bottomNavView.putExtra("user", new Customer(user.getDisplayName(), user.getUid()));
                            openAddressDialog(getActivityWeakReference().get(), bottomNavView);
                            break;
                    }
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        /**
         * Store a user with the given id, persona type, name, email address, and address.
         *
         * @param id the Uid of the user being stored in the database.
         * @param type the persona type of the user being stored in the database.
         * @param name the name of the user being stored in the database.
         * @param email the email address of the user being stored in the database.
         * @param address the address of the user being stored in the database.
         */
        private void saveUserInfo(String id, String type, String name, String email,
                                  @Nullable String address) {
            String url = "https://us-central1-scotiabank-app.cloudfunctions.net/create-user?id="
                    + id + "&type=" + type + "&name=" + name + "&email=" + email;
            if (address != null) {
                url += "&address=" + address;
            }

            new OutgoingRequest(getActivityWeakReference().get()).execute(url);
        }

        /**
         * Show a spinner and start the given Intent.
         *
         * @param context The context on which to switch to bottomNavView.
         * @param bottonNavView The Intent to which the user's data is passed and is switched to
         */
        private void switchActivities(final Context context, final Intent bottonNavView) {
            ProgressBar spinner = getActivityWeakReference().get().findViewById(R.id.progressBar);
            spinner.setVisibility(View.GONE);
            context.startActivity(bottonNavView);
        }

        /**
         * Open a dialog prompting the user to enter their address, then save an entry for them in
         * the database.
         *
         * @param context context The context on which to switch to bottomNavView.
         * @param bottomNavView The Intent to which the user's data is passed and is switched to
         */
        private void openAddressDialog(final Context context, final Intent bottomNavView) {
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivityWeakReference().get());
            builder.setTitle("Please enter your address: ");

            final EditText input = new EditText(getActivityWeakReference().get());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    address = input.getText().toString();
                    saveUserInfo(user.getUid(), "customer", user.getDisplayName(), user.getEmail(), address);
                    switchActivities(context, bottomNavView);
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}