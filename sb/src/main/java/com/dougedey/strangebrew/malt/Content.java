package com.dougedey.strangebrew.malt;

import android.content.res.Resources;

import com.dougedey.strangebrew.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.strangebrew.Fermentable;
import ca.strangebrew.Hop;

/**
 * Created by doug on 22/04/14.
 */
public class Content {
    /**
     * An array of fermentable items.
     */
    public static List<Fermentable> ITEMS = new ArrayList<Fermentable>();

    /**
     * A map of fermentable items, by ID.
     */
    public static Map<String, Fermentable> ITEM_MAP = new HashMap<String, Fermentable>();

    public static Resources resources = null;

    private static Fermentable newFerm = null;

    public static void clear() {
        // Clear and add the blank Fermentable in
        ITEM_MAP.clear();
        ITEMS.clear();

        addNew();
    }

    public static void setResources(Resources res) {
        resources = res;
    }

    public static void addNew() {
        if (newFerm == null) {
            newFerm = new Fermentable();
            newFerm.setName(resources.getString(R.string.new_string));
        }
        ITEM_MAP.put("999", newFerm);
        ITEMS.add(newFerm);
    }

    public static void addMalt(Fermentable f) {
        int id = ITEMS.size();
        String id_s = Integer.toString(id);

        // pop off and push back the new element
        ITEMS.remove(id-1);
        ITEM_MAP.remove("999");

        ITEMS.add(f);
        ITEM_MAP.put(id_s, f);

        addNew();
    }

}
