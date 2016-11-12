package com.drobisch.partkeeprscannrapp;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

public class SettingsActivity extends AppCompatActivity {

    private AutoCompleteTextView mServerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mServerView = (AutoCompleteTextView) findViewById(R.id.server);

        Button mSaveSettingsButton = (Button) findViewById(R.id.save_settings_button);
        mSaveSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings();
            }
        });

        SharedPreferences prefs = getSharedPreferences(getString(R.string.pref_name), MODE_PRIVATE);
        String server = prefs.getString(getString(R.string.pref_key_server), getString(R.string.pref_default_server));
        mServerView.setText(server);
    }

    public void saveSettings() {
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.pref_name), MODE_PRIVATE).edit();
        editor.putString(getString(R.string.pref_key_server), mServerView.getText().toString());
        editor.commit();
    }

    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;

    }
}
