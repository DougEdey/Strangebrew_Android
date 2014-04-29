package com.dougedey.strangebrew.hops;

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
     * An array of sample (dummy) items.
     */
    public static List<Hop> ITEMS = new ArrayList<Hop>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, Hop> ITEM_MAP = new HashMap<String, Hop>();


    public static void addHop(Hop h) {
        int id = ITEMS.size();
        String id_s = Integer.toString(id);

        ITEMS.add(h);
        ITEM_MAP.put(id_s, h);
    }


}
