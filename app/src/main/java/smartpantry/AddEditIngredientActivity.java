package com.richfield.smartpantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

/**
 * One screen used for both CREATE (no extra) and UPDATE (EXTRA_ITEM_ID passed by Intent).
 * Includes input validation.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "item_id";

    private DatabaseHelper db;
    private TextInputLayout tilName, tilQty;
    private TextInputEditText etName, etQty, etExpiry;
    private Spinner spUnit;
    private ArrayAdapter<CharSequence> unitAdapter;
    private long itemId = -1; // -1 means "adding a new item"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = new DatabaseHelper(this);

        tilName = findViewById(R.id.til_name);
        tilQty = findViewById(R.id.til_qty);
        etName = findViewById(R.id.et_name);
        etQty = findViewById(R.id.et_qty);
        etExpiry = findViewById(R.id.et_expiry);
        spUnit = findViewById(R.id.sp_unit);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnClearDate = findViewById(R.id.btn_clear_date);

        unitAdapter = ArrayAdapter.createFromResource(this, R.array.units,
                android.R.layout.simple_spinner_item);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUnit.setAdapter(unitAdapter);
        spUnit.setSelection(unitAdapter.getCount() - 1); // default to "pcs"

        // Edit mode? Read the id passed in the Intent and pre-fill the form.
        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            setTitle("Edit Ingredient");
            Ingredient existing = db.getPantryItem(itemId);
            if (existing != null) {
                populateForm(existing);
            }
        } else {
            setTitle("Add Ingredient");
        }

        etExpiry.setOnClickListener(v -> showDatePicker());
        btnClearDate.setOnClickListener(v -> etExpiry.setText(""));
        btnSave.setOnClickListener(v -> save());
    }

    private void populateForm(Ingredient item) {
        etName.setText(item.getName());
        etQty.setText(Formats.qty(item.getQuantity()));
        etExpiry.setText(item.getExpiryDate() == null ? "" : item.getExpiryDate());
        for (int i = 0; i < unitAdapter.getCount(); i++) {
            if (unitAdapter.getItem(i).toString().equalsIgnoreCase(item.getUnit())) {
                spUnit.setSelection(i);
                break;
            }
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                etExpiry.setText(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    /** Validates the form, then inserts (add mode) or updates (edit mode). */
    private void save() {
        String name = etName.getText() == null ? "" : etName.getText().toString().trim();
        String qtyText = etQty.getText() == null ? "" : etQty.getText().toString().trim();
        String expiry = etExpiry.getText() == null ? "" : etExpiry.getText().toString().trim();
        String unit = spUnit.getSelectedItem().toString();

        tilName.setError(null);
        tilQty.setError(null);
        boolean valid = true;

        // --- Name validation ---
        if (name.isEmpty()) {
            tilName.setError("Please enter an ingredient name");
            valid = false;
        } else if (name.length() > 50) {
            tilName.setError("Name must be 50 characters or fewer");
            valid = false;
        } else if (!name.matches(".*[A-Za-z].*")) {
            tilName.setError("Name must contain letters");
            valid = false;
        }

        // --- Quantity validation ---
        double qty = 0;
        if (qtyText.isEmpty()) {
            tilQty.setError("Please enter a quantity");
            valid = false;
        } else {
            try {
                qty = Double.parseDouble(qtyText);
                if (qty <= 0) {
                    tilQty.setError("Quantity must be greater than 0");
                    valid = false;
                } else if (qty > 100000) {
                    tilQty.setError("Quantity is too large");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                tilQty.setError("Enter a valid number");
                valid = false;
            }
        }

        if (!valid) return;

        String expiryToStore = expiry.isEmpty() ? null : expiry;
        if (itemId == -1) {
            db.addPantryItem(new Ingredient(0, name, qty, unit, expiryToStore));
            Toast.makeText(this, name + " added to pantry", Toast.LENGTH_SHORT).show();
        } else {
            db.updatePantryItem(new Ingredient(itemId, name, qty, unit, expiryToStore));
            Toast.makeText(this, name + " updated", Toast.LENGTH_SHORT).show();
        }
        finish(); // back to the pantry list, which reloads in onResume()
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
