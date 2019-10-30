package com.example.scotia_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializes this spinner with the specified possible personas in the strings.xml file
        final Spinner personaSelectorSpinner = findViewById(R.id.personaSelectorSpinner);
        ArrayAdapter<CharSequence> spinnerListAdapter;
        spinnerListAdapter = ArrayAdapter.createFromResource(this, R.array.personaList,
                android.R.layout.simple_spinner_item);
        spinnerListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        personaSelectorSpinner.setAdapter(spinnerListAdapter);

        Button startAppButton = (Button) findViewById(R.id.startAppButton);
        startAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent switchToBottomNavigationView = new Intent(MainActivity.this,
                        BottomNavigationActivity.class);
                String selectedPersona = (String) personaSelectorSpinner.getSelectedItem();
                switchToBottomNavigationView.putExtra("user", selectedPersona);
                startActivity(switchToBottomNavigationView);
            }
        });
    }

}
