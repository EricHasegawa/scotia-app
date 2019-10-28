package com.example.scotia_app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import android.os.AsyncTask;

/**
 * A class for fetching data from the database. Contains several methods used to retrieve and select
 * specific items from the database, each of which return raw json to be parsed and used in the UI.
 */
abstract public class DataFetcher extends AsyncTask<String, ArrayList<String>, ArrayList<String>> {
    @Override
    protected ArrayList<String> doInBackground(String... strings) {
        ArrayList<String> rawJsonStrings = new ArrayList<>();

        HttpURLConnection connection = null;
        try {
            URL url;
            for (String urlStr : strings) {
                url = new URL(urlStr);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("accept", "text/html");
                rawJsonStrings.add(fetchText(connection));
            }
            return rawJsonStrings;
        } catch (java.net.MalformedURLException e1) {
            e1.printStackTrace();
        } catch (java.io.IOException e2) {
            e2.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    @Override
    abstract protected void onPostExecute(ArrayList<String> rawJsons);

    /**
     * Helper method for getJson, creates a reader and builds a string out of the HTTP response for
     * the given connection.
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

}
