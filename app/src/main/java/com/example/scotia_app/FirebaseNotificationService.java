package com.example.scotia_app;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseNotificationService extends FirebaseMessagingService {

    private static final String TAG = "Notification Service";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        String title = remoteMessage.getNotification().getTitle();
        String message = remoteMessage.getNotification().getBody();

        Intent intent = new Intent(getString(R.string.notification_received));
        intent.putExtra("title", title);
        intent.putExtra("message", message);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(intent);
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        /*
         If you want to send messages to this application instance or
         manage this apps subscriptions on the server side, send the
         Instance ID token to your app server.
        */
        sendRegistrationToServer(token);
    }

    /**
     * Sends this token to the database to make sure this user has the correct notification token
     * @param token: the notification token of this user's device
     */
    private void sendRegistrationToServer(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            Log.d(TAG, "Refreshed token: " + token + " will be saved to database");
            String user_id = user.getUid();

            Intent intent = new Intent(getString(R.string.notification_token_updated));

            String url = "https://us-central1-scotiabank-app.cloudfunctions.net/";
            url += "register-device-id?uid=" + user_id + "&device_id=" + token;

            intent.putExtra("url", url);

            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
            localBroadcastManager.sendBroadcast(intent);
        } else {
            Log.d(TAG, "No user is currently logged in");
        }
    }
}
