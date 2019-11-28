package com.example.scotia_app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class for fetching data from the database. Contains several methods used to retrieve and select
 * specific items from the database, each of which return raw json to be parsed and used in the UI.
 */
abstract public class DataFetcher extends AsyncTask<String, ArrayList<String>, ArrayList<String>> {

    /**
     * Weak reference to the context in which this DataFetcher runs.
     */
    private WeakReference<Activity> activityWeakReference;

    /**
     * Create a new DataFetcher with the given context.
     *
     * @param context The context in which this DataFetcher runs.
     */
    protected DataFetcher(Activity context) {
        activityWeakReference = new WeakReference<>(context);
    }

    /**
     * Return a list of raw json strings from the data at each urlStr.
     *
     * @param urlStrs All of the urls to have raw json data retrieved from.
     * @return An ArrayList containing each raw json string.
     */
    @Override
    protected ArrayList<String> doInBackground(String... urlStrs) {
        ArrayList<String> rawJsonStrings = new ArrayList<>();
        HttpURLConnection connection = null;
        try {
            URL url;
            for (String urlStr : urlStrs) {
                url = new URL(urlStr);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("accept", "text/html");
                rawJsonStrings.add(fetchText(connection));
            }
            return rawJsonStrings;
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    /**
     * Override this method in the child class in order to properly update the UI with the retrieved
     * data.
     *
     * @param rawJsons The list of raw json strings to be parsed and used in the UI.
     */
    @Override
    abstract protected void onPostExecute(ArrayList<String> rawJsons);

    /**
     * Helper method for doInBackground, creates a reader and builds a string out of the HTTP
     * response for the given connection.
     *
     * @param connection The connection with the HTTP response we are reading.
     * @return The HTTP response for the given connection in String form.
     */
    private String fetchText(HttpURLConnection connection) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            reader.close();
            return response.toString();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get activityWeakReference.
     *
     * @return the weak reference to the context in which this DataFetcher runs.
     */
    protected WeakReference<Activity> getActivityWeakReference() {
        return activityWeakReference;
    }

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
     * Helper method to showProgressBar and hideProgressBar which set's the visibility of this fetcher's
     * associated view's progress bar.
     *
     * @param visible: whether to make the progress bar visible or not
     */
    private void showOrHideProgressBar(boolean visible) {
        Activity context = activityWeakReference.get();
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