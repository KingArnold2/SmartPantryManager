# Smart Pantry Manager

A Java Android application that helps reduce food waste. The user records the ingredients they
already have at home (their "pantry") and the app suggests recipes they can cook using **strictly**
those ingredients - a recipe is only suggested if the user has *every* ingredient it needs,
in at least the required quantity.

Built for **Mobile App Development 700 - Practical Assignment**.

## Features
- Add, edit and delete pantry items (name, quantity, unit, optional expiry date) with input validation
- Pantry list screen (RecyclerView + custom adapter bound to SQLite)
- 22 pre-loaded recipes (seeded on first run)
- Suggested Recipes screen using the strict-matching rule (`RecipeMatcher.java`)
  - ignores case, handles singular/plural ("tomato" = "tomatoes"), converts units (kg/g, cup/tbsp/ml...)
  - friendly message when no recipes match
- Recipe detail screen (ingredients + method)
- Settings screen (expiring-soon highlight toggle, stored in SharedPreferences)
- Bottom navigation bar (Pantry / Recipes / Settings)
- No Google Maps, no location/GPS features

## Database choice: SQLite
SQLite (via `SQLiteOpenHelper`) was chosen because the app is purely personal and offline: the pantry
only needs to live on the user's device, no account or internet is required, SQLite ships with Android
(no extra dependency or server to maintain), and it supports everything needed - relational tables,
foreign keys and full CRUD that persists after the app is closed.

Tables: `pantry`, `recipes`, `recipe_ingredients` (FK -> `recipes.id`).

## Setup / Run instructions
1. Install **Android Studio** (Hedgehog or newer) with Android SDK 34.
2. Clone the repo: `git clone <your-repo-url>`
3. In Android Studio choose **File > Open** and select the `SmartPantryManager` folder.
4. Wait for the Gradle sync to finish (internet needed the first time).
5. Create/start an emulator (API 24+) or plug in a device with USB debugging on.
6. Press **Run** (green triangle).

## Project structure
| File | Purpose |
|------|---------|
| `PantryListActivity` | Pantry list, delete, navigation to add/edit |
| `AddEditIngredientActivity` | Add & edit form with validation |
| `SuggestedRecipesActivity` | Shows strictly-matching recipes |
| `RecipeDetailActivity` | Full recipe |
| `SettingsActivity` | Settings toggle |
| `DatabaseHelper` / `SeedData` | SQLite schema, CRUD and recipe seeding |
| `RecipeMatcher` | Strict-matching business logic |
| `PantryAdapter` / `RecipeAdapter` | RecyclerView adapters |
