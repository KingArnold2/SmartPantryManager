package com.richfield.smartpantry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CORE BUSINESS LOGIC - the strict-matching rule (assignment section 2.3).
 *
 * A recipe is "ready to cook" ONLY if EVERY ingredient it requires is in the pantry
 * in at least the required quantity. One missing or insufficient ingredient means the
 * recipe is NOT in the strict list.
 *
 * analyseAll() works out, for every recipe, HOW MANY ingredients are missing:
 *   0 missing  -> "Ready to cook"  (the strict suggestions list)
 *   1 missing  -> "Almost there"   (bonus list, kept separate from the strict list)
 *   2+ missing -> only visible in the "All recipes" browse view
 *
 * To be robust to real-world messiness the matcher:
 *  - ignores case and extra spaces ("  Tomato " == "tomato")
 *  - treats singular/plural as the same ("tomato" == "tomatoes", "egg" == "eggs")
 *  - converts compatible units ("1 kg" satisfies "500 g", "1 cup" satisfies "10 tbsp")
 *  - adds up several pantry rows of the same ingredient (2 + 4 eggs = 6 eggs)
 */
public final class RecipeMatcher {

    private static final double EPS = 1e-9;

    private RecipeMatcher() { }

    // ------------------------------------------------------------------
    // Result of analysing one recipe against the pantry
    // ------------------------------------------------------------------

    public static final class Analysis {
        public final Recipe recipe;
        public final int missingCount;          // how many ingredient lines are not covered
        public final RecipeIngredient missing;  // the missing ingredient (only set if missingCount == 1)
        public final double shortBy;            // amount still needed, in that ingredient's own unit
        public final boolean absent;            // true = not in pantry at all, false = not enough

        Analysis(Recipe recipe, int missingCount, RecipeIngredient missing,
                 double shortBy, boolean absent) {
            this.recipe = recipe;
            this.missingCount = missingCount;
            this.missing = missing;
            this.shortBy = shortBy;
            this.absent = absent;
        }
    }

    /**
     * Analyses every recipe. The list is sorted with the recipes closest to being
     * cookable first (fewest missing ingredients), then alphabetically.
     */
    public static List<Analysis> analyseAll(List<Ingredient> pantry, List<Recipe> allRecipes) {
        Map<String, Double> stock = buildStock(pantry);
        List<Analysis> result = new ArrayList<>();

        for (Recipe recipe : allRecipes) {
            if (recipe.getIngredients().isEmpty()) continue;

            int gaps = 0;
            RecipeIngredient lastMissing = null;
            double shortBy = 0;
            boolean absent = false;

            for (RecipeIngredient needed : recipe.getIngredients()) {
                Double haveObj = stock.get(key(needed.getName(), needed.getUnit()));
                double have = haveObj == null ? 0 : haveObj;
                double need = toBase(needed.getQuantity(), needed.getUnit());
                if (have + EPS < need) {
                    gaps++;
                    lastMissing = needed;
                    absent = haveObj == null;
                    shortBy = (need - have) / toBase(1, needed.getUnit());
                }
            }
            result.add(new Analysis(recipe, gaps, gaps == 1 ? lastMissing : null, shortBy, absent));
        }

        Collections.sort(result, (a, b) -> {
            if (a.missingCount != b.missingCount) {
                return Integer.compare(a.missingCount, b.missingCount);
            }
            return a.recipe.getName().compareToIgnoreCase(b.recipe.getName());
        });
        return result;
    }

