package com.carponcal.notalone;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

public class PreferenciasFragment extends PreferenceFragment {

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferencias);
        final EditTextPreference distancia = (EditTextPreference)findPreference( "distancia");


        distancia.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                    @Override public boolean onPreferenceChange(Preference preference, Object newValue) {
                        int valor;
                        try {
                            valor = Integer.parseInt((String)newValue);
                        }
                        catch(Exception e) {
                            Toast.makeText(getActivity(), "Ha de ser un número", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        if (valor>0) {
                            distancia.setSummary( "Distancia a buscar ("+valor+")");
                            return true;
                        }
                        else {
                            if (valor < 0) {
                                Toast.makeText(getActivity(), "Ha de ser numero positivo", Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            else{
                                distancia.setSummary( "No hay filtro de distancia");
                                return true;
                            }
                        }
                    }
                });





    }

}