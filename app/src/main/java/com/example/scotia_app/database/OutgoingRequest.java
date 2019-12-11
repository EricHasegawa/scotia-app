package com.example.scotia_app.database;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;

/**
 * A class which executes an outgoing task that interacts with the database.
 */
public class OutgoingRequest extends DatabaseManager {

    private static final String TAG = "Outgoing Task";

    /**
     * Initialize a new OutgoingRequest, which runs in the given context.
     *
     * @param context The context in which this OutgoingRequest runs.
     */
    public OutgoingRequest(Activity context) {
        super(context);
    }

    public OutgoingRequest(Activity context, String idToken) {
        super(context, idToken);
    }

    /**
     * After super.doInBackground is finished executing, log the result of this outgoing task.
     * This is the default for outgoing asynchronous tasks. If some task requires some action to be
     * executed on completion, then just override this method
     *
     * @param rawJsons The list of raw json strings whose first element is the user data
     */
    @Override
    protected void onPostExecute(ArrayList<String> rawJsons) {
        Log.d(TAG, rawJsons.toString());
    }
}
