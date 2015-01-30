/**
 * NameListAdapter.java 
 * Created on: 1/29/15
 * This piece of work was
 * made for the exclusive use of <em>The Just DO!</em>. 
 * All rights reserved ©2015.
 */
package org.tjdo.latotuga;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.tjdo.latotuga.org.tjdo.latotuga.util.NameItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the standard adapter for the names
 * listing in the main interface view.
 *
 * @author emcuiti (efmcuiti@gmail.com)
 */
public class NameListAdapter extends BaseAdapter {

    /** Used in order to log messages to console. */
    public static final String TAG = "LaTotuga-NameListAdapter";

    /** Contents of the list view. */
    private final List<NameItem> children = new ArrayList<>();

    /** Android application context to be used. */
    private final Context context;

    /**
     * Default constructor with the android application context to be used.
     * @param context Where to look for environmental stuff.
     */
    public NameListAdapter(Context context) {
        this.context = context;
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return children.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return children.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1. Get the item being affected.
        NameItem child = (NameItem) getItem(position);

        // 2. Inflate the layout content for the new row.
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout itemLayout = (convertView == null) ?
                (LinearLayout) inflater.inflate(R.layout.name_list_item_view, null) :
                (LinearLayout) convertView.findViewById(R.id.name_item_layout);

        // 3. Fill in the row details.
        final TextView initial = (TextView) itemLayout.findViewById(R.id.initial_letter_item);
        initial.setText(getInitial(position));

        final TextView name = (TextView) itemLayout.findViewById(R.id.name_item);
        name.setText(child.getName());

        return itemLayout;
    }

    /**
     * Based on name location in the children list this method
     * processes the initial to be painted.
     * @param position Where in the list is the name.
     * @return The initial to be painted (may be blank).
     */
    private String getInitial(int position) {
        NameItem child = (NameItem) getItem(position);
        String candidate = child.getName().charAt(0) + "";
        if (position == 0) {
            return candidate;
        }

        // Checking the previous name.
        child = (NameItem) getItem(position-1);
        if (!candidate.equalsIgnoreCase(child.getName().charAt(0) + "")) {
            return candidate;
        }

        return "";
    }

    /**
     * Removes all the items used for the list view.
     */
    public void clear() {
        children.clear();
        notifyDataSetChanged();
    }

    /**
     * Adds a new child name to be shown.
     * @param item New name to add to the list view.
     */
    public void add(NameItem item) {
        children.add(item);
    }
}
