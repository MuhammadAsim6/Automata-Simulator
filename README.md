![Image](https://github.com/user-attachments/assets/175aa44c-1277-49e8-bb02-b1e394a3fca3) ![Image](https://github.com/user-attachments/assets/9c3add11-ac66-4d78-b1e6-110dc4b15814) ![Image](https://github.com/user-attachments/assets/a87d4103-498f-4a25-b940-8cf39c2a3b29) ![Image](https://github.com/user-attachments/assets/cba996d3-f166-467f-97ad-ac6bd49e0415)

# Theory of Automata Simulator - Technical Implementation Guide

## 1. Core Data Structures

```java
// Main data structures used throughout the app
private HashSet<String> states;        // Stores all states (q0, q1, etc.)
private HashSet<String> finalStates;   // Stores accepting states
private String currentState;           // Tracks current state during testing

// Transition table structure
private HashMap<String, HashMap<String, String>> transitions;
/* Example:
transitions = {
    "q0": {"0": "q1", "1": "q2"},  // From q0: on 0 go to q1, on 1 go to q2
    "q1": {"0": "q0", "1": "q1"}   // From q1: on 0 go to q0, on 1 go to q1
}
*/
```

## 2. Activity Flow

### MainActivity (Entry Point)
```java
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        setupButton.setOnClickListener(v -> {
            // Start automata setup process
            startActivity(new Intent(this, SetupAutomataActivity.class));
        });
    }
}
```

### SetupAutomataActivity (Create Automata)
```java
public class SetupAutomataActivity extends AppCompatActivity {
    // 1. Generate state inputs
    private void generateStateInputs(int numStates) {
        for (int i = 0; i < numStates; i++) {
            // Create input field for each state
            TextInputLayout stateInput = new TextInputLayout(this);
            statesContainer.addView(stateInput);
        }
    }

    // 2. Save states
    private void saveStates() {
        states.clear();
        for (TextInputLayout input : stateInputs) {
            String stateName = input.getEditText().getText().toString();
            states.add(stateName);
            transitions.put(stateName, new HashMap<>());
        }
    }

    // 3. Generate transition table
    private void generateTransitionInputs() {
        for (String state : states) {
            for (String input : alphabet.split(",")) {
                // Create dropdown for each state-input pair
                createTransitionRow(state, input);
            }
        }
    }
}
```

## 3. Visual Implementation (AutomataView)

```java
public class AutomataView extends View {
    // Drawing constants
    private static final float STATE_RADIUS = 80f;
    private static final float ARROW_HEAD_SIZE = 30f;

    // Calculate state positions in a circle
    private void calculateStatePositions() {
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) - STATE_RADIUS - 50;
        
        int i = 0;
        for (String state : states) {
            float angle = (float) (2 * Math.PI * i / states.size());
            float x = centerX + radius * (float) Math.cos(angle);
            float y = centerY + radius * (float) Math.sin(angle);
            statePositions.put(state, new Float[]{x, y});
            i++;
        }
    }

    // Draw states and transitions
    @Override
    protected void onDraw(Canvas canvas) {
        // Draw transitions first (arrows)
        for (Map.Entry<String, HashMap<String, String>> fromState : transitions.entrySet()) {
            String from = fromState.getKey();
            for (Map.Entry<String, String> transition : fromState.getValue().entrySet()) {
                drawTransition(canvas, from, transition.getValue(), transition.getKey());
            }
        }

        // Draw states on top
        for (Map.Entry<String, Float[]> state : statePositions.entrySet()) {
            drawState(canvas, state.getKey(), state.getValue()[0], state.getValue()[1]);
        }
    }
}
```

## 4. Testing Implementation (TestAutomataActivity)

```java
public class TestAutomataActivity extends AppCompatActivity {
    // Process input sequence
    private void processInput(String[] inputSequence) {
        String currentState = initialState;
        StringBuilder result = new StringBuilder();
        
        // Step through each input
        for (String input : inputSequence) {
            // Get next state
            String nextState = transitions.get(currentState).get(input);
            
            // Update visualization
            automataView.setCurrentState(nextState);
            
            // Update result trace
            result.append(String.format("Step %d: %s --(%s)--> %s\n", 
                step++, currentState, input, nextState));
            
            currentState = nextState;
        }

        // Check if accepted
        if (finalStates.contains(currentState)) {
            result.append("✅ ACCEPTED\n");
        } else {
            result.append("❌ REJECTED\n");
        }
    }
}
```

## 5. Key Algorithms

### State Position Calculation
```java
private void calculateStatePositions() {
    int numStates = states.size();
    float centerX = getWidth() / 2f;
    float centerY = getHeight() / 2f;
    float radius = Math.min(centerX, centerY) - STATE_RADIUS - 50;
    
    int i = 0;
    for (String state : states) {
        float angle = (float) (2 * Math.PI * i / numStates);
        float x = centerX + radius * (float) Math.cos(angle);
        float y = centerY + radius * (float) Math.sin(angle);
        statePositions.put(state, new Float[]{x, y});
        i++;
    }
}
```

### Arrow Drawing
```java
private void drawTransition(Canvas canvas, String from, String to, String input) {
    // Calculate start and end points
    float angle = (float) Math.atan2(toY - fromY, toX - fromX);
    float startX = fromX + STATE_RADIUS * (float) Math.cos(angle);
    float startY = fromY + STATE_RADIUS * (float) Math.sin(angle);
    
    // Draw arrow line
    canvas.drawLine(startX, startY, endX, endY, arrowPaint);
    
    // Draw arrow head
    drawArrowHead(canvas, endX, endY, angle);
    
    // Draw input label
    canvas.drawText(input, labelX, labelY, textPaint);
}
```

## 6. Error Handling

```java
// Input validation
private boolean validateInput(String input) {
    if (input.isEmpty()) {
        showError("Input sequence cannot be empty");
        return false;
    }
    
    for (String symbol : input.split(" ")) {
        if (!isValidSymbol(symbol)) {
            showError("Invalid symbol: " + symbol);
            return false;
        }
    }
    return true;
}

// Transition validation
private boolean validateTransitions() {
    for (String state : states) {
        for (String input : alphabet.split(",")) {
            if (!transitions.get(state).containsKey(input)) {
                showError("Missing transition for state " + state + " on input " + input);
                return false;
            }
        }
    }
    return true;
}
```

## 7. Common Issues & Solutions

1. **App Crashes During Testing**
```java
// Always validate input before processing
if (!validateInput(inputSequence)) {
    return;
}

// Check for null transitions
if (transitions.get(currentState) == null || 
    transitions.get(currentState).get(input) == null) {
    showError("Invalid transition");
    return;
}
```

2. **Visual Glitches**
```java
// Force redraw when state changes
public void setCurrentState(String state) {
    this.currentState = state;
    invalidate();  // Triggers onDraw()
}

// Handle screen rotation
@Override
protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    calculateStatePositions();  // Recalculate positions
}
```

This technical guide shows the actual implementation details of the automata simulator, including data structures, algorithms, and error handling.
