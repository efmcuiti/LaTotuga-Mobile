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
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.tjdo.latotuga.org.tjdo.latotuga.util.Constants;
import org.tjdo.latotuga.org.tjdo.latotuga.util.DownloadTask;
import org.tjdo.latotuga.org.tjdo.latotuga.util.NameItem;
import org.tjdo.latotuga.org.tjdo.latotuga.util.Util;
import org.tjdo.services.dto.Name;
import org.tjdo.services.dto.Symphony;
import org.tjdo.util.LaTotugaException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

    /** When a search is issued, we must inform the selected symphony. */
    private static final String EXTRA_SYMPHONY = "symphony";

    /** Where to look for strings and all that stuff. */
    private Resources res;

    /** Set of actual symphonies to be used. */
    private List<Symphony> symphonies;

    /** Used to store all the children names per symphony */
    private Map<Symphony, List<Name>> childrenNames;

    /** Basic adapter to use with the names list view. */
    private ChildrenListAdapter listAdapter;

    /** This one will be used to know when to query the info from the servers. */
    private boolean queryServer;

    /** Used to filter results. */
    private String search;

    /** Used when the intent is a search. */
    private int selectedSymphony;

    /** Linked at runtime with the reel on playing. */
    private SeekBar playerSeek;

    /** Used to play/pause a reel. */
    private ImageButton playButton;

    /** Used to stop/reset a reel. */
    private ImageButton stopButton;

    /** Android component to play files. */
    private MediaPlayer player;

    /** Where to paint the actual child whose reel is being played. */
    private TextView childNameLabel;

    /** WHere to paint the actual symphony to which the child reel is playing. */
    private TextView symphonyNameLabel;

    /** Handles the update of the reel player. */
    private Handler seekHandler;

    /** Updates the seeker. */
    private Runnable updater;

    /* (non-Javadoc)
	 * @see android.support.v7.app.ActionBarActivity#onCreate(android.os.Bundle)
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_la_totuga);
        res = getResources();
        childrenNames = new HashMap<>();

        // If no external storage is present, this application shall fail!.
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getApplicationContext(),
                    res.getString(R.string.no_external_storage),
                    Toast.LENGTH_LONG).show();
            finish();
        }

        // Init player component.
        seekHandler = new Handler();
        playerSeek = (SeekBar) findViewById(R.id.playerSeek);
        childNameLabel = (TextView) findViewById(R.id.playerNameLabel);
        symphonyNameLabel = (TextView) findViewById(R.id.playerSymphonyLabel);
        playButton = (ImageButton) findViewById(R.id.playButton);
        stopButton = (ImageButton) findViewById(R.id.stopButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            /** {@inheritDoc} */
            @Override
            public void onClick(View v) {
                onPlayAction(v);
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            /** {@inheritDoc} */
            @Override
            public void onClick(View v) {
                onStopAction(v);
            }
        });
        updater = new Runnable() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                updatePlayer();
            }
        };
        playerSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /** {@inheritDoc} */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (player != null) {
                    player.seekTo(progress);
                }
            }

            /** {@inheritDoc} */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            /** {@inheritDoc} */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Validating if the intent correspond to a search.
        Intent i = getIntent();
        if (Intent.ACTION_SEARCH.equals(i.getAction())) {
            search = i.getStringExtra(SearchManager.QUERY);
            Log.i(TAG, String.format("Looking for: %s", search));
            selectedSymphony = i.getIntExtra(EXTRA_SYMPHONY, 0);
            Log.i(TAG, String.format("Found selected symphony: %s", selectedSymphony));
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
            protected void onPostExecute(final List<String> symphonies) {
                super.onPostExecute(symphonies);
                final Spinner spinner = (Spinner) findViewById(R.id.symphonySpinner);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        LaTotugaActivity.this, android.R.layout.simple_spinner_item, symphonies);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                // Setting the basic context for the list view.
                final ListView namesList = (ListView) findViewById(R.id.namesList);
                namesList.setFastScrollEnabled(true);
                List<String> children = loadInitialNames();
                Collections.sort(children);
                listAdapter = new ChildrenListAdapter(getApplicationContext(), children);
                spinner.setSelection(selectedSymphony);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedSymphony = spinner.getSelectedItemPosition();
                        List<String> _children = loadInitialNames();
                        Collections.sort(_children);
                        ChildrenListAdapter adapter = new ChildrenListAdapter(
                                getApplicationContext(), _children);
                        namesList.setAdapter(adapter);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                namesList.setAdapter(listAdapter);
                namesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // Getting the selected symphony and name to download.
                        Symphony s = LaTotugaActivity.this.symphonies.get(selectedSymphony);
                        // The name shall be located through its view because search filters.
                        LinearLayout layout = (LinearLayout) view;
                        TextView name = (TextView) layout.findViewById(R.id.name_item);
                        String raw = name.getText().toString();
                        Name _n = null;
                        List<Name> names = childrenNames.get(s);
                        for (Name n : names) {
                            if(raw.equalsIgnoreCase(n.getNombre())) {
                                _n = n;
                                break;
                            }
                        }
                        Log.i(TAG, String.format("Selected name %s", _n.getNombre()));
                        downloadName(s, _n);

                    }
                });
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
     * Given a symphony and a name, this method downloads the reel using a background task.
     * @param s Owner of the reel.
     * @param n Name to download.
     */
    private void downloadName(final Symphony s, final Name n) {
        AsyncTask<Void, Integer, File>
                reelTask = new DownloadTask(LaTotugaActivity.this, s, n);

        reelTask.execute();
    }

    /**
     * Call back used by the download task once it's finished.
     * @param mp3 Reel to play.
     * @param s Symphony parent for the reel.
     * @param n Name of the child to be showed.
     */
    public void onReelDownloaded(File mp3, Symphony s, Name n) {
        Log.i(TAG, String.format("Reel ready! %s", mp3.getName()));

        // 1. Set the child name and the symphony.
        childNameLabel.setText(n.getNombre());
        symphonyNameLabel.setText(s.getNombre());

        // 2. Prepare the media player.
        Uri uri = Uri.fromFile(mp3);
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            player.setDataSource(getApplicationContext(), uri);
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    playerSeek.setMax(player.getDuration());
                    playerSeek.setProgress(0);

                    // 3. Start playing the reel.
                    onPlayAction(findViewById(R.id.playButton));
                    new Thread(updater).start();
                }
            });
            player.prepareAsync();
        } catch (IOException e) {
            Log.e(TAG, String.format("Couldn't load the mp3 %s", mp3.getName()), e);
            return;
        }
    }

    /**
     * Handles the play event (or pause if already playing).
     * @param view The play button.
     */
    private void onPlayAction(View view) {
        // 1. If no playing.. everything starts.
        if (!player.isPlaying()) {
            player.start();
        } else {
            player.pause();
        }
    }

    /**
     * Handles the stop event.
     * @param view The stop button.
     */
    private void onStopAction(View view) {
        if (player.isPlaying()) {
            player.pause();
            player.seekTo(0);
        }
    }

    /**
     * Assuming an actual reel playing, it modifies the seek bar
     * state.
     */
    private void updatePlayer() {
        playerSeek.setProgress(player.getCurrentPosition());
        seekHandler.postDelayed(updater, Constants.SEEKER_DELAY);
    }

    /**
     * Updates the list view adapter with the names for the first found symphony.
     */
    private List<String> loadInitialNames() {
        List<String> result = new ArrayList<>();
        // 1. Get the first symphony.
        Symphony s = symphonies.get(selectedSymphony);

        // 2. Find the list of names.
        List<Name> children = childrenNames.get(s);

        // 3. If the user is searching for a name, this will be shortened.
        boolean filter = (search != null) && !("".equalsIgnoreCase(search.trim()));

        // 3. Updates the list view with all the names.
        NameItem name = null;
        for(Name n : children) {
            name = new NameItem();
            if (filter) {
                search = search.toUpperCase();
                if (n.getNombre().startsWith(search)) {
                    name.setName(n.getNombre());
                    result.add(name.getName());
                }
            } else {
                name.setName(n.getNombre());
                result.add(name.getName());
            }
        }

        return result;
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
        searchView.setQueryRefinementEnabled(true);

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

    /**
     * Same as {@link #startActivity(android.content.Intent, android.os.Bundle)} with no options
     * specified.
     *
     * @param intent The intent to start.
     * @throws android.content.ActivityNotFoundException
     * @see {@link #startActivity(android.content.Intent, android.os.Bundle)}
     * @see #startActivityForResult
     */
    @Override
    public void startActivity(Intent intent) {
        // Adding the selected symphony.
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            Spinner symphonies = (Spinner) findViewById(R.id.symphonySpinner);
            int s = symphonies.getSelectedItemPosition();
            intent.putExtra(EXTRA_SYMPHONY, s);
        }

        super.startActivity(intent);
    }
}
