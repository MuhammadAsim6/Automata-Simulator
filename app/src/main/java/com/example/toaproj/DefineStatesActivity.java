package com.example.toaproj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.HashSet;

public class DefineStatesActivity extends AppCompatActivity {

    private HashSet<String> states;
    private HashMap<String, HashMap<String, String>> transitions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_define_states2);

        states = (HashSet<String>) getIntent().getSerializableExtra("states");
        transitions = (HashMap<String, HashMap<String, String>>) getIntent().getSerializableExtra("transitions");

        findViewById(R.id.testButton).setOnClickListener(v -> {
            EditText initialStateInput = findViewById(R.id.initialStateInput);
            EditText finalStatesInput = findViewById(R.id.finalStatesInput);

            String initialState = initialStateInput.getText().toString().trim();
            String[] finalStateArray = finalStatesInput.getText().toString().trim().split(" ");

            if (!states.contains(initialState)) {
                Toast.makeText(this, "Initial state must be valid!", Toast.LENGTH_SHORT).show();
                return;
            }

            HashSet<String> finalStates = new HashSet<>();
            for (String state : finalStateArray) {
                if (states.contains(state)) {
                    finalStates.add(state);
                }
            }

            Intent intent = new Intent(this, TestAutomataActivity.class);
            intent.putExtra("initialState", initialState);
            intent.putExtra("finalStates", finalStates);
            intent.putExtra("transitions", transitions);
            startActivity(intent);
        });
    }
}
