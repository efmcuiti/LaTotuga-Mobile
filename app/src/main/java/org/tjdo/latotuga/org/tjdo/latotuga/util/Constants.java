/**
 * Constants.java 
 * Created on: 1/27/15
 * This piece of work was
 * made for the exclusive use of <em>The Just DO!</em>. 
 * All rights reserved ©2015.
 */
package org.tjdo.latotuga.org.tjdo.latotuga.util;

/**
 * Defines a set of invariable values to be used in the application.
 *
 * @author emcuiti (efmcuiti@gmail.com)
 */
public class Constants {

    /** This will be used to authenticate against the server. */
    public static final String LATOTUGA_USERNAME = "edison";

    /** This will be used to authenticate against the server. */
    public static final String LATOTUGA_PASSWORD = "ingeniero";

    /** Where to find and download the reel linked to a certain name and symphony. */
    public static final String LATOTUGA_REELS_URL_BASE = "http://latortuga.s3.amazonaws.com/";

    /** How much read on a buffer. */
    public static final int BLOCK_SIZE = 1024;

    /** Default zip file extension. */
    public static final String ZIP_EXT = ".zip";

    /** Default mp3 file extension. */
    public static final String MP3_EXT = ".mp3";
}
