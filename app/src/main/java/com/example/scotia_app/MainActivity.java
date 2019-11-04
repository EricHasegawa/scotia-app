package com.example.scotia_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Spinner personaSelectorSpinner = findViewById(R.id.personaSelectorSpinner);
        setSpinnerValues(personaSelectorSpinner);

        final String[] selectorToURL = new String[5];
        defineSelectorToURL(selectorToURL);

        defineButtonBehaviour(personaSelectorSpinner, selectorToURL);
    }

    /**
     * Helper method for onCreate which sets this spinner's values to the specified possible
     * personas in the strings.xml file
     *
     * @param personaSelectorSpinner: the Spinner whose values will be set
     */
    private void setSpinnerValues(Spinner personaSelectorSpinner) {
        ArrayAdapter<CharSequence> spinnerListAdapter;
        spinnerListAdapter = ArrayAdapter.createFromResource(this, R.array.personaList,
                android.R.layout.simple_spinner_item);
        spinnerListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        personaSelectorSpinner.setAdapter(spinnerListAdapter);
    }

    /**
     * Helper method for onCreate which sets this array's values to the specified possible
     * personas in the strings.xml file
     *
     * Note: this is a temporary solution until authentication is implemented for the next phase.
     *
     * @param selectorToURL: the array whose values will be set
     */
    private void defineSelectorToURL(String[] selectorToURL) {
        final String userDataUrl = "https://us-central1-scotiabank-app.cloudfunctions.net/" +
                "get-user-by-id?";
        selectorToURL[0] = userDataUrl + "id=jQFoXa2X7NriramADeph&type=supplier";
        selectorToURL[1] = userDataUrl + "id=6sO5KTFPxBPTCioYbHiA&type=driver";
        selectorToURL[2] = userDataUrl + "id=ovIUOGDZGvvVVWUi70WS&type=driver";
        selectorToURL[3] = userDataUrl + "id=EXsF6CM1WxoVNPS9CmfJ&type=customer";
        selectorToURL[4] = userDataUrl + "id=XU4PSNljqjhgRg72c4dV&type=customer";
    }

    /**
     * Helper method for onCreate which defines the Button's onClick behavior.
     *
     * Note: this is a temporary solution until authentication is implemented for the next phase.
     *
     * @param selectorToURL: the array whose values will be set
     */
    private void defineButtonBehaviour(final Spinner personaSelectorSpinner, final String[]
            selectorToURL) {
        final Button startAppButton = findViewById(R.id.startAppButton);
        startAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Starts the loading icon
                ProgressBar spinner = findViewById(R.id.progressBar);
                spinner.setVisibility(View.VISIBLE);

                // Fetches the data of the selected user.
                new UserFetcher(MainActivity.this).execute(
                        selectorToURL[personaSelectorSpinner.getSelectedItemPosition()]);
            }
        });
    }

    /**
     * Fetches and parses the attributes of the logged-in User.
     */
    static private class UserFetcher extends DataFetcher {

        /**
         * Initialize a new UserFetcher, which runs in the given context.
         *
         * @param context The context in which this UserFetcher runs.
         */
        UserFetcher(Activity context) {
            super(context);
        }

        /**
         * Returns a JSONObject with which to populate the User.
         *
         * @param rawJson The raw json string to be parsed
         * @return A List of JSONObjects, each corresponding to a raw json string.
         */
        private JSONObject createJSONObject(String rawJson) {
            try {
                return new JSONObject(rawJson);
            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After super.doInBackground is finished executing, populate a user with the retrieved JSON
         *
         * @param rawJsons The list of raw json strings whose first element is the user data
         */
        @Override
        protected void onPostExecute(ArrayList<String> rawJsons) {
            JSONObject userData = createJSONObject(rawJsons.get(0));
            AppCompatActivity context = (AppCompatActivity) super.getActivityWeakReference().get();
            Spinner personaSelectorSpinner = context.findViewById(R.id.personaSelectorSpinner);

            Intent switchToBottomNavigationView = new Intent(context,
                    BottomNavigationActivity.class);

            int selectedPersona = personaSelectorSpinner.getSelectedItemPosition();

            switch (selectedPersona) {
                case 0:
                    switchToBottomNavigationView.putExtra("user", new Supplier(userData));
                    break;
                case 1:
                case 2:
                    switchToBottomNavigationView.putExtra("user", new Driver(userData));
                    break;
                case 3:
                case 4:
                    switchToBottomNavigationView.putExtra("user", new Customer(userData));
                    break;
            }

            // Starts the loading icon
            ProgressBar spinner = getActivityWeakReference().get().findViewById(R.id.progressBar);
            spinner.setVisibility(View.GONE);
            context.startActivity(switchToBottomNavigationView);
        }
    }
}
