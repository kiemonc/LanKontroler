package com.example.lankontroller;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class MySettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    //TODO sprawdzanie wpisywanych wartości

    private ApplicationConfig config;
    private EditTextPreference ipAdress;
    private EditTextPreference hysteresis;
    private EditTextPreference difference;

    public MySettingsFragment(ApplicationConfig config) {
        super();
        this.config = config;
    }




    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        ipAdress = (EditTextPreference) findPreference(getString(R.string.ip_adress_key));
        hysteresis = (EditTextPreference) findPreference(getString(R.string.hysteresis_key));
        difference  = (EditTextPreference) findPreference(getString(R.string.temperature_steps_key));
        setValues();
        setSummaries();

    }


    /**
     * ustawia wartości wyświetlane pod nawzą preferencji widoczne w SettingsActivity
     */
    private void setSummaries() {
        ipAdress.setSummary(ipAdress.getText());
        hysteresis.setSummary(hysteresis.getText());
        difference.setSummary(difference.getText());
    }


    /**
     * ustawia wartości po otwarciu okienka konkretnej preferencji
     */
    private void setValues() {
        ipAdress.setText(config.ipAdress);
        hysteresis.setText(Double.toString(config.hysteresis));
        difference.setText(Double.toString(config.temperatureSteps));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        if (pref instanceof EditTextPreference) {
            EditTextPreference listPref = (EditTextPreference) pref;
            listPref.setSummary(listPref.getText());
            if (getString(R.string.ip_adress_key) == key) {
                updateIpAdress();
            } else if (getString(R.string.hysteresis_key) == key) {
                updateHysteresis();
            } else if (getString(R.string.temperature_steps_key) == key) {
                updateDifference();
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);

    }

    private void updateIpAdress() {
        config.ipAdress = ipAdress.getText();
    }

    private void updateHysteresis() {
        config.hysteresis = Double.parseDouble(hysteresis.getText());
    }

    private void updateDifference() {
        config.temperatureSteps = Double.parseDouble(difference.getText());
    }
}
