package com.richfield.smartpantry;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

/**
 * Pre-loads the recipe collection the first time the database is created.
 * Ingredient format: "name|quantity|unit".
 */
public final class SeedData {
    private SeedData() { }

    public static void seed(SQLiteDatabase db) {
        add(db, "Scrambled Eggs on Toast",
                "1. Whisk the eggs with the salt.\n2. Melt the butter in a pan over medium heat.\n3. Add the eggs and stir gently until just set.\n4. Toast the bread and serve the eggs on top.",
                "egg|2|pcs", "bread|2|pcs", "butter|1|tbsp", "salt|1|tsp");
        add(db, "Plain Omelette",
                "1. Beat the eggs with the salt.\n2. Melt the butter in a non-stick pan.\n3. Pour in the eggs and cook until nearly set.\n4. Fold in half and serve.",
                "egg|3|pcs", "butter|1|tbsp", "salt|1|tsp");
        add(db, "Cheese Omelette",
                "1. Beat the eggs.\n2. Melt the butter in a pan and add the eggs.\n3. When nearly set, sprinkle over the cheese.\n4. Fold in half and cook for 1 more minute.",
                "egg|3|pcs", "cheese|40|g", "butter|1|tbsp");
        add(db, "Cheese Toastie",
                "1. Butter the outside of both bread slices.\n2. Place the cheese between the slices.\n3. Fry in a pan for 2-3 minutes each side until golden.",
                "bread|2|pcs", "cheese|50|g", "butter|1|tbsp");
        add(db, "Tomato Pasta",
                "1. Boil the pasta in salted water until al dente.\n2. Fry the chopped garlic in olive oil.\n3. Add chopped tomatoes and simmer for 10 minutes.\n4. Toss the drained pasta through the sauce.",
                "pasta|200|g", "tomato|3|pcs", "garlic|2|pcs", "olive oil|2|tbsp", "salt|1|tsp");
        add(db, "Spaghetti Aglio e Olio",
                "1. Cook the pasta until al dente.\n2. Gently fry sliced garlic in olive oil until golden.\n3. Toss in the pasta with a splash of cooking water and the salt.",
                "pasta|200|g", "garlic|4|pcs", "olive oil|3|tbsp", "salt|1|tsp");
        add(db, "Tuna Pasta",
                "1. Cook the pasta.\n2. Fry the diced onion in olive oil until soft.\n3. Stir in the drained tuna and heat through.\n4. Mix with the pasta and serve.",
                "pasta|200|g", "tuna|1|pcs", "onion|1|pcs", "olive oil|1|tbsp");
        add(db, "Egg Fried Rice",
                "1. Cook the rice and let it cool.\n2. Fry the chopped onion in oil.\n3. Push aside, scramble the eggs in the pan.\n4. Add the rice and soy sauce and stir-fry for 3 minutes.",
                "rice|1|cup", "egg|2|pcs", "onion|1|pcs", "oil|2|tbsp", "soy sauce|2|tbsp");
        add(db, "Tomato Rice",
                "1. Fry the chopped onion in oil.\n2. Add chopped tomatoes and cook until soft.\n3. Stir in the rice, salt and 2 cups of water.\n4. Cover and simmer for 15 minutes.",
                "rice|1|cup", "tomato|2|pcs", "onion|1|pcs", "oil|1|tbsp", "salt|1|tsp");
        add(db, "Pancakes",
                "1. Whisk flour, milk, egg and sugar into a smooth batter.\n2. Melt a little butter in a pan.\n3. Pour in a ladle of batter and cook until bubbles form.\n4. Flip and cook until golden.",
                "flour|200|g", "milk|300|ml", "egg|1|pcs", "sugar|1|tbsp", "butter|20|g");
        add(db, "Mashed Potatoes",
                "1. Peel and chop the potatoes.\n2. Boil until soft, about 15 minutes.\n3. Drain and mash with the butter, milk and salt.",
                "potato|4|pcs", "butter|30|g", "milk|100|ml", "salt|1|tsp");
        add(db, "Cheesy Baked Potato",
                "1. Prick the potatoes and bake at 200C for 60 minutes.\n2. Cut open and fluff the inside with a fork.\n3. Add butter and top with the cheese.",
                "potato|2|pcs", "cheese|60|g", "butter|1|tbsp");
        add(db, "Banana Oats",
                "1. Mash the banana.\n2. Combine with the oats and milk in a pot.\n3. Cook over low heat for 5 minutes, stirring.",
                "oats|50|g", "banana|1|pcs", "milk|200|ml");
        add(db, "French Toast",
                "1. Whisk the eggs, milk and sugar.\n2. Soak each bread slice in the mixture.\n3. Fry in butter until golden on both sides.",
                "bread|4|pcs", "egg|2|pcs", "milk|100|ml", "sugar|1|tbsp", "butter|1|tbsp");
        add(db, "Vegetable Soup",
                "1. Chop the carrots, potatoes and onion.\n2. Simmer them in the stock with the salt for 25 minutes.\n3. Blend for a smooth soup or serve chunky.",
                "carrot|2|pcs", "potato|2|pcs", "onion|1|pcs", "vegetable stock|500|ml", "salt|1|tsp");
        add(db, "Garlic Bread",
                "1. Mash the butter with the crushed garlic.\n2. Spread over the bread slices.\n3. Bake at 190C for 8-10 minutes until crisp.",
                "bread|4|pcs", "butter|40|g", "garlic|2|pcs");
        add(db, "Peanut Butter Banana Sandwich",
                "1. Spread the peanut butter on both slices of bread.\n2. Slice the banana and layer on one slice.\n3. Close the sandwich and cut in half.",
                "bread|2|pcs", "peanut butter|2|tbsp", "banana|1|pcs");
        add(db, "Chicken Stir-Fry",
                "1. Slice the chicken, onion and pepper.\n2. Heat the oil in a wok and brown the chicken.\n3. Add the vegetables and stir-fry for 5 minutes.\n4. Add the soy sauce and toss to coat.",
                "chicken breast|300|g", "onion|1|pcs", "bell pepper|1|pcs", "soy sauce|2|tbsp", "oil|1|tbsp");
        add(db, "Chicken and Rice",
                "1. Season the chicken with salt and brown in a pot.\n2. Add the chopped onion and cook until soft.\n3. Add the rice and 2 cups of water.\n4. Cover and simmer for 18 minutes.",
                "chicken breast|300|g", "rice|1|cup", "onion|1|pcs", "salt|1|tsp");
        add(db, "Egg Mayo Sandwich",
                "1. Hard-boil the eggs for 10 minutes, then peel.\n2. Mash with the mayonnaise.\n3. Spread on the bread and close the sandwich.",
                "egg|2|pcs", "bread|2|pcs", "mayonnaise|2|tbsp");
        add(db, "Lentil Curry",
                "1. Fry the chopped onion in oil.\n2. Stir in the curry powder.\n3. Add chopped tomatoes, lentils and 3 cups of water.\n4. Simmer for 25 minutes until the lentils are soft.",
                "lentil|1|cup", "onion|1|pcs", "tomato|2|pcs", "curry powder|1|tbsp", "oil|1|tbsp");
        add(db, "Baked Beans on Toast",
                "1. Toast the bread and butter it.\n2. Heat the beans in a small pot.\n3. Pour over the toast.",
                "baked beans|1|pcs", "bread|2|pcs", "butter|1|tbsp");
    }

    private static void add(SQLiteDatabase db, String name, String steps, String... ingredients) {
        ContentValues recipe = new ContentValues();
        recipe.put(DatabaseHelper.COL_NAME, name);
        recipe.put(DatabaseHelper.COL_STEPS, steps);
        long recipeId = db.insert(DatabaseHelper.TABLE_RECIPES, null, recipe);

        for (String line : ingredients) {
            String[] p = line.split("\\|");
            ContentValues ing = new ContentValues();
            ing.put(DatabaseHelper.COL_RECIPE_ID, recipeId);
            ing.put(DatabaseHelper.COL_NAME, p[0]);
            ing.put(DatabaseHelper.COL_QUANTITY, Double.parseDouble(p[1]));
            ing.put(DatabaseHelper.COL_UNIT, p[2]);
            db.insert(DatabaseHelper.TABLE_RECIPE_INGREDIENTS, null, ing);
        }
    }
}
