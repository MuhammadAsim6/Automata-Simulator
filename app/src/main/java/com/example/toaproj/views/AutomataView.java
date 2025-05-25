package com.example.toaproj.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
import android.graphics.Color;
import android.util.AttributeSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AutomataView extends View {
    private Paint statePaint, textPaint, arrowPaint, highlightPaint;
    private HashMap<String, Float[]> statePositions = new HashMap<>();
    private HashMap<String, HashMap<String, String>> transitions;
    private String currentState;
    private HashSet<String> finalStates;
    
    // Constants for drawing
    private static final float STATE_RADIUS = 80f;
    private static final float ARROW_HEAD_SIZE = 30f;
    private static final float LABEL_OFFSET = 20f;

    public AutomataView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupPaints();
    }

    private void setupPaints() {
        // State circle paint
        statePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        statePaint.setColor(Color.WHITE);
        statePaint.setStyle(Paint.Style.FILL);
        statePaint.setShadowLayer(5, 0, 0, Color.GRAY);

        // Text paint for labels
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(32f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Arrow paint
        arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowPaint.setColor(Color.GRAY);
        arrowPaint.setStyle(Paint.Style.STROKE);
        arrowPaint.setStrokeWidth(3f);

        // Current state highlight paint
        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(Color.parseColor("#4CAF50"));
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(8f);
    }

    public void setAutomata(HashMap<String, HashMap<String, String>> transitions,
                           HashSet<String> finalStates, String initialState) {
        this.transitions = transitions;
        this.finalStates = finalStates;
        this.currentState = initialState;
        calculateStatePositions();
        invalidate();
    }

    public void setCurrentState(String state) {
        this.currentState = state;
        invalidate();
    }

    private void calculateStatePositions() {
        if (transitions == null) return;
        
        statePositions.clear();
        int numStates = transitions.size();
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) - STATE_RADIUS - 50;
        
        int i = 0;
        for (String state : transitions.keySet()) {
            float angle = (float) (2 * Math.PI * i / numStates - Math.PI / 2);
            float x = centerX + radius * (float) Math.cos(angle);
            float y = centerY + radius * (float) Math.sin(angle);
            statePositions.put(state, new Float[]{x, y});
            i++;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (transitions == null) return;

        // Draw transitions (arrows)
        for (Map.Entry<String, HashMap<String, String>> fromState : transitions.entrySet()) {
            String from = fromState.getKey();
            for (Map.Entry<String, String> transition : fromState.getValue().entrySet()) {
                drawTransition(canvas, from, transition.getValue(), transition.getKey());
            }
        }

        // Draw states
        for (Map.Entry<String, Float[]> state : statePositions.entrySet()) {
            drawState(canvas, state.getKey(), state.getValue()[0], state.getValue()[1]);
        }
    }

    private void drawState(Canvas canvas, String state, float x, float y) {
        // Draw outer circle for final states
        if (finalStates.contains(state)) {
            canvas.drawCircle(x, y, STATE_RADIUS + 10, statePaint);
        }

        // Draw state circle
        canvas.drawCircle(x, y, STATE_RADIUS, statePaint);
        
        // Highlight current state
        if (state.equals(currentState)) {
            canvas.drawCircle(x, y, STATE_RADIUS, highlightPaint);
        }

        // Draw state label
        canvas.drawText(state, x, y + textPaint.getTextSize()/3, textPaint);
    }

    private void drawTransition(Canvas canvas, String from, String to, String input) {
        Float[] fromPos = statePositions.get(from);
        Float[] toPos = statePositions.get(to);
        
        if (fromPos == null || toPos == null) return;

        // Calculate arrow points
        float angle = (float) Math.atan2(toPos[1] - fromPos[1], toPos[0] - fromPos[0]);
        
        // Adjust start and end points to start from circle edges
        float startX = fromPos[0] + STATE_RADIUS * (float) Math.cos(angle);
        float startY = fromPos[1] + STATE_RADIUS * (float) Math.sin(angle);
        float endX = toPos[0] - STATE_RADIUS * (float) Math.cos(angle);
        float endY = toPos[1] - STATE_RADIUS * (float) Math.sin(angle);

        // Draw the line
        canvas.drawLine(startX, startY, endX, endY, arrowPaint);

        // Draw arrow head
        drawArrowHead(canvas, endX, endY, angle);

        // Draw transition label
        float labelX = (startX + endX) / 2;
        float labelY = (startY + endY) / 2 - LABEL_OFFSET;
        canvas.drawText(input, labelX, labelY, textPaint);
    }

    private void drawArrowHead(Canvas canvas, float tipX, float tipY, float angle) {
        Path arrowHead = new Path();
        float arrowAngle = (float) Math.toRadians(30); // 30 degree arrow head

        float x1 = tipX - ARROW_HEAD_SIZE * (float) Math.cos(angle - arrowAngle);
        float y1 = tipY - ARROW_HEAD_SIZE * (float) Math.sin(angle - arrowAngle);
        float x2 = tipX - ARROW_HEAD_SIZE * (float) Math.cos(angle + arrowAngle);
        float y2 = tipY - ARROW_HEAD_SIZE * (float) Math.sin(angle + arrowAngle);

        arrowHead.moveTo(tipX, tipY);
        arrowHead.lineTo(x1, y1);
        arrowHead.lineTo(x2, y2);
        arrowHead.close();

        canvas.drawPath(arrowHead, arrowPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (transitions != null) {
            calculateStatePositions();
        }
    }
} 