package com.dougedey.strangebrew.recipe;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.dougedey.strangebrew.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ca.strangebrew.Database;
import ca.strangebrew.ImportXml;
import ca.strangebrew.Recipe;


/**
 * An activity representing a list of Recipes. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link DetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link RecipeListFragment} and the item details
 * (if present) is a {@link DetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link RecipeListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ListActivity extends FragmentActivity
        implements RecipeListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.loadRecipes();

        File dataDir = new File(Environment.getExternalStorageDirectory(), "StrangeBrew/Data/");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
            copyDataFromAssetsToSD(dataDir);
        }

        Database db = Database.getInstance();
        db.readDB("", "2008");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        if (findViewById(R.id.recipe_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((RecipeListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.recipe_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link RecipeListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(DetailFragment.ARG_ITEM_ID, id);
            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.recipe_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, DetailActivity.class);
            detailIntent.putExtra(DetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    public void loadRecipes() {
        File file = new File(Environment.getExternalStorageDirectory(), "StrangeBrew/Recipes/");

        if (!file.exists()) {
            if (!file.mkdirs()){
                Log.e("Recipe Create", "Could not create the SDCard dir. " + file.getAbsolutePath());
                return;
            }
            copyRecipesFromAssetsToSD(file);
        }

        File[] recipes = file.listFiles();

        if (recipes == null || recipes.length == 0) {
            copyRecipesFromAssetsToSD(file);
            recipes = file.listFiles();
        }

        Content.clear();

        /* TODO: Create a cache so we don't load all the recipes on startup */
        for (File rFile: recipes) {
            ImportXml x = new ImportXml(rFile.getAbsolutePath());
            if (x.handler != null) {
                Recipe r = x.getRecipe();
                if (r != null) {
                    Content.addRecipe(
                            r.getName(), rFile.getAbsolutePath(), r.getColour("LOV"));
                }
            }
        }
    }

    public void copyRecipesFromAssetsToSD(File target) {

        AssetManager manager = this.getAssets();
        try {
            String rList[] = manager.list("Recipes");
            if (rList != null && rList.length > 0) {
                for (String rFile: rList) {
                    InputStream in = null;
                    OutputStream out = null;

                    try {
                        in = manager.open("Recipes/"+rFile);
                        File outfile = new File(target.getAbsolutePath(), rFile);
                        if (!outfile.exists()) {
                            outfile.createNewFile();
                        }
                        out = new FileOutputStream(outfile);
                        copyFile(in, out);
                        in.close();
                        in = null;
                        out.flush();
                        ((FileOutputStream)out).getFD().sync();
                        out.close();
                        out = null;
                        Log.i("Recipe Load", "Copied file " + rFile);
                    } catch(IOException e) {
                        Log.e("tag", "Failed to copy asset file: " + rFile, e);
                    }
                }

            } else {
                Log.w("Recipe Load", "No Recipes in the assets");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void copyDataFromAssetsToSD(File target) {

        AssetManager manager = this.getAssets();
        try {
            String rList[] = manager.list("data");
            if (rList != null && rList.length > 0) {
                for (String rFile: rList) {
                    InputStream in = null;
                    OutputStream out = null;

                    try {
                        in = manager.open("data/"+rFile);
                        File outfile = new File(target.getAbsolutePath(), rFile);
                        if (!outfile.exists()) {
                            outfile.createNewFile();
                        }
                        out = new FileOutputStream(outfile);
                        copyFile(in, out);
                        in.close();
                        in = null;
                        out.flush();
                        ((FileOutputStream)out).getFD().sync();
                        out.close();
                        out = null;
                        Log.i("Data Load", "Copied file " + rFile);
                    } catch(IOException e) {
                        Log.e("tag", "Failed to copy asset file: " + rFile, e);
                    }
                }

            } else {
                Log.w("Recipe Load", "No Recipes in the assets");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}
