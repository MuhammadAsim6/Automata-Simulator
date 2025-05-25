package com.example.toaproj;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.toaproj.views.AutomataView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TestAutomataActivity extends AppCompatActivity {

    private String initialState;
    private HashSet<String> finalStates;
    private HashMap<String, HashMap<String, String>> transitions;
    private TextView resultText;
    private AutomataView automataView;
    private Handler handler = new Handler(Looper.getMainLooper());
    private volatile boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_automata);

        // Initialize views
        setupViews();
        
        // Get automata data from intent
        getAutomataData();
        
        // Setup test button
        setupTestButton();
    }

    private void setupViews() {
        resultText = findViewById(R.id.resultText);
        automataView = findViewById(R.id.automataView);
        
        if (automataView == null || resultText == null) {
            showError("Error: Views not found");
            return;
        }
    }

    private void getAutomataData() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            showError("Error: No automata data received");
            return;
        }

        initialState = extras.getString("initialState");
        finalStates = (HashSet<String>) extras.getSerializable("finalStates");
        transitions = (HashMap<String, HashMap<String, String>>) 
            extras.getSerializable("transitions");

        try {
            automataView.setAutomata(transitions, finalStates, initialState);
        } catch (Exception e) {
            showError("Error initializing automata: " + e.getMessage());
        }
    }

    private void setupTestButton() {
        Button testButton = findViewById(R.id.testSequenceButton);
        EditText sequenceInput = findViewById(R.id.sequenceInput);

        testButton.setOnClickListener(v -> {
            if (!isProcessing && !sequenceInput.getText().toString().trim().isEmpty()) {
                String[] inputSequence = sequenceInput.getText().toString().trim().split(" ");
                processInput(inputSequence);
            }
        });
    }

    private void processInput(String[] inputSequence) {
        if (resultText == null || automataView == null) {
            showError("Views not initialized");
            return;
        }

        if (inputSequence == null || inputSequence.length == 0) {
            Toast.makeText(this, "Please enter a valid input sequence", Toast.LENGTH_SHORT).show();
            return;
        }

        isProcessing = true;
        new Thread(() -> {
            try {
                StringBuilder result = new StringBuilder();
                String currentState = initialState;
                boolean isValid = true;
                int step = 1;

                // Reset to initial state
                final String initialStateTemp = currentState;
                runOnUiThread(() -> {
                    try {
                        automataView.setCurrentState(initialStateTemp);
                        resultText.setText(""); // Clear previous results
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                result.append("Step-by-step execution:\n");
                result.append("START → ").append(currentState).append("\n");

                for (String input : inputSequence) {
                    if (!transitions.containsKey(currentState) || 
                        !transitions.get(currentState).containsKey(input)) {
                        result.append("\n❌ Invalid input '").append(input)
                              .append("' for state '").append(currentState).append("'\n");
                        isValid = false;
                        break;
                    }

                    String nextState = transitions.get(currentState).get(input);
                    result.append(String.format("Step %d: %s --(%s)--> %s", 
                        step++, currentState, input, nextState));
                    
                    if (finalStates.contains(nextState)) {
                        result.append(" (Final State)");
                    }
                    result.append("\n");
                    
                    currentState = nextState;
                    
                    // Update visualization for each step
                    final String finalCurrentState = currentState;
                    runOnUiThread(() -> {
                        try {
                            automataView.setCurrentState(finalCurrentState);
                            resultText.setText(result.toString()); // Update results in real-time
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    
                    Thread.sleep(500);
                }

                result.append("\nFinal Result: ");
                if (isValid && finalStates.contains(currentState)) {
                    result.append("✅ ACCEPTED\n");
                    result.append("Input sequence reached final state: ").append(currentState);
                } else {
                    result.append("❌ REJECTED\n");
                    if (isValid) {
                        result.append("Stopped at non-final state: ").append(currentState);
                    }
                }

                // Update UI with final result
                runOnUiThread(() -> {
                    try {
                        resultText.setText(result.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> showError("Error processing input: " + e.getMessage()));
            } finally {
                isProcessing = false;
            }
        }).start();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isProcessing = false;
    }
}
