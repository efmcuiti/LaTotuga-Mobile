/**
 * DownloadTask.java 
 * Created on: 2/4/15
 * This piece of work was
 * made for the exclusive use of <em>The Just DO!</em>. 
 * All rights reserved ©2015.
 */
package org.tjdo.latotuga.org.tjdo.latotuga.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.tjdo.latotuga.LaTotugaActivity;
import org.tjdo.latotuga.R;
import org.tjdo.services.dto.Name;
import org.tjdo.services.dto.Symphony;
import org.tjdo.util.UnZip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Used to download the reel for a certain kid.
 *
 * @author emcuiti (efmcuiti@gmail.com)
 */
public class DownloadTask extends AsyncTask<Void, Integer, File> {

    /** Used to log messages to the console. */
    private static final String TAG = "LaTotuga-DownloadReel";

    /** Generic waiting process dialog for long batch operations. */
    private ProgressDialog progress;

    /** Parent symphony to download reels. */
    private Symphony symphony;

    /** What reel should be downloaded. */
    private Name name;

    /** Who's calling this. */
    private LaTotugaActivity activity;

    /** Used to configure the stuff. */
    private Context context;

    public DownloadTask(LaTotugaActivity activity, Symphony symphony, Name name) {
        this.activity = activity;
        this.context = activity;
        progress = new ProgressDialog(context);
        this.symphony = symphony;
        this.name = name;
    }

    /**
     * @see {@link AsyncTask#onPreExecute()}
     */
    @Override
    protected void onPreExecute() {
        progress = new ProgressDialog(context);
        String title =
                context.getResources().getString(R.string.app_name);
        String msg =
                context.getResources().getString(R.string.downloading_reel)
                        + " " + name.getNombre();
        progress.setTitle(title);
        progress.setMessage(msg);
        progress.setIndeterminate(false);
        progress.setCancelable(true);
        progress.setMax(100);
        progress.setProgress(0);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.show();
    }

    /** {@inheritDoc} */
    @Override
    protected void onProgressUpdate(Integer... values) {
        progress.setProgress(values[0]);
    }

    /** {@inheritDoc} */
    @Override
    protected File doInBackground(Void... params) {
        File mp3 = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            // 1. Define where will the reel be saved.
            String concrete = symphony.getRuta() + File.separator +
                    context.getResources().getString(R.string.reels_directory_name) + File.separator;
            File targetDir = new File(context.getExternalFilesDir(null), concrete);

            // 2. Define the remote URL where to get the name from.
            String sX = "S" + symphony.getId_sinfonia() + "/";
            String _url = Constants.LATOTUGA_REELS_URL_BASE + sX + name.getRuta();

            URL url = new URL(_url);

            // 3. Define the file to save the reel to.
            File goal = new File(targetDir, name.getRuta());
            mp3 = new File(targetDir, name.getRuta().replaceAll(
                    Constants.ZIP_EXT, Constants.MP3_EXT));

            // 3.1. If the reel have been downloaded already, we need no more.
            if (mp3.exists()) {
                return mp3;
            }

            // 4. Connecting and getting the file size.
            // Preparing also everything to download.
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int size = connection.getContentLength();
            int totalRead = 0;

            bis = new BufferedInputStream(connection.getInputStream());
            FileOutputStream fos = new FileOutputStream(goal);
            bos = new BufferedOutputStream(fos, Constants.BLOCK_SIZE);
            byte data[] = new byte[Constants.BLOCK_SIZE];

            // 5. Downloading the song.
            int i;
            float percent;
            while (((i = bis.read(data, 0, Constants.BLOCK_SIZE)) >= 0)
                    && !(isCancelled())) {
                totalRead += i;
                bos.write(data, 0, i);
                percent = (totalRead * 100) / size;

                // Notifying the progress.
                publishProgress((int) percent);
            }

            // 5.1. If the task was cancelled, we shall delete the downloaded files.
            if (isCancelled()) {
                Log.i(TAG, String.format("Removing file %s", goal.getName()));
                goal.delete();
            }

            // 6. Unzipping the file so we can retain only the mp3 file.
            // After that we should remove the zip file.
            UnZip.unZip(goal, mp3);
            goal.delete();
        } catch (IOException e) {
            Log.e(TAG, String.format("Coudln't download reel! %s", name.getNombre()), e);
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }

                if (bos != null) {
                    bos.flush();
                    bos.close();
                }

            } catch (IOException e) {
                Log.e(TAG, String.format("Coudln't download reel! %s", name.getNombre()), e);
            }
        }

        return mp3;
    }

    /** {@inheritDoc} */
    @Override
    protected void onPostExecute(File file) {
        if (progress.isShowing()) {
            progress.dismiss();
        }

        // call back so the main application can continue.
        activity.onReelDownloaded(file, symphony, name);
    }
}
