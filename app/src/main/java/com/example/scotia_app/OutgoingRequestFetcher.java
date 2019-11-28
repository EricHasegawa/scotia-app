package com.example.scotia_app;

import android.app.Activity;

import java.util.ArrayList;

/**
 * Child class of DataFetcher which has an empty body for OnPostExecute. To be used for any outgoing
 * tasks when nothing has to be done once the outgoing task is completed.
 */
public class OutgoingRequestFetcher extends DataFetcher {

    /**
     * Initialize a new NotificationTokenFetcher, which runs in the given context.
     *
     * @param context The context in which this UserFetcher runs.
     */
    public OutgoingRequestFetcher(Activity context) {
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
