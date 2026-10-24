package com.richfield.smartpantry;

import android.os.Bundle;

import com.google.android.material.switchmaterial.SwitchMaterial;

/** Settings screen - toggle for expiring-soon alerts, saved in SharedPreferences. */
public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Settings");

        SwitchMaterial switchAlerts = findViewById(R.id.switch_alerts);
        switchAlerts.setChecked(Prefs.isExpiryAlertsEnabled(this));
        switchAlerts.setOnCheckedChangeListener((button, isChecked) ->
                Prefs.setExpiryAlertsEnabled(this, isChecked));

        setupBottomNav(R.id.nav_settings);
    }
}
