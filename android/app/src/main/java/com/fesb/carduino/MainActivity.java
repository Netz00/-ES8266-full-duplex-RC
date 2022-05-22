package com.fesb.carduino;

import static java.lang.Math.exp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
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

public class MainActivity extends AppCompatActivity {
    String ip;
    int port;
    int dbs; // delay between packets

    TextView IpTextView;
    TextView PortTextView;
    TextView voltage;
    TextView current;
    TextView rssi;
    Button settingsB;
    Button startB;
    SeekBar accelerateS;


    String msg = "";
    String tmp_msg = "";
    int forward = 0, backward = 0, acceleration = 1024, left = 0, right = 0;


    private SensorManager mSensorManager;
    private Sensor mRotationVectorSensor;
    private int mSensorInUse;
    private SensorEventListener mRotationListener;
    float accelerometer = 0, accelerometer_temp = 0;
    float sensorInitValue;
    boolean init = true;


    MessageSender messageSender;
    UdpServerThread udpServerThread;


    // Sound variables
    private SoundPool soundPool;
    private int sound1;
    private int sound3StreamId;

    boolean soundsLoaded = false;
    float actVolume, maxVolume, volume;
    AudioManager audioManager;
    int counter;

    Integer measureDelay = 600;
    float distance = 50;
    String previousMeasurement = "";


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


            float acc_val;
            acc_val = accelerometer;
            acc_val -= sensorInitValue; // Starting position correction
            acc_val = ((int) (acc_val * 100)) / 100f;

            // value 0.02 - 0.20

            if (acc_val > 0.02 || acc_val < -0.02) { // is change significant?
                if (acc_val > 0) {
                    acc_val = (float) (exp(acc_val * 20) * 20);

                    if (acc_val >= 1024) // did overflow happened?
                        acc_val = 1024;

                    left = 0;
                    right = (int) (acc_val);
                } else if (acc_val < 0) {
                    acc_val = (float) (exp(-acc_val * 20) * 20);

                    if (acc_val >= 1024) // did overflow happened?
                        acc_val = 1024;

                    right = 0;
                    left = (int) (acc_val);
                }
            } else
                left = right = 0;

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
        voltage = (TextView) findViewById(R.id.voltage);
        current = (TextView) findViewById(R.id.current);
        rssi = (TextView) findViewById(R.id.rssi);
        settingsB = (Button) findViewById(R.id.settings);
        startB = (Button) findViewById(R.id.start);
        accelerateS = (SeekBar) findViewById(R.id.seekBar);

        display();
        reset();

        // Start listening for UDP port
        udpServerThread = new UdpServerThread(4211);


        // AudioManager audio settings for adjusting the volume
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volume = actVolume / maxVolume;

