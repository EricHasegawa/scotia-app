package com.example.scotia_app.database;

import java.util.ArrayList;

import android.app.Activity;
import android.view.View;
import android.widget.ProgressBar;

import com.example.scotia_app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * An abstract class for fetching data from the database. Contains several methods used to
 * retrieve and select specific items from the database, each of which return raw json to be
 * parsed and used in the UI.
 */
abstract public class DataFetcher extends DatabaseManager {

    /**
     * Create a new DataFetcher with the given context.
     *
     * @param context The context in which this DataFetcher runs.
     */
    protected DataFetcher(Activity context) {
        super(context);
    }

    protected DataFetcher(Activity context, String idToken) {
        super(context, idToken);
    }

    /**
     * Override this method in the child class in order to properly update the UI with the
     * retrieved data.
     *
     * @param rawJsons The list of raw json strings to be parsed and used in the UI.
     */
    @Override
    abstract protected void onPostExecute(ArrayList<String> rawJsons);

    /**
     * Return a list of JSONObjects with which to populate invoices.
     *
     * @param rawJson The raw json string to be parsed
     * @return A List of JSONObjects, each corresponding to a raw json string.
     */
    protected JSONArray createJSONObjects(String rawJson) {
        try {
            return new JSONArray(rawJson);
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns a JSONObject with which to populate the User.
     *
     * @param rawJson The raw json string to be parsed
     * @return A List of JSONObjects, each corresponding to a raw json string.
     */
    protected JSONObject createJSONObject(String rawJson) {
        try {
            return new JSONObject(rawJson);
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Shows this fetcher's associated view's progress bar if it has one
     */
    public void showProgressBar() {
        showOrHideProgressBar(true);
    }

    /**
     * Hides this fetcher's associated view's progress bar if it has one
     */
    protected void hideProgressBar() {
        showOrHideProgressBar(false);
    }

    /**
     * Helper method to showProgressBar and hideProgressBar which set's the visibility of this
     * fetcher's associated view's progress bar.
     *
     * @param visible: whether to make the progress bar visible or not
     */
    private void showOrHideProgressBar(boolean visible) {
        Activity context = super.getActivityWeakReference().get();
        ProgressBar progressBar = context.findViewById(R.id.progressBar);

        if (progressBar != null) {
            if (!visible) {
                progressBar.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.VISIBLE);
            }
        }
    }
}