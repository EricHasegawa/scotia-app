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

import com.example.scotia_app.data.model.Persona;
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
import java.util.Objects;

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
                                idToken = Objects.requireNonNull(task.getResult()).getToken();
                                initializeUser(user.getUid(), LoginActivity.this);
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
                Objects.requireNonNull(user).getIdToken(true)
                        .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                            public void onComplete(@NonNull Task<GetTokenResult> task) {
                                if (task.isSuccessful()) {
                                    idToken = Objects.requireNonNull(task.getResult()).getToken();
                                    initializeUser(user.getUid(), LoginActivity.this);
                                }
                            }
                        });
            } else {
                Toast.makeText(this, Objects.requireNonNull(Objects.requireNonNull(response).getError()).getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private static void initializeUser(String user_id, Activity loginActivity) {
        sendNotificationTokenToServer(user_id, loginActivity);

        String url = "https://us-central1-scotiabank-app.cloudfunctions.net/get-user-by-id?";
        url += "id=" + user_id;

        new UserFetcher(loginActivity, idToken).execute(url);
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    private static void sendNotificationTokenToServer(final String user_id, final Activity loginActivity) {
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
                        String token = Objects.requireNonNull(task.getResult()).getToken();
                        String url = "https://us-central1-scotiabank-app.cloudfunctions.net/";
                        url += "register-device-id?uid=" + user_id + "&device_id=" + token;

                        new OutgoingRequest(loginActivity).execute(url);
                    }
                });
    }

    /**
     * Store a user with the given id, persona type, name, email address, and address.
     *
     * @param id      the Uid of the user being stored in the database.
     * @param type    the persona type of the user being stored in the database.
     * @param name    the name of the user being stored in the database.
     * @param email   the email address of the user being stored in the database.
     * @param address the address of the user being stored in the database.
     */
    private static void saveUserInfo(String id, Persona type, String name, String email,
                              @Nullable String address, Activity loginActivity) {
        String url = "https://us-central1-scotiabank-app.cloudfunctions.net/create-user?id="
                + id + "&type=" + type + "&name=" + name + "&email=" + email;

        if (address != null) {
            url += "&address=" + address;
            url = url.replaceAll(" ", "%20");
        }

        new UserSaver(loginActivity, id, idToken).execute(url);
    }

    /**
     * Open a dialog prompting the user to enter their address, then save an entry for them in
     * the database.
     */
    private static void openAddressDialog(final Activity loginActivity) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AlertDialog.Builder builder = new AlertDialog.Builder(loginActivity);
        builder.setTitle("Please enter your address: ");

        final EditText input = new EditText(loginActivity);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String address = input.getText().toString();
                if (user != null) {
                    saveUserInfo(user.getUid(), Persona.customer, user.getDisplayName(),
                            user.getEmail(), address, loginActivity);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Open a dialog prompting the user to choose a persona type, then create an entry for them
     * and save it in the database.
     */
    private static void createUserInfo(final Activity loginActivity) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String[] personas = {"Customer", "Driver", "Supplier"};

        AlertDialog.Builder builder = new AlertDialog.Builder(loginActivity);
        builder.setItems(personas, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int personaSelection) {
                if (user != null) {
                    switch (personaSelection) {
                        case 1:
                            saveUserInfo(user.getUid(), Persona.driver, user.getDisplayName(), user.getEmail(), null, loginActivity);
                            break;
                        case 2:
                            saveUserInfo(user.getUid(), Persona.supplier, user.getDisplayName(), user.getEmail(), null, loginActivity);
                            break;
                        case 0:
                        default:
                            openAddressDialog(loginActivity);
                            break;
                    }
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Fetches and parses the attributes of the logged-in User.
     */
    static private class UserFetcher extends DataFetcher {

        UserFetcher(Activity context, String idToken) {
            super(context, idToken);
        }

        /**
         * After super.doInBackground is finished executing, populate a user with the retrieved JSON
         *
         * @param rawJsons The list of raw json strings whose first element is the user data
         */
        @Override
        protected void onPostExecute(ArrayList<String> rawJsons) {
            JSONObject userData = createJSONObject(rawJsons.get(0));

            // if user data exists in the database i.e. if this isn't the first sign-in
            if (userData != null) {
                final AppCompatActivity context = (AppCompatActivity) getActivityWeakReference().get();

                final Intent switchToBottomNavigationView = new Intent(context,
                        MainActivity.class);

                context.finish();

                try {
                    putUserInfo(userData, context, switchToBottomNavigationView);
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
            } else {
                createUserInfo(getActivityWeakReference().get());
            }
        }

        /**
         * Add the currently signed in user's User object to the given Intent.
         *
         * @param userData      The JSONObject containing this user's data.
         * @param context       The context on which to switch to bottomNavView.
         * @param bottomNavView The Intent to which the user's data is passed and is switched to
         * @throws JSONException if JSONObject contains no field "persona", possibly to network
         *                       problems or a malformed URL.
         */
        private void putUserInfo(@Nullable JSONObject userData, Context context, Intent bottomNavView) throws JSONException {
            switch (Objects.requireNonNull(userData).getString("persona")) {
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
         * Show a spinner and start the given Intent.
         *
         * @param context       The context on which to switch to bottomNavView.
         * @param bottomNavView The Intent to which the user's data is passed and is switched to
         */
        private void switchActivities(final Context context, final Intent bottomNavView) {
            ProgressBar spinner = getActivityWeakReference().get().findViewById(R.id.progressBar);
            spinner.setVisibility(View.GONE);
            context.startActivity(bottomNavView);
        }
    }

    /**
     * If a new user is created, saves the users data and then calls user fetcher as though the user
     * was pre-existing
     */
    static private class UserSaver extends OutgoingRequest {
        private String user_id;

        /**
         * Initialize a new OutgoingRequest, which runs in the given context.
         *
         * @param context The context in which this OutgoingRequest runs.
         */
        UserSaver(Activity context, String user_id, String idToken) {
            super(context, idToken);
            this.user_id = user_id;
        }

        @Override
        protected void onPostExecute(ArrayList<String> rawJsons) {
            super.onPostExecute(rawJsons);
            initializeUser(user_id, getActivityWeakReference().get());
        }
    }
}