package com.richfield.smartpantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom RecyclerView adapter for the recipe cards. Each card shows the recipe name,
 * how many ingredients it needs, and a status line:
 *   green  - "You have everything"
 *   orange - exactly one ingredient missing (says which one and how much)
 *   grey   - "Missing N ingredients"
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface Listener {
        void onRecipeClick(Recipe recipe);
    }

    private static final int GREEN = 0xFF2E7D32;
    private static final int ORANGE = 0xFFE65100;
    private static final int GREY = 0xFF616161;

    private final Listener listener;
    private List<RecipeMatcher.Analysis> items = new ArrayList<>();

    public RecipeAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<RecipeMatcher.Analysis> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        RecipeMatcher.Analysis a = items.get(position);
        Recipe recipe = a.recipe;

        h.tvName.setText(recipe.getName());
        int n = recipe.getIngredients().size();
        h.tvCount.setText(n + (n == 1 ? " ingredient" : " ingredients"));

        if (a.missingCount == 0) {
            h.tvStatus.setText("\u2713 You have everything");
            h.tvStatus.setTextColor(GREEN);
        } else if (a.missingCount == 1) {
            RecipeIngredient m = a.missing;
            String amount = Formats.qty(a.shortBy) + " " + m.getUnit();
            h.tvStatus.setText(a.absent
                    ? "Missing: " + amount + " " + m.getName()
                    : "Short by " + amount + " of " + m.getName());
            h.tvStatus.setTextColor(ORANGE);
        } else {
            h.tvStatus.setText("Missing " + a.missingCount + " ingredients");
            h.tvStatus.setTextColor(GREY);
        }

        h.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName, tvCount, tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_recipe_name);
            tvCount = itemView.findViewById(R.id.tv_recipe_ingredients);
            tvStatus = itemView.findViewById(R.id.tv_recipe_status);
        }
    }
}
