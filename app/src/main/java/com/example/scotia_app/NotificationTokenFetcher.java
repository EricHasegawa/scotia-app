package com.example.scotia_app;

import android.app.Activity;

import java.util.ArrayList;

/**
 * Sends this device's notification id to the database
 */
public class NotificationTokenFetcher extends DataFetcher {

    /**
     * Initialize a new NotificationTokenFetcher, which runs in the given context.
     *
     * @param context The context in which this UserFetcher runs.
     */
    public NotificationTokenFetcher(Activity context) {
        super(context);
    }

    /**
     * After super.doInBackground is finished executing, do nothing. This method is only written
     * since it has to be overridden
     *
     * @param rawJsons The list of raw json strings whose first element is the user data
     */
    @Override
    protected void onPostExecute(ArrayList<String> rawJsons) {  }
}
