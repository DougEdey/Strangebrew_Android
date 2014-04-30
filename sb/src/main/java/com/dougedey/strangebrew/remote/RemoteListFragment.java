package com.dougedey.strangebrew.remote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.dougedey.strangebrew.R;
import com.dougedey.strangebrew.recipe.DetailFragment;
import com.dougedey.strangebrew.remote.Content;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import ca.strangebrew.Debug;
import ca.strangebrew.ImportXml;
import ca.strangebrew.Recipe;


/**
 * A list fragment representing a list of Hops. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link com.dougedey.strangebrew.recipe.DetailFragment}.
 * <p>

 */
public class RemoteListFragment extends android.support.v4.app.ListFragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";


    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RemoteListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(new ListAdapter(
                getActivity(),
                Content.ITEMS));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        RemoteRecipes downloadTask = new RemoteRecipes();
        downloadTask.setActivity(this);
        downloadTask.execute(Content.ITEMS.get(position));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    private void newFileSaved() {

        File file = new File(Environment.getExternalStorageDirectory(), "StrangeBrew/Recipes/");
        File[] recipes = file.listFiles();

        com.dougedey.strangebrew.recipe.Content.clear();

        /* TODO: Create a cache so we don't load all the recipes on startup */
        for (File rFile: recipes) {
            ImportXml x = new ImportXml(rFile.getAbsolutePath());
            if (x.handler != null) {
                Recipe r = x.getRecipe();
                if (r != null) {
                    com.dougedey.strangebrew.recipe.Content.addRecipe(
                            r.getName(), rFile.getAbsolutePath(), r.getColour("LOV"));
                }
            }
        }

        Collections.sort(com.dougedey.strangebrew.recipe.Content.ITEMS);
        getFragmentManager().findFragmentById(R.layout.activity_recipe_list).notify();
    }

    public class RemoteRecipes extends AsyncTask<BasicRecipe, Integer, String> {

        public String cloudURL = "strangebrewcloud.appspot.com";
        private ArrayList<String> input = null;
        public String results = null;

        private RemoteListFragment activity = null;
        private ProgressDialog pleaseWaitDialog = null;
        private boolean completed = false;

        protected String doInBackground(BasicRecipe... recipes) {
            input = new ArrayList<String>();

            int count = recipes.length;

            for (int i = 0; i < count; i++) {

                if (recipes[i] != null) {
                    results = (getRemoteRecipe(recipes[i]));
                }
                publishProgress((int) ((i / (float) count) * 100));
                // Escape early if cancel() is called
                if (isCancelled()) break;
            }

            //return Void;
            return results;
        }

        //Pre execution actions
        @Override
        protected void onPreExecute() {
            //Start the splash screen dialog
            if (pleaseWaitDialog == null) {
//            Context acontext = activity.getActivity().getApplicationContext();
//            Context bcontext = activity.getActivity().getBaseContext();

                pleaseWaitDialog = ProgressDialog.show(getActivity(),
                        "Please Wait",
                        "Getting Recipe...",
                        false);
            }
        }

        //Post execution actions
        @Override
        protected void onPostExecute(String response) {
            //Set task completed and notify the activity
            completed = true;
            results = response;
            notifyActivityTaskCompleted();
            //Close the splash screen
            if (pleaseWaitDialog != null) {
                pleaseWaitDialog.dismiss();
                pleaseWaitDialog = null;
            }
        }

        //Notify activity of async task completion
        private void notifyActivityTaskCompleted() {
            if (null != activity) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                // set title
                alertDialogBuilder.setTitle("Recipe saved");

                // set dialog message
                alertDialogBuilder
                        .setMessage("Saved to: " + this.results)
                        .setNeutralButton("OK", null);

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

                activity.newFileSaved();
            }
        }

        //for maintain attached the async task to the activity in phone states changes
        //Sets the current activity to the async task
        public void setActivity(RemoteListFragment activity) {
            this.activity = activity;
            if (completed) {
                notifyActivityTaskCompleted();
            }
        }

        private String getRemoteRecipe(BasicRecipe recipe) {
            long id = recipe.id;
            String title = recipe.title;
            String brewer = recipe.brewer;
            int iteration = recipe.iteration;

            try {
                String baseURL = "http://strangebrewcloud.appspot.com";

                URL url = new URL(baseURL + "/recipes/" + id);
                InputStream response = url.openStream();

                HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                huc.setRequestMethod("GET");
                huc.connect();

                int code = huc.getResponseCode();

                if (code != 200) {

                    huc.disconnect();

                    return "";

                }
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

                dbf.setValidating(false);
                dbf.setIgnoringComments(false);
                dbf.setIgnoringElementContentWhitespace(true);
                dbf.setNamespaceAware(true);
                // dbf.setCoalescing(true);
                // dbf.setExpandEntityReferences(true);

                DocumentBuilder db = null;
                db = dbf.newDocumentBuilder();
                //db.setEntityResolver(new NullResolver());

                // db.setErrorHandler( new MyErrorHandler());

                Document readXML = db.parse(response);

                // check if we already have this file
                String file = title + " - " + brewer + " (" + iteration + ").xml";

                String currentDir = Environment.getExternalStorageDirectory() + "/StrangeBrew/Recipes/";
                File recipeFile = new File(currentDir, file);

                if (recipeFile.exists()) {
                    //TODO: Prompt for a new filename
                    return recipeFile.getAbsolutePath();

                }

                Debug.print("Writing recipe file: " + recipeFile.getAbsolutePath());
                recipeFile.createNewFile();

                // file doesn't exist, lets dump the file
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");

                StreamResult result = new StreamResult(new StringWriter());
                DOMSource source = new DOMSource(readXML);
                transformer.transform(source, result);

                OutputStream oStream = new FileOutputStream(recipeFile);
                String outXML = result.getWriter().toString();
                oStream.write(outXML.getBytes(Charset.forName("UTF-8")));
                oStream.close();
                return recipeFile.getAbsolutePath();

            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }


    }
}
