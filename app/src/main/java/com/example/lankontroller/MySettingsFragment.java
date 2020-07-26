package com.example.lankontroller;

import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

public class MySettingsFragment extends PreferenceFragmentCompat {

    private ApplicationConfig config;

    /*
    public MySettingsFragment(ApplicationConfig config) {
        super();
        this.config = config;
    }

     */

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        EditTextPreference ipAdress = (EditTextPreference) findPreference("ipAdress");
        ipAdress.setDefaultValue("xd");
    }
}
