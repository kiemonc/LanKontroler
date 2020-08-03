package com.example.lankontroller;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class MySettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    //TODO sprawdzanie wpisywanych wartości

    private ApplicationConfig config;
    private EditTextPreference ipAdress;
    private EditTextPreference hysteresis;
    private EditTextPreference difference;
    private EditTextPreference email;

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
        email = (EditTextPreference) findPreference(getString(R.string.email_adress_key));

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
        email.setSummary(email.getText());
    }


    /**
     * ustawia wartości po otwarciu okienka konkretnej preferencji
     */
    private void setValues() {
        ipAdress.setText(config.ipAdress);
        hysteresis.setText(Double.toString(config.hysteresis));
        difference.setText(Double.toString(config.temperatureSteps));
        email.setText(config.emailAdress);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);

        String buff;

            if (pref instanceof EditTextPreference) {
                EditTextPreference listPref = (EditTextPreference) pref;
                //zapamiętanie wartości przed zmianą, by potem w przypaku błędnej wartości móc ją wpisać jako warość
                buff = (String) listPref.getSummary();

                try {
                    if (getString(R.string.ip_adress_key) == key) {
                        updateIpAdress();
                    } else if (getString(R.string.hysteresis_key) == key) {
                        updateHysteresis();
                    } else if (getString(R.string.temperature_steps_key) == key) {
                        updateDifference();
                    } else if (getString(R.string.email_adress_key) == key) {
                        updateEmailAdress();
                    }
                    listPref.setSummary(listPref.getText());
                } catch(NumberFormatException e) {
                    listPref.setText(buff);
                    UserDialog exampleDialog = new UserDialog(getString(R.string.information), getString(R.string.bad_format_number));
                    exampleDialog.show(getChildFragmentManager(),"Zły format");
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
        double number = Double.parseDouble(hysteresis.getText());
        if(number < 0) {
            throw new NumberFormatException();
        } else {
            config.hysteresis = number;
        }
    }

    private void updateDifference() {
        double number = Double.parseDouble(difference.getText());
        if(number < 0) {
            throw new NumberFormatException();
        } else {
            config.temperatureSteps = number;
        }
    }

    private void updateEmailAdress() {
        config.emailAdress = email.getText();

    }
}
