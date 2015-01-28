/**
 * LaTotugaActivity.java
 * Created on: Jan 21, 2015
 * This piece of work was
 * made for the exclusive use of <em>Software Colombia</em>.
 * All rights reserved to them ©2014.
 */
package org.tjdo.latotuga;

import android.app.SearchManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.tjdo.latotuga.org.tjdo.latotuga.util.Constants;
import org.tjdo.services.LaTotugaFactory;
import org.tjdo.services.dto.AuthenticateResponse;
import org.tjdo.services.dto.Symphony;
import org.tjdo.util.LaTotugaException;

import java.util.ArrayList;
import java.util.List;

/**
 * Main entrance for the application.
 * @author efmcuiti (efmcuiti@gmail.com)
 */
public class LaTotugaActivity extends ActionBarActivity {

    /** Used to log messages to the console. */
    private static final String TAG = "LaTotuga";

    /** Where to look for strings and all that stuff. */
    private Resources res;

    /** Set of actual symphonies to be used. */
    private List<Symphony> symphonies;

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
        AsyncTask<Void, Void, List<String>>
                symphoniesTask = new AsyncTask<Void, Void, List<String>>() {
            /**
             * @see {@link AsyncTask#doInBackground(Object[])}
             */
            @Override
            protected List<String> doInBackground(Void... params) {
                List<String> answer = new ArrayList<>();

                try {
                    AuthenticateResponse response = LaTotugaFactory.getServices(
                            LaTotugaActivity.this).authenticate(
                            Constants.LATOTUGA_USERNAME, Constants.LATOTUGA_PASSWORD);

                    if (response.isSuccess()) {
                        // 1. The whole object may be needed forward in time.
                        symphonies = response.getSinfonias();

                        // 2. Building the actual response.
                        for (Symphony s : symphonies) {
                            answer.add(s.getNombre());
                        }
                    }
                } catch (LaTotugaException e) {
                    Log.e(TAG, String.format("Couldn't authenticate with username: %s",
                            Constants.LATOTUGA_USERNAME), e);
                }

                return answer;
            }

            /**
             * @see {@link AsyncTask#doInBackground(Object[])}
             */
            @Override
            protected void onPostExecute(List<String> symphonies) {
                Spinner spinner = (Spinner) findViewById(R.id.symphonySpinner);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        LaTotugaActivity.this, android.R.layout.simple_spinner_item, symphonies);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
            }
        };

        symphoniesTask.execute();
    }

    /* (non-Javadoc)
	 * @see android.support.v7.app.ActionBarActivity#onCreateOptionsMenu(android.view.Menu)
	 */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_la_totuga, menu);

        // Configuring the search widget.
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.name_search);
        SearchView searchView = (SearchView)searchItem.getActionView();

        // The following works if and only if this activity is the search activity.
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

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