        //Hardware buttons setting to adjust the media sound
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Sound pool init

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(6)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);
        }

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundsLoaded = true;
            }
        });

        sound1 = soundPool.load(this, R.raw.sound1, 1);

        // Settings button listener init

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
                    if (udpServerThread != null) {
                        udpServerThread.setRunning(false);
                    }
                    timerHandler.removeCallbacks(timerRunnable);
                    timerHandler.removeCallbacks(timerRunnableMeasurement);
                    startB.setText("continue");
                    reset();

                } else if (startB.getText().equals("start")) {
                    // initiate first execution
                    timerHandler.post(timerRunnable);
                    timerHandler.post(timerRunnableMeasurement);
                    startB.setText("pause");
                    reset();
                    if (udpServerThread != null) {
                        udpServerThread.start();
                    }
                } else { // continue
                    timerHandler.post(timerRunnable);
                    timerHandler.post(timerRunnableMeasurement);
                    startB.setText("pause");
                    reset();
                    if (udpServerThread != null) {
                        udpServerThread.setRunning(false);
                    }
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


        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // sensorManager.registerListener((SensorEventListener) this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);

    }

    public void updateMeasurements() {

        if (udpServerThread != null) {
            String measurement = udpServerThread.message;

            if (measurement != null
                    && !previousMeasurement.equals(measurement)
                    && measurement.matches("[0-9]*\\.[0-9]+&([+-]?(?=\\.\\d|\\d)(?:\\d+)?(?:\\.?\\d*))(?:[eE]([+-]?\\d+))?&[0-9]*\\.[0-9]+&([+-]?(?=\\.\\d|\\d)(?:\\d+)?(?:\\.?\\d*))(?:[eE]([+-]?\\d+))?&([+-]?(?=\\.\\d|\\d)(?:\\d+)?(?:\\.?\\d*))(?:[eE]([+-]?\\d+))?")
            ) {
                previousMeasurement = measurement;

                String[] parsed = measurement.split("[&]");
                voltage.setText("Cell 1: " + parsed[0] + " V  Cell 2 : " + parsed[1] + " V");
                current.setText("Load: " + parsed[2] + "A");
                rssi.setText("RSSI: " + parsed[4] + "dBm");

                distance = Float.parseFloat(parsed[3]);
            }

            // 110 entire beep
            if (distance < 50) {
                float delay = (float) (exp(distance / 8) + 150);
                measureDelay = Math.round(delay);
                playSound(sound1);
            } else {
                measureDelay = 600;
            }
        }
    }

    Runnable timerRunnableMeasurement = new Runnable() {

        @Override
        public void run() {
            updateMeasurements();
            timerHandler.postDelayed(this, measureDelay); // execute yourself after dbs time
        }
    };


    public String[] parseString(String str) {
        return str.split("[&]");
    }

    /**
     * Sound player
     *
     * @param soundID to be played
     */

    public void playSound(int soundID) {

        // AudioManager audio settings for adjusting the volume
        if (actVolume != (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)) {
            audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            volume = actVolume / maxVolume;
        }

        // Is the sound loaded does it already play?
        if (soundsLoaded) {
            soundPool.play(soundID, volume, volume, 1, 0, 1f);
            counter = counter++;
        }
/* TODO
        soundPool.play(sound1, 1, 1, 0, 0, 1);
        //soundPool.pause(sound3StreamId);
        soundPool.autoPause();
        sound3StreamId = soundPool.play(sound1, 1, 1, 0, 0, 1);
*/

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
    protected void onResume() {
        if (udpServerThread != null) {
            // Start listening for UDP port
            udpServerThread = new UdpServerThread(4211);
            udpServerThread.setRunning(false);
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR) != null) {
            mSensorInUse = Sensor.TYPE_GAME_ROTATION_VECTOR;
        } else {
            mSensorInUse = Sensor.TYPE_ROTATION_VECTOR;
        }

        mRotationVectorSensor = mSensorManager.getDefaultSensor(mSensorInUse);
        mRotationListener = new RotationListener();
        mSensorManager.registerListener(mRotationListener, mRotationVectorSensor, SensorManager.SENSOR_DELAY_GAME);

        super.onResume();
    }

    @Override
    public void onPause() {
        if (udpServerThread != null) {
            udpServerThread.setRunning(false);
        }
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.removeCallbacks(timerRunnableMeasurement);
        startB.setText("continue");
        reset();

        mSensorManager.unregisterListener(mRotationListener);
        super.onPause();
    }


    @Override
    public void onStop() {

        if (udpServerThread != null) {
            udpServerThread.setRunning(false);
            udpServerThread = null;
        }
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.removeCallbacks(timerRunnableMeasurement);
        mSensorManager.unregisterListener(mRotationListener);
        startB.setText("continue");
        reset();
        super.onStop();
    }

    @Override
    public void onRestart() {
        if (udpServerThread != null) {
            udpServerThread.setRunning(false);
            udpServerThread = null;
        }
        // delete all timerRunnable tasks that are waiting for execution
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.removeCallbacks(timerRunnableMeasurement);
        startB.setText("continue");

        // TODO is it necessary?

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR) != null) {
            mSensorInUse = Sensor.TYPE_GAME_ROTATION_VECTOR;
        } else {
            mSensorInUse = Sensor.TYPE_ROTATION_VECTOR;
        }

        mRotationVectorSensor = mSensorManager.getDefaultSensor(mSensorInUse);
        mRotationListener = new RotationListener();
        mSensorManager.registerListener(mRotationListener, mRotationVectorSensor, SensorManager.SENSOR_DELAY_GAME);

        reset();
        display();
        super.onRestart();
    }

    @Override
    public void onDestroy() {
        if (udpServerThread != null) {
            udpServerThread.setRunning(false);
            udpServerThread = null;
        }
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.removeCallbacks(timerRunnableMeasurement);
        mSensorManager.unregisterListener(mRotationListener);
        startB.setText("start");
        reset();
        soundPool.release();
        soundPool = null;
        super.onDestroy();
    }

    // ------------------------------------------------------------------


    // --------------- Handling accelerometer sensor state changes ---------------


    private class RotationListener implements SensorEventListener {

        public void onSensorChanged(SensorEvent event) {
            // we received a sensor event. it is a good practice to check
            // that we received the proper event

            if (event.sensor.getType() == mSensorInUse) {


                accelerometer = event.values[2];

                if (init) {

                    sensorInitValue = accelerometer;
                    init = false;

                } else if ((accelerometer - accelerometer_temp) > 0.01 || (accelerometer - accelerometer_temp) < (-0.01)) {

                    accelerometer_temp = accelerometer;

                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }


    // ------------------------------------------------------------------

}