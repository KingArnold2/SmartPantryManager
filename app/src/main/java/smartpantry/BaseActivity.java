package com.richfield.smartpantry;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Parent class for the three top-level screens (Pantry, Recipes, Settings).
 * It wires up the bottom navigation bar and uses Intents to move between screens.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected void setupBottomNav(int selectedItemId) {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(selectedItemId);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) return true; // already here

            Class<?> target;
            if (id == R.id.nav_pantry) {
                target = PantryListActivity.class;
            } else if (id == R.id.nav_recipes) {
                target = SuggestedRecipesActivity.class;
            } else {
                target = SettingsActivity.class;
            }
            Intent intent = new Intent(this, target);
            // Re-use an existing instance of the target screen instead of stacking copies
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            return false; // keep highlight on the current screen's tab
        });
    }
}
