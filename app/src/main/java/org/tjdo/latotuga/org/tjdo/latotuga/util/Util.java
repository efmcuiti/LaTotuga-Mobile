/**
 * Util.java 
 * Created on: 1/30/15
 * This piece of work was
 * made for the exclusive use of <em>The Just DO!</em>. 
 * All rights reserved ©2015.
 */
package org.tjdo.latotuga.org.tjdo.latotuga.util;

import android.content.Context;
import android.util.Log;

import org.tjdo.latotuga.R;
import org.tjdo.services.LaTotugaFactory;
import org.tjdo.services.dto.AuthenticateResponse;
import org.tjdo.services.dto.Name;
import org.tjdo.services.dto.NamesResponse;
import org.tjdo.services.dto.Symphony;
import org.tjdo.util.LaTotugaException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Collects a set of methods handy for the
 * correct operation of the <strong>LaTotuga</strong> mobile
 * application.
 *
 * @author emcuiti (efmcuiti@gmail.com)
 */
public class Util {

    /** TAG to be ussed when logging. */
    public static final String TAG = "LaTotuga-Utils";

    /**
     * Loads the existing symphonies object based on local storage or web services.
     * @param fromServer If it should be done using web services.
     * @param context Environment of the mobile application.
     * @return The set of existent symphonies.
     * @throws LaTotugaException If any error occurs.
     */
    public static List<Symphony> getSymphonies(boolean fromServer, Context context)
            throws LaTotugaException {
        List<Symphony> answer = null;

        if (fromServer) {
            Log.i(TAG, String.format("Loading symphonies from REST services"));
            AuthenticateResponse response = LaTotugaFactory.getServices(
                    context).authenticate(
                    Constants.LATOTUGA_USERNAME, Constants.LATOTUGA_PASSWORD);

            if (response.isSuccess()) {
                answer = response.getSinfonias();
            }
        } else {
            Log.i(TAG, String.format("Loading symphonies from local storage. "));
            // Locating the local storage directory.
            answer = new ArrayList<>();
            File localStorage = context.getExternalFilesDir(null);
            File f = new File(localStorage,
                    context.getResources().getString(R.string.symphonies_file_name));
            FileReader fr = null;
            BufferedReader br = null;
            try {
                fr = new FileReader(f);
                br = new BufferedReader(fr);

                // Each line should be parsed accordingly.
                String line = null;
                Symphony s = null;
                while (br.ready()) {
                    line = br.readLine();
                    s = parseSymphony(line, context);
                    answer.add(s);
                }

            } catch (IOException e) {
                String msg = String.format("Couldn't read file %s", f.getName());
                Log.e(TAG, msg);
                throw new LaTotugaException(msg, e);
            } finally {
                try {
                    if (fr != null) {
                        fr.close();
                    }

                    if (br != null) {
                        br.close();
                    }
                } catch (IOException e) {
                    String msg = String.format("Couldn't close file %s", f.getName());
                    Log.e(TAG, msg);
                    throw new LaTotugaException(msg, e);
                }
            }
        }

        return answer;
    }

    /**
     * Loads the existing names objects based on local storage or web services.
     * @param fromServer If it should be done using web services.
     * @param context Environment of the mobile application.
     * @param symphony Parent of the name to be queried.
     * @return The set of existent names.
     * @throws LaTotugaException If any error occurs.
     */
    public static List<Name> getNames(boolean fromServer, Context context, Symphony symphony)
        throws LaTotugaException {
        List<Name> answer = null;

        if (fromServer) {
            NamesResponse response = LaTotugaFactory.getServices(
                    context).getNamesBySymphony(
                    Constants.LATOTUGA_USERNAME, Constants.LATOTUGA_PASSWORD,
                    symphony.getNombre());

            if (response.isSuccess()) {
                answer = response.getNombres();
            }
        } else {
            Log.i(TAG, String.format("Loading symphonies from local storage. "));
            // Locating the local storage directory.
            answer = new ArrayList<>();
            File localStorage = context.getExternalFilesDir(null);
            File _f = new File(localStorage, symphony.getRuta());
            File f = new File(_f,
                    context.getResources().getString(R.string.names_file_name));
            FileReader fr = null;
            BufferedReader br = null;
            try {
                fr = new FileReader(f);
                br = new BufferedReader(fr);

                // Each line should be parsed accordingly.
                String line = null;
                Name n = null;
                while (br.ready()) {
                    line = br.readLine();
                    n = parseName(line, context);
                    answer.add(n);
                }

            } catch (IOException e) {
                String msg = String.format("Couldn't read file %s", f.getName());
                Log.e(TAG, msg);
                throw new LaTotugaException(msg, e);
            } finally {
                try {
                    if (fr != null) {
                        fr.close();
                    }

                    if (br != null) {
                        br.close();
                    }
                } catch (IOException e) {
                    String msg = String.format("Couldn't close file %s", f.getName());
                    Log.e(TAG, msg);
                    throw new LaTotugaException(msg, e);
                }
            }
        }

        return answer;
    }

