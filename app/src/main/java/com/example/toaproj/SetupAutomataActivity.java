package com.example.toaproj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.graphics.Typeface;
import android.graphics.Color;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.os.Parcelable;
import androidx.annotation.NonNull;

public class SetupAutomataActivity extends AppCompatActivity {

    private HashSet<String> states = new HashSet<>();
    private HashMap<String, HashMap<String, String>> transitions = new HashMap<>();
    private LinearLayout statesContainer;
    private LinearLayout transitionsContainer;
    private EditText numStatesInput;
    private EditText alphabetInput;
    private TextView currentStatesView;

    private static final String KEY_STATES = "states";
    private static final String KEY_TRANSITIONS = "transitions";

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_STATES, states);
        outState.putSerializable(KEY_TRANSITIONS, transitions);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_automata);

        initializeViews();
        setupClickListeners();

        if (savedInstanceState != null) {
            states = (HashSet<String>) savedInstanceState.getSerializable(KEY_STATES);
            transitions = (HashMap<String, HashMap<String, String>>) 
                savedInstanceState.getSerializable(KEY_TRANSITIONS);
            if (!states.isEmpty()) {
                updateCurrentStatesView();
                generateTransitionInputs();
            }
        }
    }

    private void initializeViews() {
        statesContainer = findViewById(R.id.statesContainer);
        transitionsContainer = findViewById(R.id.transitionsContainer);
        numStatesInput = findViewById(R.id.numStatesInput);
        alphabetInput = findViewById(R.id.alphabetInput);
        currentStatesView = findViewById(R.id.currentStatesView);
    }

    private void setupClickListeners() {
        findViewById(R.id.generateStatesButton).setOnClickListener(v -> {
            try {
                int numStates = Integer.parseInt(numStatesInput.getText().toString().trim());
                if (numStates <= 0 || numStates > 10) {
                    Toast.makeText(this, "Please enter a number between 1 and 10", Toast.LENGTH_SHORT).show();
                    return;
                }
                generateStateInputs(numStates);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.confirmStatesButton).setOnClickListener(v -> {
            saveStates();
        });

        findViewById(R.id.nextButton).setOnClickListener(v -> {
            if (validateAndProceed()) {
                Intent intent = new Intent(this, DefineStatesActivity.class);
                intent.putExtra("states", states);
                intent.putExtra("transitions", transitions);
                startActivity(intent);
            }
        });
    }

    private void generateStateInputs(int numStates) {
        statesContainer.removeAllViews();
        
        for (int i = 0; i < numStates; i++) {
            TextInputLayout inputLayout = new TextInputLayout(this);
            inputLayout.setHint("State " + (i + 1));
            
            TextInputEditText stateInput = new TextInputEditText(this);
            stateInput.setId(View.generateViewId());
            inputLayout.addView(stateInput);
            
            statesContainer.addView(inputLayout);
        }
        
        findViewById(R.id.confirmStatesButton).setVisibility(View.VISIBLE);
    }

    private void saveStates() {
        states.clear();
        transitions.clear();
        boolean validInput = true;
        HashSet<String> tempStates = new HashSet<>();

        for (int i = 0; i < statesContainer.getChildCount(); i++) {
            View view = statesContainer.getChildAt(i);
            if (view instanceof TextInputLayout) {
                TextInputEditText stateInput = (TextInputEditText) ((TextInputLayout) view).getEditText();
                String state = stateInput.getText().toString().trim();
                
                if (state.isEmpty()) {
                    validInput = false;
                    stateInput.setError("State name cannot be empty");
                } else if (tempStates.contains(state)) {
                    validInput = false;
                    stateInput.setError("Duplicate state name");
                } else {
                    tempStates.add(state);
                }
            }
        }

        if (validInput) {
            states.addAll(tempStates);
            for (String state : states) {
                transitions.put(state, new HashMap<>());
            }
            updateCurrentStatesView();
            generateTransitionInputs();
        }
    }

    private void updateCurrentStatesView() {
        StringBuilder sb = new StringBuilder("Current States:\n");
        for (String state : states) {
            sb.append("• ").append(state).append("\n");
        }
        currentStatesView.setText(sb.toString());
        currentStatesView.setVisibility(View.VISIBLE);
    }

    private void generateTransitionInputs() {
        String alphabet = alphabetInput.getText().toString().trim();
        if (!validateAlphabet(alphabet)) {
            return;
        }

        transitionsContainer.removeAllViews();
        String[] inputs = alphabet.split(",");

        // Add header
        TextView header = new TextView(this);
        header.setText("Define Transitions");
        header.setTextSize(18);
        header.setPadding(0, 32, 0, 16);
        transitionsContainer.addView(header);

        // Add example
        TextView example = new TextView(this);
        example.setText("Select where each state goes on each input\nExample: From q0 on input '0' select where it should go");
        example.setTypeface(example.getTypeface(), Typeface.ITALIC);
        example.setTextColor(Color.GRAY);
        example.setPadding(0, 0, 0, 16);
        transitionsContainer.addView(example);

        // Create table layout
        TableLayout tableLayout = new TableLayout(this);
        tableLayout.setLayoutParams(new TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT));
        tableLayout.setStretchAllColumns(true);

        // Add header row
        TableRow headerRow = new TableRow(this);
        headerRow.addView(createHeaderCell("From"));
        headerRow.addView(createHeaderCell("Input"));
        headerRow.addView(createHeaderCell("To"));
        tableLayout.addView(headerRow);

        // Add rows for transitions
        for (String fromState : states) {
            for (String input : inputs) {
                TableRow row = new TableRow(this);
                row.addView(createCell(fromState));
                row.addView(createCell(input));
                
                Spinner spinner = new Spinner(this);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item,
                    new ArrayList<>(states));
                spinner.setAdapter(adapter);
                
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String toState = parent.getItemAtPosition(position).toString();
                        if (!transitions.containsKey(fromState)) {
                            transitions.put(fromState, new HashMap<>());
                        }
                        transitions.get(fromState).put(input.trim(), toState);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
                
                row.addView(spinner);
                tableLayout.addView(row);
            }
        }
        
        transitionsContainer.addView(tableLayout);
    }

    private TextView createHeaderCell(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        tv.setPadding(8, 8, 8, 8);
        tv.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        return tv;
    }

    private TextView createCell(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(8, 8, 8, 8);
        return tv;
    }

    private boolean validateAndProceed() {
        if (states.isEmpty()) {
            Toast.makeText(this, "Please define states first", Toast.LENGTH_SHORT).show();
            return false;
        }

        boolean allTransitionsDefined = true;
        String alphabet = alphabetInput.getText().toString().trim();
        String[] inputs = alphabet.split(",");

        for (String fromState : states) {
            for (String input : inputs) {
                if (!transitions.get(fromState).containsKey(input.trim())) {
                    allTransitionsDefined = false;
                    break;
                }
            }
        }

        if (!allTransitionsDefined) {
            Toast.makeText(this, "Please define all transitions", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean validateAlphabet(String alphabet) {
        if (alphabet.isEmpty()) {
            alphabetInput.setError("Alphabet cannot be empty");
            return false;
        }
        
        String[] inputs = alphabet.split(",");
        for (String input : inputs) {
            if (input.trim().isEmpty()) {
                alphabetInput.setError("Invalid alphabet format");
                return false;
            }
        }
        return true;
    }
}