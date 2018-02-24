package com.github.abstractkim.hapticsimulation;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by peter on 2/22/18.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {
    public final String TAG = this.getClass().getSimpleName();
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_haptic_simulation);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();


        //do test for getting wave file info to create entries and entryValues after updating media map
        List<Media> mediaList = new ArrayList<>(MediaManager.getInstance()
                .updateMediaMapFromContentResolver(getContext(), MediaManager.MIME_TYPE_FILTER_WAV)
                .getMediaMap().values());
        Collections.sort(mediaList,(m1, m2) -> m1.getTitle().compareTo(m2.getTitle()));

        int size = mediaList.size();
        CharSequence[] entries = new CharSequence[size];
        CharSequence[] entryValues = entries;

        int n = 0;
        for(Media media : mediaList){
            entries[n] = media.getTitle();
            n++;
        }

        for(int i = 0; i < count; i++){
            Preference p = prefScreen.getPreference(i);
            if((p instanceof ListPreference)){
                setListPreferenceEntriesAndValues(p, entries, entryValues);
                String value = sharedPreferences.getString(p.getKey(),
                        getString(R.string.pref_default_value));
                setPreferenceSummary(p, value);
            }
        }
    }


    private void setListPreferenceEntriesAndValues(Preference preference,
                                                   CharSequence[] entries,
                                                   CharSequence[] entryValues) {
        ListPreference listPreference = (ListPreference) preference;
        listPreference.setEntries(entries);
        listPreference.setEntryValues(entryValues);
    }

    private void setPreferenceSummary(Preference preference, String value) {
        if (preference instanceof ListPreference){
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if(prefIndex >=0){
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Figure out which preference was changed
        Preference preference = findPreference(key);
        Log.d(TAG, "onSharedPreferenceChanged()");
        if (null != preference) {
            // Updates the summary for the preference
            if(preference instanceof ListPreference){
                String value = sharedPreferences.getString(preference.getKey(), "");
                setPreferenceSummary(preference,value);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