    /**
     * Writes the actual set of symphonies down to a file in the
     * local storage or whatever.
     * @param local If should be saved on local storage.
     * @param symphonies What to write.
     * @param context Where to extract environmental information.
     * @throws LaTotugaException If any error occurs.
     */
    public static void saveSymphonies(boolean local, List<Symphony> symphonies, Context context)
        throws LaTotugaException {
        // 1. Getting the local directory where to save the files.
        File localStorage = context.getExternalFilesDir(null);

        // 2. Preparing the object to write to.
        File f = new File(localStorage,
                context.getResources().getString(R.string.symphonies_file_name));
        FileWriter fw = null;
        try {
            fw = new FileWriter(f, false);

            // 2.1. Each symphony goes in a line.
            // each symphony should have it's own directory.
            String line = "";
            String split = context.getResources().getString(R.string.csv_separator);
            File reels = null;
            File _reels = null;
            for (Symphony s : symphonies) {
                line = s.getId_sinfonia() + split +
                        s.getNombre() + split +
                        s.getRuta() + "\n";
                fw.write(line);

                // Building the folder fot the symphony's reels.
                reels = new File(localStorage, s.getRuta());
                if (!reels.exists()) {
                    reels.mkdirs();
                }
                _reels = new File(reels,
                        context.getResources().getString(R.string.reels_directory_name));
                if (!_reels.exists()) {
                    _reels.mkdirs();
                }
            }

        } catch (IOException e) {
            String msg = String.format("Couldn't find file %s", f.getName());
            Log.e(TAG, msg);
            throw new LaTotugaException(msg, e);
        } finally {
            try {
                if (fw != null) {
                    fw.flush();
                    fw.close();
                }
            } catch (IOException e) {
                String msg = String.format("Couldn't close file %s", f.getName());
                Log.e(TAG, msg);
                throw new LaTotugaException(msg, e);
            }
        }
    }

    /**
     * Writes the actual set of names for a symphony down to a file in the
     * local storage or whatever.
     * @param local If should be saved on local storage.
     * @param symphony Parent of the names to be written.
     * @param names Names to be written down.
     * @param context Where to extract environmental information.
     * @throws LaTotugaException If any error occurs.
     */
    public static void saveNames(boolean local, Symphony symphony, List<Name> names,
                                      Context context) throws LaTotugaException {
        // 1. Getting the local directory where to save the files.
        File localStorage = context.getExternalFilesDir(null);

        // 2. Preparing the object to write to.
        File symphonyDir = new File(localStorage, symphony.getRuta());
        File f = new File(symphonyDir,
                context.getResources().getString(R.string.names_file_name));
        FileWriter fw = null;

        try {
            fw = new FileWriter(f, false);

            // 2.1. Each name goes in a line.
            String line = "";
            String split = context.getResources().getString(R.string.csv_separator);
            for (Name n : names) {
                line = n.getId_nombre() + split +
                        n.getNombre() + split +
                        n.getRuta() + "\n";
                fw.write(line);
            }
        } catch (IOException e) {
            String msg = String.format("Couldn't find file %s", f.getName());
            Log.e(TAG, msg);
            throw new LaTotugaException(msg, e);
        } finally {
            try {
                if (fw != null) {
                    fw.flush();
                    fw.close();
                }
            } catch (IOException e) {
                String msg = String.format("Couldn't close file %s", f.getName());
                Log.e(TAG, msg);
                throw new LaTotugaException(msg, e);
            }
        }
    }

    /**
     * Builds a symphony object given a line.
     * @param line Raw line to be transformed.
     * @param context Environment of the mobile application.
     * @return The parsed object.
     */
    public static Symphony parseSymphony(String line, Context context) {
        Symphony s = new Symphony();

        String split = context.getResources().getString(R.string.csv_separator);
        String parts[] = line.split(split);

        s.setId_sinfonia(parts[0]);
        s.setNombre(parts[1]);
        s.setRuta(parts[2]);

        return s;
    }

    /**
     * Builds a name object given a line.
     * @param line Raw line to be transformed.
     * @param context Environment of the mobile application.
     * @return The parsed object.
     */
    public static Name parseName(String line, Context context) {
        Name n = new Name();

        String split = context.getResources().getString(R.string.csv_separator);
        String parts[] = line.split(split);

        n.setId_nombre(parts[0]);
        n.setNombre(parts[1]);
        n.setRuta(parts[2]);

        return n;
    }
}
