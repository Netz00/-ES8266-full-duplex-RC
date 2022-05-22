package com.fesb.carduino;

import static java.lang.Integer.parseInt;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Setting class responsible for handling user settings changes
 */
public class Settings extends AppCompatActivity {


    /**
     * Responsible for displaying settings screen and changing settings
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        final TextView ip = (TextView) findViewById(R.id.editTextIP);
        final TextView port = (TextView) findViewById(R.id.editTextPort);
        final TextView fps = (TextView) findViewById(R.id.editTextFPS);


        // Load stored user configuration from last session
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); // 0 - for private mode
        final SharedPreferences.Editor editor = pref.edit();

        ip.setText(pref.getString("ip", "192.168.0.17"));
        port.setText(String.valueOf(pref.getInt("port", 4210)));
        fps.setText(String.valueOf(pref.getInt("fps", 200)));

        // Add listener on IP address input field
        ip.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() == 0) {
                    return;
                }
                String ulaz1 = ip.getText().toString();
                if (validIP(ulaz1)) {
                    editor.putString("ip", ulaz1);
                    editor.commit();
                    ip.setError(null);
                } else {
                    ip.setError("Invalid IP address");
                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        // Add listener on port input field
        port.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() == 0) {
                    return;
                }
                int ulaz1 = parseInt(port.getText().toString());
                if ((ulaz1 > 0 && ulaz1 <= 65535)) {
                    editor.putInt("port", ulaz1);
                    editor.commit();
                    ip.setError(null);
                } else {
                    port.setError("Invalid port number");
                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        // Add listener on delay between packets input field
        fps.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() == 0) {
                    return;
                }
                int ulaz2 = parseInt(fps.getText().toString());
                if (ulaz2 > 10) {
                    editor.putInt("fps", ulaz2);
                    editor.commit();
                    ip.setError(null);
                } else {
                    fps.setError("Too small delay");
                }


            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

    }


    /**
     * Validate entered IP address
     *
     * @param ip address for validation
     * @return true if IP address valid
     */
    public static boolean validIP(String ip) {
        try {
            if (ip == null || ip.isEmpty()) {
                return false;
            }

            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return false;
            }

            for (String s : parts) {
                int i = parseInt(s);
                if ((i < 0) || (i > 255)) {
                    return false;
                }
            }
            if (ip.endsWith(".")) {
                return false;
            }

            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }


}