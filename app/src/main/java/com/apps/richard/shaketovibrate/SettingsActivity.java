/*****************************************************************************

 Richard Huang
 March 27, 2015
 SettingsActivity.java

 Description: This activity displays the settings that the user can modify.
              At the moment, three generic settings can be modified:
              A checkbox, a list, and an editable text. These settings can
              be modified in the preferences.xml located in the xml folder.

 ****************************************************************************/


package com.apps.richard.shaketovibrate;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Name: SettingsActivity (class)
 * Description: Instantiates a basic settings activity that the user can modify
 */
public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    /*
     * Inner class that uses a PreferenceFragment to instantiate the
     * settings to display to the user. The settings are loaded by the
     * fragment from preferences.xml in the res/values/xml folder.
     */
    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
