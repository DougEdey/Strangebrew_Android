package com.dougedey.strangebrew.recipe;

import android.content.res.AssetManager;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by doug on 15/04/14.
 */
public class Content {

    /**
     * An array of sample (dummy) items.
     */
    public static List<RecipeItem> ITEMS = new ArrayList<RecipeItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, RecipeItem> ITEM_MAP = new HashMap<String, RecipeItem>();


    public static void clear() {
        Content.ITEM_MAP.clear();
        Content.ITEMS.clear();
    }
    public static void addRecipe(String name, String file, Double colour) {

        if (ITEMS.size() == 0) {
            RecipeItem r = new RecipeItem("0", "Download Recipe", "", 0.0);
            ITEMS.add(r);
            ITEM_MAP.put("0", r);
        }

        int id = ITEMS.size();
        String id_s = Integer.toString(id);
        RecipeItem r = new RecipeItem(id_s, name, file, colour);
        ITEMS.add(r);
        ITEM_MAP.put(id_s, r);
    }

    public static class RecipeItem {
        public String file;
        public String name;
        public String id;
        public Double color;

        public RecipeItem(String id, String name, String file, Double color) {
            this.id = id;
            this.file = file;
            this.name = name;
            this.color = color;
        }
    }
}
