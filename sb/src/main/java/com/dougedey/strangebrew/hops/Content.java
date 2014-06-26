package com.dougedey.strangebrew.hops;

import android.content.res.Resources;

import com.dougedey.strangebrew.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.strangebrew.Hop;

/**
 * Created by doug on 22/04/14.
 */
public class Content {
    /**
     * An array of hop items.
     */
    public static List<Hop> ITEMS = new ArrayList<Hop>();

    /**
     * A map of hop items, by ID.
     */
    public static Map<String, Hop> ITEM_MAP = new HashMap<String, Hop>();

    public static Resources resources = null;
    private static Hop newHop = null;

    public static void clear() {
        // Clear and add the blank hop in
        ITEM_MAP.clear();
        ITEMS.clear();

        addNew();
    }

    public static void setResources(Resources res) {
        resources = res;
    }

    public static void addNew() {
        if (newHop == null) {
            newHop = new Hop();
            newHop.setName(resources.getString(R.string.new_string));
        }

        ITEM_MAP.put("999", newHop);
        ITEMS.add(newHop);
    }

    public static void addHop(Hop h) {

        int id = ITEMS.size();

        // pop off and push back the new element
        ITEMS.remove(id-1);
        ITEM_MAP.remove("999");

        String id_s = Integer.toString(id);

        ITEMS.add(h);
        ITEM_MAP.put(id_s, h);

        addNew();
    }


}
