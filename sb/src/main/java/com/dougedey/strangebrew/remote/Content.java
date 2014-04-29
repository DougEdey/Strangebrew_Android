package com.dougedey.strangebrew.remote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.strangebrew.Fermentable;

/**
 * Created by doug on 26/04/14.
 */
public class Content {
    public static List<BasicRecipe> ITEMS = new ArrayList<BasicRecipe>();

    /**
     * A map of fermentable items, by ID.
     */
    public static Map<String, BasicRecipe> ITEM_MAP = new HashMap<String, BasicRecipe>();

    public static void clear() {
        ITEMS.clear();
        ITEM_MAP.clear();
    }

    public static void addRecipe(BasicRecipe f) {
        int id = ITEMS.size();
        String id_s = Integer.toString(id);

        ITEMS.add(f);
        ITEM_MAP.put(id_s, f);
    }

}
