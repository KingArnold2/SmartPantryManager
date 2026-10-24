package com.richfield.smartpantry;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/** Shows the full ingredient list and method for the recipe passed via Intent. */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = new DatabaseHelper(this).getRecipe(recipeId);
        if (recipe == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setTitle("Recipe");
        ((TextView) findViewById(R.id.tv_detail_name)).setText(recipe.getName());

        StringBuilder sb = new StringBuilder();
        for (RecipeIngredient ing : recipe.getIngredients()) {
            sb.append("\u2022 ")
              .append(Formats.qty(ing.getQuantity())).append(" ")
              .append(ing.getUnit()).append(" ")
              .append(ing.getName()).append("\n");
        }
        ((TextView) findViewById(R.id.tv_detail_ingredients)).setText(sb.toString().trim());
        ((TextView) findViewById(R.id.tv_detail_steps)).setText(recipe.getSteps());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
