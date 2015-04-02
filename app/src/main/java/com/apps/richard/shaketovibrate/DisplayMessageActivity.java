/*****************************************************************************

 Richard Huang
 March 27, 2015
 DisplayMessageActivity.java

 Description: This activity displays the message sent by another activity
              through an intent (MainActivity). The message displayed
              is from the edit_message box.
 ****************************************************************************/

package com.apps.richard.shaketovibrate;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/*
 * Name: DisplayMessageActivity (class)
 * Description: Displays the message sent to this activity
 */
public class DisplayMessageActivity extends ActionBarActivity {

    /*
     * Gets the message in the EditText edit_message and displays it in this activity
     *
     * @param savedInstanceState The previous state of the application (can be restored by
     *                           overriding onRestoreInstanceState())
     * @return void
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        String message;     // message of display

        // Gets the intent if bundle is null
        if(savedInstanceState == null) {
            Bundle bundle = getIntent().getExtras();
            if(bundle == null)
                message = null;
            else
                message = bundle.getString(MainActivity.EXTRA_MESSAGE);
        }
        // otherwise use the savedInstanceState
        else {
            message = (String)savedInstanceState.getSerializable(MainActivity.EXTRA_MESSAGE);
        }

        // Display the message on the text view (not editable)
        TextView text = (TextView) findViewById(R.id.text_message);
        text.setText(message, TextView.BufferType.NORMAL);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_display_message, menu);
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
}
