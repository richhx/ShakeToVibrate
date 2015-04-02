/*****************************************************************************

 Richard Huang
 March 27, 2015
 MainActivity.java

 Description: This activity hosts the bulk of the application and is the
              initial starting point. The vibration when shaken is handled
              by this activity and the settings can be opened from the
              action bar.

 ****************************************************************************/

package com.apps.richard.shaketovibrate;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * Name: MainActivity (class)
 * Description: Home/main activity that will be used by the user.
 */
public class MainActivity extends ActionBarActivity implements SensorEventListener {

    // Key values
    public static final String EXTRA_MESSAGE = "com.richard.apps.shaketovibrate.MESSAGE";
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    // Instance variables
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private Vibrator vibrator;
    private EditText edit_message;

    // Stores previous values to calculate delta in accelerometer
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;

    // Constants: can be adjusted to user preference
    private static final int SHAKE_THRESHOLD = 1000;
    private static final int UPDATE_INTERVAL = 100;
    private static final int VIBRATE_INTERVAL = 400;
    private static final String FILE_MESSAGES = "messages";

    private String mCurrentPhotoPath;

    /*
     * This method will instantiate the savedInstance of the application if
     * it exists. Afterwards, it will initialize and instantiate the instance
     * variables handling the system services and other variables. Furthermore,
     * it will read in string data if it exists and set it to the edit_message
     * text.
     *
     * @param  savedInstanceState  Previous state of the application storing
     *                             key values and such.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initiate system devices and services
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        edit_message = (EditText) findViewById(R.id.edit_message);

        // Read string data and set edit_message text
        String message = readData(FILE_MESSAGES);
        edit_message.setText(message);
    }

    /*
     * Creates the action bar and adds the items to it.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /*
     * Handles what happens on selection of an item in the menu of
     * the action bar. The action bar will automatically handle clicks
     * on the Home/Up button as long as a parent activity is specified
     * in the AndroidManifest.xml
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();  // which item selected

        // Settings button
        if (id == R.id.action_settings) {
            openSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * Releases the use of the accelerometer sensor
     */
    @Override
    protected void onPause() {
        super.onPause();    // always call superclass method first

        // Release activities in use
        senSensorManager.unregisterListener(this);
    }

    /*
     * Registers the use of the accelerometer sensor.
     */
    @Override
    protected void onResume() {
        super.onResume();   // always call superclass method first

        // Resume activities to be used
        senSensorManager.registerListener(this, senAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    /*
     * Handles application what occurs when application no longer visible.
     * Saves the string in edit_message to file.
     */
    @Override
    protected void onStop() {
        super.onStop();
        writeData(edit_message.getText().toString(), FILE_MESSAGES);
    }

    /*
     * Creates a new activity by instantiating an intent. This activity
     * will display the string in the editText in a new window. The intent
     * holds the string message.
     *
     * @param   view   The location/object from which the function is called
     */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        String message = edit_message.getText().toString();     // retrieve message
        intent.putExtra(EXTRA_MESSAGE, message);                 // store message in intent
        startActivity(intent);
    }

    /*
     * Creates an intent that will take a picture through the camera if it can
     */
    public void dispatchTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Check if the intent can be handled by a camera activity
        if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;  // where the photo should go
            // Create the file
            try {
                photoFile = createImageFile();
            }
            // Error occurred in creation of file
            catch (IOException error) {
                error.printStackTrace();
            }

            // As long as file exists
            if(photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                setResult(RESULT_OK, takePictureIntent);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    public File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir);    // directory

        // save file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

          /*  Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView photo_image = (ImageView) findViewById(R.id.photo_image);
            photo_image.setImageBitmap(imageBitmap); */

            if (data == null) {
                // A known bug here!!! The image should have saved in fileUri
                Toast.makeText(this, "Image saved successfully", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(this, "Image saved successfully in: " + data.getData(),
                        Toast.LENGTH_LONG).show();
            }
        }
        else if(resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Callout for image capture failed!", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Creates a new activity that will display the settings for this
     * application.
     */
    public void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /*
     * Detects if the sensor event is from the accelerometer and will vibrate the device when
     * the user shakes it vigorously. Required to implement SensorEventListener
     *
     * @param  event  The event sent by the device's sensor
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor; // the sensor that calls this method

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
     * Writes string data to a file.
     *
     * @param  data      The string to write to file
     * @param  fileName  File to write data to
     */
    public void writeData(String data, String fileName) {
        // Create output stream and write data to fileName
        try {
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(data);
            osw.flush();
            osw.close();
        }
        // Print stack trace of the error to debug
        catch(IOException error) {
            error.printStackTrace();
        }
    }

    /*
     * Reads string data from a file.
     *
     * @param  fileName  File to read data from
     */
    public String readData(String fileName) {
        StringBuffer data = new StringBuffer("");       // used to store read data
        // Create input stream to read data from fileName
        try {
            FileInputStream fis = openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader buffReader = new BufferedReader(isr);

            // read until nothing to read
            String readString = buffReader.readLine();
            while(readString != null) {
                data.append(readString);
                readString = buffReader.readLine();
            }
            isr.close();
        }
        // Print stack trace of error to debug
        catch(IOException error) {
            error.printStackTrace();
        }

        return data.toString();
    }

    /*
     * Required to implement SensorEventListener. Unused.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

}
