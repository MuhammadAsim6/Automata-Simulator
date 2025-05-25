package com.example.toaproj;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View logoImage = findViewById(R.id.logoImage);
        View setupButton = findViewById(R.id.setupAutomataButton);

        // Add animations
        logoImage.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        setupButton.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left));

        setupButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            Intent intent = new Intent(MainActivity.this, SetupAutomataActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_credits) {
            showCreditsDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showCreditsDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Credits")
            .setMessage("Theory of Automata Simulator\n\nCreated by: Asim\n\nVersion 1.0")
            .setPositiveButton("OK", null)
            .show();
    }
}

