package com.example.lankontroller;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    private ApplicationConfig config;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        //Pobiera konfiguracje z MainActivity
        Intent thisIntent = getIntent();
        config = (ApplicationConfig) thisIntent.getSerializableExtra("config");

        //włącza przycisk cofania
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new MySettingsFragment(config))
                .commit();
    }


    @Override
    public void finish() {
        mySetResult();
        super.finish();
    }

    /**
     * Ustawia resultat okna ustwień.
     * Przekazuje referencje do objektu ustawień
     */
    private void mySetResult() {
        int resultCode = 1;
        Intent resultIntent = new Intent();
        resultIntent.putExtra("config", config);
        setResult(resultCode, resultIntent);
    }


    /**
     * Kliknięcie przycisku
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //Klikniecie przycisku powrotu na górze
        if (id==android.R.id.home) {
            mySetResult();
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}