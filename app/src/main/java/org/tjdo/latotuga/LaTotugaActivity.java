/**
 * LaTotugaActivity.java
 * Created on: Jan 21, 2015
 * This piece of work was
 * made for the exclusive use of <em>Software Colombia</em>.
 * All rights reserved to them ©2014.
 */
package org.tjdo.latotuga;

import android.content.res.Resources;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Main entrance for the application.
 * @author efmcuiti (efmcuiti@gmail.com)
 */
public class LaTotugaActivity extends ActionBarActivity {

    /** Where to look for strings and all that stuff. */
    private Resources res;

    /* (non-Javadoc)
	 * @see android.support.v7.app.ActionBarActivity#onCreate(android.os.Bundle)
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_la_totuga);
        res = getResources();

        // Setting up the "action bar"
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolBar);
        toolbar.setTitle(res.getString(R.string.app_name));
        toolbar.setLogo(R.drawable.tortuga);
        setSupportActionBar(toolbar);

        // Setting the spinner content.
        Spinner spinner = (Spinner) findViewById(R.id.symphonySpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.shyphonies, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    /* (non-Javadoc)
	 * @see android.support.v7.app.ActionBarActivity#onCreateOptionsMenu(android.view.Menu)
	 */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_la_totuga, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView)searchItem.getActionView();
        return true;
    }

    /* (non-Javadoc)
	 * @see android.support.v7.app.ActionBarActivity#onOptionsItemSelected(android.view.MenuItem)
	 */
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
