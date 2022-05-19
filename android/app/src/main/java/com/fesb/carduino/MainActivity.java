package com.fesb.carduino;

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

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    String ip;
    int port;
    int dbs; // delay between packets

    TextView IpTextView;
    TextView PortTextView;
    TextView voltage;
    TextView current;
    Button settingsB;
    Button startB;
    SeekBar accelerateS;

    private SensorManager sensorManager = null;

    String msg = "";
    String tmp_msg = "";
    int forward = 0, backward = 0, acceleration = 1024, left = 0, right = 0;
    float accelerometer = 0, accelerometer_temp = 0;

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
            updateMeasurements();

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

                // Test sound
                playSound(sound1);
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
                    udpServerThread.setRunning(false);
                    timerHandler.removeCallbacks(timerRunnable);
                    startB.setText("continue");
                    reset();

                } else if (startB.getText().equals("start")) {
                    // initiate first execution
                    timerHandler.post(timerRunnable);
                    startB.setText("pause");
                    reset();
                    udpServerThread.start();
                } else { // continue
                    timerHandler.post(timerRunnable);
                    startB.setText("pause");
                    reset();
                    udpServerThread.setRunning(false);
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

    public void updateMeasurements() {
        voltage.setText(String.valueOf(udpServerThread.message));
        current.setText(String.valueOf(udpServerThread.message));
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


    // --------------- Handling application state changes ---------------

    @Override
    public void onPause() {
        udpServerThread.setRunning(false);
        timerHandler.removeCallbacks(timerRunnable);
        sensorManager.unregisterListener((SensorEventListener) this);
        startB.setText("continue");
        reset();
        super.onPause();
    }


    @Override
    public void onStop() {

        if (udpServerThread != null) {
            udpServerThread.setRunning(false);
            udpServerThread = null;
        }
        timerHandler.removeCallbacks(timerRunnable);
        sensorManager.unregisterListener((SensorEventListener) this);
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
        if (udpServerThread != null) {
            udpServerThread.setRunning(false);
            udpServerThread = null;
        }
        timerHandler.removeCallbacks(timerRunnable);
        sensorManager.unregisterListener((SensorEventListener) this);
        startB.setText("start");
        reset();
        soundPool.release();
        soundPool = null;
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