package com.apps.richard.shaketovibrate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends ActionBarActivity implements SensorEventListener {

    // Used to obtain the message in edit_message
    public static final String EXTRA_MESSAGE = "com.richard.apps.shaketovibrate.MESSAGE";

    // Device instance variables
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private Vibrator vibrator;

    // Stored data values
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;

    // Constants
    private static final int SHAKE_THRESHOLD = 1000;
    private static final int UPDATE_INTERVAL = 100;
    private static final int VIBRATE_INTERVAL = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initiate system devices and services
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * Detects if the sensor event is from the accelerometer and will vibrate the device when
     * the user shakes it vigorously. Required to implement SensorEventListener
     *
     * @param event The event sent by the device's sensor
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        // Check if reference to correct sensor type (accelerometer)
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Get initial coordinates of accelerometer
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Accelerometer very sensitive, so update on certain interval only
            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > UPDATE_INTERVAL) {
                long diffTime = curTime - lastUpdate;

                // Calculate if should perform shake action
                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                // Vibrate if reached threshold
                if(speed > SHAKE_THRESHOLD) {
                    System.out.println("ENTER 4");
                    vibrator.vibrate(VIBRATE_INTERVAL);
                }

                // update variables
                last_x = x;
                last_y = y;
                last_z = z;
                lastUpdate = curTime;
            }


        }
    }

    /*
     * Required to implement SensorEventListener
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    /*
     * Handles what occurs when paused
     */
    @Override
    protected void onPause() {
        super.onPause();    // always call superclass method first

        // Release activities in use
        senSensorManager.unregisterListener(this);
    }

    /*
     * Handles what occurs when resumed
     */
    @Override
    protected void onResume() {
        super.onResume();   // always call superclass method first

        // Resume activities to be used
        senSensorManager.registerListener(this, senAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    /*
     * Handles what occurs when stopped
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

    /* Called when the user clicks the Send button */
    private void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
}
