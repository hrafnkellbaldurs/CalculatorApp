package com.example.hrafnkell.calculatorapp;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Hrafnkell on 28/8/2015.
 */
public class CalcPreferenceActivity extends PreferenceActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
