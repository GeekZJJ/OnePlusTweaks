package com.geekzjj.oneplustweaks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

@SuppressLint("SetWorldReadable")
@SuppressWarnings({"deprecation","ResultOfMethodCallIgnored"})
public class SettingsActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static  final String TAG =  "SettingsActivity";
    SharedPreferences SP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();

        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext().createDeviceProtectedStorageContext());
        SP.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SP.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SP.registerOnSharedPreferenceChangeListener(this);
        final File dataDir = new File(Environment.getDataDirectory(), "user_de/0/" + getApplicationInfo().packageName);
        final File prefsDir = new File(dataDir, "shared_prefs");
        final File prefsFile = new File(prefsDir,  getApplicationInfo().packageName + "_preferences.xml");

        if (prefsFile.exists()) {
            dataDir.setReadable(true, false);
            dataDir.setExecutable(true, false);

            prefsDir.setReadable(true, false);
            prefsDir.setExecutable(true, false);

            prefsFile.setReadable(true, false);
            prefsFile.setExecutable(true, false);
        }
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setStorageDeviceProtected();
            addPreferencesFromResource(R.xml.preference);
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        try {
            Intent intent = new Intent();
            intent.setAction(Common.ACTION_SETTINGS_CHANGED);

            switch (key) {
                case Common.KEY_VOLUME_EXPAND_STREAMS: {
                    HashSet<String> value = (HashSet<String>) SP.getStringSet(key,new HashSet<>(Arrays.asList(Common.DEFAULT_VOLUME_EXPAND_STREAMS)));
                    intent.putExtra(key,value);
                    break;
                }
                case Common.KEY_VOLUME_PANEL_EXPANDED:{
                    Boolean value = SP.getBoolean(Common.KEY_VOLUME_PANEL_EXPANDED,Common.DEFAULT_VOLUME_PANEL_EXPANDED);
                    intent.putExtra(key,value);
                    break;
                }
                case Common.KEY_HOTSPOT_HIDDEN_SSID:{
                    Boolean value = SP.getBoolean(Common.KEY_HOTSPOT_HIDDEN_SSID,Common.DEFAULT_HOTSPOT_HIDDEN_SSID);
                    intent.putExtra(key,value);
                    break;
                }
                case Common.KEY_HOTSPOT_2G_CHANNEL:{
                    int value = Integer.parseInt(SP.getString(Common.KEY_HOTSPOT_2G_CHANNEL,Integer.toString(Common.DEFAULT_HOTSPOT_2G_CHANNEL)));
                    intent.putExtra(key,value);
                    break;
                }
                case Common.KEY_HOTSPOT_5G_CHANNEL:{
                    int value = Integer.parseInt(SP.getString(Common.KEY_HOTSPOT_5G_CHANNEL,Integer.toString(Common.DEFAULT_HOTSPOT_5G_CHANNEL)));
                    intent.putExtra(key,value);
                    break;
                }
            }
            if (intent.getAction() != null) {
                sendBroadcast(intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "onSharedPreferenceChanged failed: ", e);
        }
    }
}
