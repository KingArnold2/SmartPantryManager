package com.richfield.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite database for the app (local, on-device).
 *
 * Tables:
 *  - pantry              : the user's ingredients (full CRUD)
 *  - recipes             : pre-loaded recipe collection (seeded on first run)
 *  - recipe_ingredients  : ingredients each recipe requires (FK -> recipes.id)
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_PANTRY = "pantry";
    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";

    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_UNIT = "unit";
    public static final String COL_EXPIRY = "expiry_date";
    public static final String COL_STEPS = "steps";
    public static final String COL_RECIPE_ID = "recipe_id";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true); // enforce FK + ON DELETE CASCADE
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_QUANTITY + " REAL NOT NULL, "
                + COL_UNIT + " TEXT NOT NULL, "
                + COL_EXPIRY + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_STEPS + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RECIPE_ID + " INTEGER NOT NULL, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_QUANTITY + " REAL NOT NULL, "
                + COL_UNIT + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + COL_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COL_ID + ") ON DELETE CASCADE)");

        SeedData.seed(db); // pre-load recipes on first run
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple strategy for this assignment: rebuild everything.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // ------------------------------------------------------------------
    // PANTRY CRUD
    // ------------------------------------------------------------------

    private ContentValues toValues(Ingredient i) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, i.getName());
        cv.put(COL_QUANTITY, i.getQuantity());
        cv.put(COL_UNIT, i.getUnit());
        cv.put(COL_EXPIRY, i.getExpiryDate());
        return cv;
    }

    private Ingredient fromCursor(Cursor c) {
        return new Ingredient(
                c.getLong(c.getColumnIndexOrThrow(COL_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(COL_QUANTITY)),
                c.getString(c.getColumnIndexOrThrow(COL_UNIT)),
                c.getString(c.getColumnIndexOrThrow(COL_EXPIRY)));
    }

    /** CREATE - returns the new row id, or -1 on failure. */
    public long addPantryItem(Ingredient item) {
        return getWritableDatabase().insert(TABLE_PANTRY, null, toValues(item));
    }

    /** READ (list) - all pantry items sorted by name. */
    public List<Ingredient> getAllPantryItems() {
        List<Ingredient> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(TABLE_PANTRY, null, null, null,
                null, null, COL_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                list.add(fromCursor(c));
            }
        }
        return list;
    }

    /** READ (single) - returns null if the id does not exist. */
    public Ingredient getPantryItem(long id) {
        try (Cursor c = getReadableDatabase().query(TABLE_PANTRY, null, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? fromCursor(c) : null;
        }
    }

    /** UPDATE - returns the number of rows changed. */
    public int updatePantryItem(Ingredient item) {
        return getWritableDatabase().update(TABLE_PANTRY, toValues(item), COL_ID + "=?",
                new String[]{String.valueOf(item.getId())});
    }

    /** DELETE - returns the number of rows removed. */
    public int deletePantryItem(long id) {
        return getWritableDatabase().delete(TABLE_PANTRY, COL_ID + "=?",
                new String[]{String.valueOf(id)});
    }

    // ------------------------------------------------------------------
    // RECIPES (read-only for the user)
    // ------------------------------------------------------------------

    /** All recipes, each with its ingredient list loaded. */
    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_RECIPES, null, null, null, null, null,
                COL_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                Recipe r = new Recipe(
                        c.getLong(c.getColumnIndexOrThrow(COL_ID)),
                        c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                        c.getString(c.getColumnIndexOrThrow(COL_STEPS)));
                loadIngredients(db, r);
                list.add(r);
            }
        }
        return list;
    }

    /** A single recipe by id (null if not found). */
    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_RECIPES, null, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (!c.moveToFirst()) return null;
            Recipe r = new Recipe(
                    c.getLong(c.getColumnIndexOrThrow(COL_ID)),
                    c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                    c.getString(c.getColumnIndexOrThrow(COL_STEPS)));
            loadIngredients(db, r);
            return r;
        }
    }

    private void loadIngredients(SQLiteDatabase db, Recipe recipe) {
        try (Cursor c = db.query(TABLE_RECIPE_INGREDIENTS, null, COL_RECIPE_ID + "=?",
                new String[]{String.valueOf(recipe.getId())}, null, null, COL_ID + " ASC")) {
            while (c.moveToNext()) {
                recipe.getIngredients().add(new RecipeIngredient(
                        c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                        c.getDouble(c.getColumnIndexOrThrow(COL_QUANTITY)),
                        c.getString(c.getColumnIndexOrThrow(COL_UNIT))));
            }
        }
    }
}
