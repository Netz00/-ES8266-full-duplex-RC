package com.fesb.carduino;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    String ip;
    int port;
    int dbs; // delay between packets

    TextView IpTextView;
    TextView PortTextView;
    Button settingsB;
    Button startB;
    SeekBar accelerateS;

    private SensorManager sensorManager = null;

    String msg = "";
    String tmp_msg = "";
    int forward = 0, backward = 0, acceleration = 1024, left = 0, right = 0;
    float accelerometer = 0, accelerometer_temp = 0;

    MessageSender messageSender;

    /**
     * Simulating recursive delayed function
     */
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {

            if (acceleration > 1024) {
                backward = 0;
                forward = (acceleration - 1024);
            } else if (acceleration < 1024) {
                forward = 0;
                backward = -(acceleration - 1024);

            } else if (acceleration == 1024) {
                forward = backward = 0;
            }

            if (accelerometer > 0) {
                right = 0;
                left = Integer.valueOf((int) (accelerometer * 110));
            } else if (accelerometer < 0) {
                left = 0;
                right = Integer.valueOf((int) (-accelerometer * 110));
            } else if (accelerometer == 0) {
                left = right = 0;
            }

            sendMsg();

            timerHandler.postDelayed(this, dbs); // execute yourself after dbs time

        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load settings
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        ip = settings.getString("ip", "192.168.0.17");
        port = settings.getInt("port", 4210);
        dbs = settings.getInt("fps", 200);

        IpTextView = (TextView) findViewById(R.id.ip);
        PortTextView = (TextView) findViewById(R.id.port);
        settingsB = (Button) findViewById(R.id.settings);
        startB = (Button) findViewById(R.id.start);
        accelerateS = (SeekBar) findViewById(R.id.seekBar);

        display();
        reset();

        settingsB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings();
            }
        });

        startB.setText("start");//start-pause-continue
        startB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (startB.getText().equals("pause")) {
                    // delete all timerRunnable tasks that are waiting for execution
                    timerHandler.removeCallbacks(timerRunnable);
                    startB.setText("continue");
                    reset();
                } else {
                    // initiate first execution
                    timerHandler.post(timerRunnable);
                    startB.setText("pause");
                    reset();
                }

            }
        });

        accelerateS.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                reset();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                acceleration = progress;

            }
        });

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener((SensorEventListener) this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);

    }

    /**
     * Reset all controls to 0 values
     */
    public void reset() {
        accelerateS = findViewById(R.id.seekBar);
        accelerateS.setProgress(1024);
        accelerateS.refreshDrawableState();
        forward = 0;
        backward = 0;
        left = 0;
        right = 0;
        accelerometer = 0;
        accelerometer_temp = 0;
        acceleration = 1024;
        sendMsg();
    }

    /**
     * Send message
     */
    private void sendMsg() {

        // building message from parameters
        msg = (String.valueOf(forward) + "&" + String.valueOf(backward) + ":" + String.valueOf(left) + "&" + String.valueOf(right) + "e");

        // checking if any new messages and sending if any
        if (!tmp_msg.equals(msg)) {
            new MessageSender().execute(msg);
            Log.i("PACKET: ", msg);
            tmp_msg = msg;
        }
    }

    /**
     * Open setting view
     */
    public void settings() {
        reset();
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    /**
     * Display IP and port
     * Set message sender IP and port
     */
    public void display() {
        IpTextView.setText(ip);
        PortTextView.setText(String.valueOf(port));
        try {
            MessageSender.ip = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        MessageSender.port = port;
    }


    // --------------- Handling application state changes ---------------

    @Override
    public void onPause() {
        timerHandler.removeCallbacks(timerRunnable);
        sensorManager.unregisterListener((SensorEventListener) this);
        startB.setText("continue");
        reset();
        super.onPause();
    }


    @Override
    public void onStop() {
        timerHandler.removeCallbacks(timerRunnable);
        sensorManager.unregisterListener((SensorEventListener) this);
        startB.setText("continue");
        reset();
        super.onStop();
    }

    @Override
    public void onRestart() {
        // delete all timerRunnable tasks that are waiting for execution
        timerHandler.removeCallbacks(timerRunnable);
        startB.setText("continue");
        sensorManager.registerListener(
                (SensorEventListener) this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME
        );
        reset();
        display();
        super.onRestart();
    }

    @Override
    public void onDestroy() {
        timerHandler.removeCallbacks(timerRunnable);
        sensorManager.unregisterListener((SensorEventListener) this);
        startB.setText("start");
        reset();
        super.onDestroy();
    }

    // ------------------------------------------------------------------


    // --------------- Handling accelerometer sensor state changes ---------------

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            accelerometer_temp = event.values[1];

            if ((accelerometer_temp - accelerometer) > 0.5 || (accelerometer_temp - accelerometer) < (-0.5))
                accelerometer = accelerometer_temp;

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // ------------------------------------------------------------------

}