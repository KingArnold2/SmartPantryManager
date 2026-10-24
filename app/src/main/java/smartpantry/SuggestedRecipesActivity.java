package com.richfield.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Suggested Recipes screen with three views (filter buttons at the top):
 *  - READY TO COOK : the strict list - ONLY recipes with nothing missing (default view)
 *  - ALMOST THERE  : recipes missing exactly one ingredient (bonus, kept separate)
 *  - ALL RECIPES   : every recipe, closest-to-cookable first, each showing what is missing
 */
public class SuggestedRecipesActivity extends BaseActivity implements RecipeAdapter.Listener {

    private enum Filter { READY, ALMOST, ALL }

    private DatabaseHelper db;
    private RecipeAdapter adapter;
    private RecyclerView rvRecipes;
    private TextView tvEmpty, tvSummary;
    private Filter filter = Filter.READY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        setTitle("Suggested Recipes");

        db = new DatabaseHelper(this);
        rvRecipes = findViewById(R.id.rv_recipes);
        tvEmpty = findViewById(R.id.tv_empty);
        tvSummary = findViewById(R.id.tv_summary);

        adapter = new RecipeAdapter(this);
        rvRecipes.setLayoutManager(new LinearLayoutManager(this));
        rvRecipes.setAdapter(adapter);

        // Filter buttons: only one can be selected at a time
        MaterialButtonToggleGroup group = findViewById(R.id.filter_group);
        group.check(R.id.btn_filter_ready);
        group.addOnButtonCheckedListener((g, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btn_filter_ready) {
                filter = Filter.READY;
            } else if (checkedId == R.id.btn_filter_almost) {
                filter = Filter.ALMOST;
            } else {
                filter = Filter.ALL;
            }
            refresh();
        });

        setupBottomNav(R.id.nav_recipes);
    }

    /** Re-run the matching every time the screen is shown, so it reflects pantry changes. */
    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        List<Ingredient> pantry = db.getAllPantryItems();
        List<Recipe> all = db.getAllRecipes();
        List<RecipeMatcher.Analysis> analysed = RecipeMatcher.analyseAll(pantry, all);

        // Keep only the recipes that belong in the selected view
        List<RecipeMatcher.Analysis> shown = new ArrayList<>();
        for (RecipeMatcher.Analysis a : analysed) {
            boolean keep = filter == Filter.ALL
                    || (filter == Filter.READY && a.missingCount == 0)
                    || (filter == Filter.ALMOST && a.missingCount == 1);
            if (keep) shown.add(a);
        }
        adapter.setItems(shown);

        switch (filter) {
            case READY:
                tvSummary.setText("You can cook " + shown.size() + " of " + all.size()
                        + " recipes right now");
                tvEmpty.setText("No recipes match your pantry yet - add more ingredients.");
                break;
            case ALMOST:
                tvSummary.setText(shown.size() + " recipe(s) missing just one ingredient");
                tvEmpty.setText("No recipes are missing just one ingredient right now.");
                break;
            default:
                tvSummary.setText("Showing all " + all.size()
                        + " recipes - closest to cookable first");
                tvEmpty.setText("No recipes found.");
                break;
        }

        // Friendly message instead of a blank screen
        boolean none = shown.isEmpty();
        tvEmpty.setVisibility(none ? View.VISIBLE : View.GONE);
        rvRecipes.setVisibility(none ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
