package edu.stevens.cs522.chat.activities;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import edu.stevens.cs522.chat.R;

public class fragmentPreference extends PreferenceFragment {
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.preferences);
    }
}
