/**
 * ChildrenListAdapter.java 
 * Created on: 2/2/15
 * This piece of work was
 * made for the exclusive use of <em>The Just DO!</em>. 
 * All rights reserved ©2015.
 */
package org.tjdo.latotuga;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import org.tjdo.latotuga.org.tjdo.latotuga.util.NameItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Implements a fast-scrollable-list with
 * index.
 *
 * @author emcuiti (efmcuiti@gmail.com)
 */
public class ChildrenListAdapter extends ArrayAdapter<String> implements SectionIndexer {

    /** Defines section and indexes for the fast scrolling. */
    private HashMap<String, Integer> mapIndex;

    /** Used to divide the information to be presented. */
    private String sections[];

    /** Children names to be presented. */
    private List<String> names;

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param names The content to show.
     */
    public ChildrenListAdapter(Context context, List<String> names) {
        super(context, R.layout.name_list_item_view,names);

        this.names = names;

        mapIndex = new LinkedHashMap<>();
        String i = "";
        for (String n : names) {
            i = n.charAt(0) + "";
            if (!mapIndex.containsKey(i)) {
                mapIndex.put(i, names.indexOf(n));
            }
        }

        Set<String> sectionLetters = mapIndex.keySet();
        List<String> sectionList = new ArrayList<>(sectionLetters);
        Collections.sort(sectionList);
        sections = new String[sectionList.size()];
        sectionList.toArray(sections);
    }

    /**
     * Returns an array of objects representing sections of the list. The
     * returned array and its contents should be non-null.
     * <p/>
     * The list view will call toString() on the objects to get the preview text
     * to display while scrolling. For example, an adapter may return an array
     * of Strings representing letters of the alphabet. Or, it may return an
     * array of objects whose toString() methods return their section titles.
     *
     * @return the array of section objects
     */
    @Override
    public Object[] getSections() {
        return sections;
    }

    /**
     * Given the index of a section within the array of section objects, returns
     * the starting position of that section within the adapter.
     * <p/>
     * If the section's starting position is outside of the adapter bounds, the
     * position must be clipped to fall within the size of the adapter.
     *
     * @param sectionIndex the index of the section within the array of section
     *                     objects
     * @return the starting position of that section within the adapter,
     * constrained to fall within the adapter bounds
     */
    @Override
    public int getPositionForSection(int sectionIndex) {
        return mapIndex.get(sections[sectionIndex]);
    }

    /**
     * Given a position within the adapter, returns the index of the
     * corresponding section within the array of section objects.
     * <p/>
     * If the section index is outside of the section array bounds, the index
     * must be clipped to fall within the size of the section array.
     * <p/>
     * For example, consider an indexer where the section at array index 0
     * starts at adapter position 100. Calling this method with position 10,
     * which is before the first section, must return index 0.
     *
     * @param position the position within the adapter for which to return the
     *                 corresponding section index
     * @return the index of the corresponding section within the array of
     * section objects, constrained to fall within the array bounds
     */
    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @param position
     * @param convertView
     * @param parent
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1. Get the item being affected.
        String child = names.get(position);

        // 2. Inflate the layout content for the new row.
        Context context = getContext();
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout itemLayout = (convertView == null) ?
                (LinearLayout) inflater.inflate(R.layout.name_list_item_view, null) :
                (LinearLayout) convertView.findViewById(R.id.name_item_layout);

        // 3. Fill in the row details.
        final TextView initial = (TextView) itemLayout.findViewById(R.id.initial_letter_item);
        initial.setText(getInitial(position));

        final TextView name = (TextView) itemLayout.findViewById(R.id.name_item);
        name.setText(child);

        return itemLayout;
    }

    /**
     * Based on name location in the children list this method
     * processes the initial to be painted.
     * @param position Where in the list is the name.
     * @return The initial to be painted (may be blank).
     */
    private String getInitial(int position) {
        String child = names.get(position);
        String candidate = child.charAt(0) + "";
        if (position == 0) {
            return candidate;
        }

        // Checking the previous name.
        child = names.get(position-1);
        if (!candidate.equalsIgnoreCase(child.charAt(0) + "")) {
            return candidate;
        }

        return "";
    }
}
