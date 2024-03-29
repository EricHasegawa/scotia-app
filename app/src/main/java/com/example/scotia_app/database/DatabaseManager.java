package com.example.scotia_app.database;

import android.app.Activity;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * An abstract class for executing an async task which interacts with the database.
 */
abstract class DatabaseManager extends AsyncTask<String, ArrayList<String>, ArrayList<String>> {

    private static String idToken;

    /**
     * Weak reference to the context in which this DatabaseManager runs.
     */
    private WeakReference<Activity> activityWeakReference;

    /**
     * Create a new DataFetcher with the given context.
     *
     * @param context The context in which this DataFetcher runs.
     */
    DatabaseManager(Activity context) {
        this.activityWeakReference = new WeakReference<>(context);
    }

    DatabaseManager(Activity context, String idToken) {
        this.activityWeakReference = new WeakReference<>(context);
        DatabaseManager.idToken = idToken;
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
                connection.setRequestProperty("Authorization", "Bearer " + idToken);
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
     * Override this method in the child class in order to properly update the UI elements
     *
     * @param rawJsons The list of raw json strings to be parsed and used in the UI.
     */
    @Override
    abstract protected void onPostExecute(ArrayList<String> rawJsons);

    /**
     * Get activityWeakReference.
     *
     * @return the weak reference to the context in which this DataFetcher runs.
     */
    protected WeakReference<Activity> getActivityWeakReference() {
        return activityWeakReference;
    }
}
