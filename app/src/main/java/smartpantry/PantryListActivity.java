package com.richfield.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Home screen - lists every ingredient in the pantry (READ), and lets the user
 * open the edit screen (UPDATE) or delete an item (DELETE).
 */
public class PantryListActivity extends BaseActivity implements PantryAdapter.Listener {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private TextView tvEmpty;
    private TextView tvBanner;
    private Button btnSuggest;
    private RecyclerView rvPantry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);
        setTitle("My Pantry");

        db = new DatabaseHelper(this);

        rvPantry = findViewById(R.id.rv_pantry);
        tvEmpty = findViewById(R.id.tv_empty);
        tvBanner = findViewById(R.id.tv_banner);
        FloatingActionButton fab = findViewById(R.id.fab_add);
        btnSuggest = findViewById(R.id.btn_suggest);

        // Intent -> open the Suggested Recipes screen
        btnSuggest.setOnClickListener(v ->
                startActivity(new Intent(this, SuggestedRecipesActivity.class)));

        adapter = new PantryAdapter(this);
        rvPantry.setLayoutManager(new LinearLayoutManager(this));
        rvPantry.setAdapter(adapter);

        // Intent -> open the Add/Edit screen in "add" mode
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));

        setupBottomNav(R.id.nav_pantry);
    }

    /** Reload from the database every time the screen becomes visible (e.g. after saving). */
    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    private void loadPantry() {
        List<Ingredient> items = db.getAllPantryItems();
        boolean alerts = Prefs.isExpiryAlertsEnabled(this);
        adapter.setItems(items, alerts);

        // Show how many recipes can be cooked with the current pantry (strict-matching rule)
        int canCook = RecipeMatcher.getSuggestedRecipes(items, db.getAllRecipes()).size();
        btnSuggest.setText("What can I cook? (" + canCook + ")");

        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        rvPantry.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);

        // Expiring-soon banner (controlled by the Settings screen)
        int expiring = 0;
        if (alerts) {
            for (Ingredient i : items) {
                if (ExpiryUtils.isExpiringSoon(i.getExpiryDate())) expiring++;
            }
        }
        if (expiring > 0) {
            tvBanner.setText("\u26A0 " + expiring + " item(s) expiring within "
                    + ExpiryUtils.SOON_DAYS + " days - cook with them soon!");
            tvBanner.setVisibility(View.VISIBLE);
        } else {
            tvBanner.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(Ingredient item) {
        // Intent with extra -> open the Add/Edit screen in "edit" mode
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Ingredient item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove " + item.getName() + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.deletePantryItem(item.getId());
                    Toast.makeText(this, item.getName() + " deleted", Toast.LENGTH_SHORT).show();
                    loadPantry();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
