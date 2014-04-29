package com.dougedey.strangebrew.malt;

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


    public static void addMalt(Fermentable f) {
        int id = ITEMS.size();
        String id_s = Integer.toString(id);

        ITEMS.add(f);
        ITEM_MAP.put(id_s, f);
    }

}
