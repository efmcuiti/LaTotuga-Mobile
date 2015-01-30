/**
 * NameItem.java 
 * Created on: 1/29/15
 * This piece of work was
 * made for the exclusive use of <em>The Just DO!</em>. 
 * All rights reserved ©2015.
 */
package org.tjdo.latotuga.org.tjdo.latotuga.util;

/**
 * Represents a basic object to transport one child name information.
 *
 * @author emcuiti (efmcuiti@gmail.com)
 */
public class NameItem {
    /** Actual name of the child. */
    private String name;

    /**
     * Gets the name to be painted in the list view.
     * @return The name of the child.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the child.
     * @param name Name to be painted on the list view.
     */
    public void setName(String name) {
        this.name = name;
    }
}
