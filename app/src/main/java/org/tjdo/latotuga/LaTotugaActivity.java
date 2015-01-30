/**
 * LaTotugaActivity.java
 * Created on: Jan 21, 2015
 * This piece of work was
 * made for the exclusive use of <em>Software Colombia</em>.
 * All rights reserved to them ©2014.
 */
package org.tjdo.latotuga;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.tjdo.latotuga.org.tjdo.latotuga.util.Constants;
import org.tjdo.latotuga.org.tjdo.latotuga.util.NameItem;
import org.tjdo.latotuga.org.tjdo.latotuga.util.Util;
import org.tjdo.services.dto.Name;
import org.tjdo.services.dto.Symphony;
import org.tjdo.util.LaTotugaException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /** Used to store all the children names per symphony */
    private Map<Symphony, List<Name>> childrenNames;

    /** Basic adapter to use with the names list view. */
    private NameListAdapter listAdapter;

    /** This one will be used to know when to query the info from the servers. */
    private boolean queryServer;

    /* (non-Javadoc)
	 * @see android.support.v7.app.ActionBarActivity#onCreate(android.os.Bundle)
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_la_totuga);
        res = getResources();
        childrenNames = new HashMap<>();
        listAdapter = new NameListAdapter(getApplicationContext());

        // If no external storage is present, this application shall fail!.
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getApplicationContext(),
                    res.getString(R.string.no_external_storage),
                    Toast.LENGTH_LONG).show();
            finish();
        }

        // Setting flag for querying server.
        verifyLocalData();

        // Setting up the "action bar"
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolBar);
        toolbar.setTitle(res.getString(R.string.app_name));
        toolbar.setLogo(R.drawable.tortuga);
        setSupportActionBar(toolbar);

        // Setting the spinner content.
        AsyncTask<Void, String, List<String>>
                symphoniesTask = new AsyncTask<Void, String, List<String>>() {
            /** Generic waiting process dialog for long batch operations. */
            private ProgressDialog progress;

            /** Generic message to use when loading names.*/
            private String loadingMessage;

            /**
             * @see {@link AsyncTask#onPreExecute()}
             */
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progress = new ProgressDialog(LaTotugaActivity.this);
                String title =
                        progress.getContext().getResources().getString(R.string.app_name);
                String msg =
                        progress.getContext().getResources().getString(R.string.loading_data);
                progress.setTitle(title);
                progress.setMessage(msg);
                progress.setIndeterminate(true);

                loadingMessage = progress.getContext().getResources().getString(
                        R.string.querying_symphony_names);
                progress.show();
            }

            /**
             * @see {@link AsyncTask#doInBackground(Object[])}
             */
            @Override
            protected List<String> doInBackground(Void... params) {
                List<String> answer = new ArrayList<>();

                try {
                    symphonies = Util.getSymphonies(queryServer, LaTotugaActivity.this);
                    // Storing the symphonies if extracted from web.
                    if (queryServer) {
                        Util.saveSymphonies(true, symphonies, LaTotugaActivity.this);
                    }

                    List<Name> names = null;
                    for (Symphony s : symphonies) {
                        answer.add(s.getNombre());
                        // 2.1 For each symphony we get the children names.
                        publishProgress(loadingMessage + " " + s.getNombre());
                        Log.i(TAG, String.format("Querying names for %s", s.getNombre()));
                        names = Util.getNames(queryServer, LaTotugaActivity.this, s);
                        childrenNames.put(s, names);

                        //  Storing the names if extracted from web.
                        if (queryServer) {
                            Util.saveNames(true, s, names, LaTotugaActivity.this);
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
                super.onPostExecute(symphonies);
                Spinner spinner = (Spinner) findViewById(R.id.symphonySpinner);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        LaTotugaActivity.this, android.R.layout.simple_spinner_item, symphonies);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                // Setting the basic context for the list view.
                final ListView namesList = (ListView) findViewById(R.id.namesList);
                loadInitialNames();
                namesList.setAdapter(listAdapter);
                if (progress != null) {
                    progress.dismiss();
                }
            }

            /**
             * @see {@link AsyncTask#onProgressUpdate(Object[])}
             */
            @Override
            protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);
                progress.setMessage(values[0]);
            }
        };

        symphoniesTask.execute();
    }

    /**
     * Updates the list view adapter with the names for the first found symphony.
     */
    private void loadInitialNames() {
        // 1. Get the first symphony.
        Symphony s = symphonies.get(0);

        // 2. Find the list of names.
        List<Name> children = childrenNames.get(s);

        // 3. Updates the list view with all the names.
        NameItem name = null;
        for(Name n : children) {
            name = new NameItem();
            name.setName(n.getNombre());
            listAdapter.add(name);
        }
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

    /**
     * This method verifies the status of already downloaded information.
     * The first time it'll mark the corresponding flag so the content shall
     * be downloaded.
     */
    public void verifyLocalData() {
        File storage = getExternalFilesDir(null);

        // Looking for the most basic file.
        File files[] = storage.listFiles();

        for (File f : files) {
            if ((!f.isDirectory())
                    && (f.getName().equalsIgnoreCase(
                    res.getString(R.string.symphonies_file_name)))) {
                Log.i(TAG, String.format("Found symphonies files %s",
                        res.getString(R.string.symphonies_file_name)));
                queryServer = false;
                return;
            }
        }

        queryServer = true;
    }
}
