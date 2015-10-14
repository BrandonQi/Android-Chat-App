package edu.stevens.cs522.chat.activities;

import android.app.Activity;
import android.os.Bundle;

public class activityPreference extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new fragmentPreference()).commit();
    }

}
