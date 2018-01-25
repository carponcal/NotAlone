package com.carponcal.notalone;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class PreferenciasActivity extends PreferenceActivity {


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PreferenciasFragment())
                .commit();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

    }






    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}