    /** The strict list: only recipes the user can make right now (nothing missing). */
    public static List<Recipe> getSuggestedRecipes(List<Ingredient> pantry, List<Recipe> allRecipes) {
        Map<String, Double> stock = buildStock(pantry);
        List<Recipe> result = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (canMake(recipe, stock)) {
                result.add(recipe);
            }
        }
        return result;
    }

    /** True only if EVERY required ingredient is available in sufficient quantity. */
    private static boolean canMake(Recipe recipe, Map<String, Double> stock) {
        if (recipe.getIngredients().isEmpty()) return false;
        for (RecipeIngredient needed : recipe.getIngredients()) {
            Double have = stock.get(key(needed.getName(), needed.getUnit()));
            if (have == null || have + EPS < toBase(needed.getQuantity(), needed.getUnit())) {
                return false; // missing or not enough -> whole recipe is rejected
            }
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Stock building
    // ------------------------------------------------------------------

    /** Total pantry stock keyed by "normalisedName#dimension", stored in base units. */
    private static Map<String, Double> buildStock(List<Ingredient> pantry) {
        Map<String, Double> stock = new HashMap<>();
        for (Ingredient item : pantry) {
            if (item.getQuantity() <= 0) continue;
            String key = key(item.getName(), item.getUnit());
            double base = toBase(item.getQuantity(), item.getUnit());
            stock.put(key, stock.getOrDefault(key, 0.0) + base);
        }
        return stock;
    }

    private static String key(String name, String unit) {
        return normalizeName(name) + "#" + dimension(unit);
    }

    // ------------------------------------------------------------------
    // Name normalisation
    // ------------------------------------------------------------------

    /** Lower-cases, trims and singularises the last word: "Tomatoes" -> "tomato". */
    public static String normalizeName(String raw) {
        if (raw == null) return "";
        String s = raw.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        int lastSpace = s.lastIndexOf(' ');
        String head = lastSpace >= 0 ? s.substring(0, lastSpace + 1) : "";
        String last = lastSpace >= 0 ? s.substring(lastSpace + 1) : s;
        return head + singular(last);
    }

    /** Simple English singularisation - enough for ingredient names, not full NLP. */
    private static String singular(String w) {
        if (w.length() <= 3) return w;
        if (w.endsWith("ies")) return w.substring(0, w.length() - 3) + "y";      // berries -> berry
        if (w.endsWith("oes")) return w.substring(0, w.length() - 2);            // tomatoes -> tomato
        if (w.endsWith("ches") || w.endsWith("shes") || w.endsWith("xes")) {
            return w.substring(0, w.length() - 2);                               // peaches -> peach
        }
        if (w.endsWith("ss") || w.endsWith("us")) return w;                      // couscous, glass
        if (w.endsWith("s")) return w.substring(0, w.length() - 1);              // eggs -> egg
        return w;
    }

    // ------------------------------------------------------------------
    // Unit handling
    // ------------------------------------------------------------------

    /** Canonical unit: "Grams" -> "g", "Tablespoons" -> "tbsp", unknown/blank -> "pcs". */
    private static String normalizeUnit(String raw) {
        if (raw == null) return "pcs";
        String u = raw.trim().toLowerCase(Locale.ROOT).replace(".", "");
        if (u.endsWith("s") && u.length() > 1) u = u.substring(0, u.length() - 1); // grams -> gram
        switch (u) {
            case "g": case "gram": case "gm": return "g";
            case "kg": case "kilogram": return "kg";
            case "ml": case "milliliter": case "millilitre": return "ml";
            case "l": case "litre": case "liter": return "l";
            case "tsp": case "teaspoon": return "tsp";
            case "tbsp": case "tablespoon": return "tbsp";
            case "cup": return "cup";
            default: return "pcs"; // pcs, piece, whole, clove, can, blank...
        }
    }

    /** Which kind of measurement a unit is: mass, volume or count. */
    private static String dimension(String unit) {
        switch (normalizeUnit(unit)) {
            case "g": case "kg": return "mass";
            case "ml": case "l": case "tsp": case "tbsp": case "cup": return "volume";
            default: return "count";
        }
    }

    /** Converts to base units: grams, millilitres or pieces. */
    private static double toBase(double qty, String unit) {
        switch (normalizeUnit(unit)) {
            case "kg": return qty * 1000;
            case "l": return qty * 1000;
            case "tsp": return qty * 5;
            case "tbsp": return qty * 15;
            case "cup": return qty * 240;
            default: return qty; // g, ml, pcs
        }
    }
}
