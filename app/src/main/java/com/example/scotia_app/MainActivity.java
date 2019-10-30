package com.example.scotia_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Spinner personaSelectorSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializes this spinner with the specified possible personas in the strings.xml file
        personaSelectorSpinner = findViewById(R.id.personaSelectorSpinner);
        ArrayAdapter<CharSequence> spinnerListAdapter;
        spinnerListAdapter = ArrayAdapter.createFromResource(this, R.array.personaList,
                android.R.layout.simple_spinner_item);
        spinnerListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        personaSelectorSpinner.setAdapter(spinnerListAdapter);

        final User[] selectorToUserDictionary = new User[5];
        selectorToUserDictionary[0] = new User("CocaCola", Persona.supplier, "jQFoXa2X7NriramADeph");
        selectorToUserDictionary[1] = new User("Great Tate", Persona.driver, "6sO5KTFPxBPTCioYbHiA");
        selectorToUserDictionary[2] = new User("Meric Gertler", Persona.driver, "ovIUOGDZGvvVVWUi70WS");
        selectorToUserDictionary[3] = new User("Antoine's Convenience Store", Persona.customer, "EXsF6CM1WxoVNPS9CmfJ");
        selectorToUserDictionary[4] = new User("Parsa's Store", Persona.customer, "XU4PSNljqjhgRg72c4dV");

        final Button startAppButton = findViewById(R.id.startAppButton);
        startAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent switchToBottomNavigationView = new Intent(MainActivity.this,
                        BottomNavigationActivity.class);

                int selectedPersona = personaSelectorSpinner.getSelectedItemPosition();
                switchToBottomNavigationView.putExtra("user", selectorToUserDictionary[selectedPersona]);

                startActivity(switchToBottomNavigationView);
            }
        });
    }

}
