package com.dougedey.strangebrew.recipe;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.dougedey.strangebrew.R;
import com.dougedey.strangebrew.StyleListAdapter;
import com.dougedey.strangebrew.YeastListAdapter    ;
import com.dougedey.strangebrew.malt.ListFragment;
import com.dougedey.strangebrew.remote.BasicRecipe;
import com.dougedey.strangebrew.remote.RemoteListFragment;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import ca.strangebrew.Database;
import ca.strangebrew.Debug;
import ca.strangebrew.Fermentable;
import ca.strangebrew.Hop;
import ca.strangebrew.ImportXml;
import ca.strangebrew.Quantity;
import ca.strangebrew.Recipe;

/**
 * A fragment representing a single Recipe detail screen.
 * This fragment is either contained in a {@link ListActivity}
 * in two-pane mode (on tablets) or a {@link DetailActivity}
 * on handsets.
 */
public class DetailFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The content this item is representing
     */
    private Content.RecipeItem mItem;

    /**
     * The Recipe itself
     */
    private Recipe recipe = null;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = Content.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = null;

        // Show the dummy content as text in a TextView.
        if (mItem != null && !mItem.file.equals("")) {
            rootView = inflater.inflate(R.layout.fragment_recipe_detail, container, false);
            ImportXml importXml = new ImportXml(mItem.file);
            if (importXml.handler != null) {
                recipe = importXml.handler.getRecipe();
                recipe.calcFermentTotals();
                recipe.calcHopsTotals();
                recipe.calcMaltTotals();
                recipe.calcPrimeSugar();
            }
            ((TextView) rootView.findViewById(R.id.title_text)).setText(recipe.getName());
            rootView = showRecipe(rootView);
        } else if (mItem.file.equals("")) {
            // Start the new activity for getting a recipe
            rootView = inflater.inflate(R.layout.cloud_recipes, container, false);
            startCloudDownloads();
        }
        return rootView;
    }

    public View showRecipe(View rootView) {
        // ABV
        TextView abv_text = (TextView) rootView.findViewById(R.id.abv_text);
        DecimalFormat df = new DecimalFormat("#.##");
        String tDouble = df.format(recipe.getAlcohol());
        abv_text.setText(tDouble + "%");

        // IBU
        TextView ibu_text = (TextView) rootView.findViewById(R.id.ibu_text);
        tDouble = df.format(recipe.getIbu());
        ibu_text.setText(tDouble + " IBU");

        // Setup the style Dropdown
        Spinner styleSpin = (Spinner) rootView.findViewById(R.id.style_spinner);

        StyleListAdapter styleAdapter =
            new StyleListAdapter(getActivity(), Database.getInstance().styleDB);

        styleSpin.setAdapter(styleAdapter);
        styleSpin.setSelection(styleAdapter.getPosition(recipe.getStyleObj()));

        // Setup the Yeast Drop Down
        Spinner yeastSpin = (Spinner) rootView.findViewById(R.id.yeast_spinner);

        YeastListAdapter yeastAdapter =
                new YeastListAdapter(getActivity(), Database.getInstance().yeastDB);

        yeastSpin.setAdapter(yeastAdapter);
        yeastSpin.setSelection(yeastAdapter.getPosition(recipe.getYeastObj()));

        // Original Gravity
        EditText og = (EditText) rootView.findViewById(R.id.og_picker);
        DecimalFormat three_df = new DecimalFormat("#.###");
        tDouble = three_df.format(recipe.getEstOg());
        og.setText(tDouble);

        og = (EditText) rootView.findViewById(R.id.fg_picker);
        tDouble = three_df.format(recipe.getEstFg());
        og.setText(tDouble);

        og = (EditText) rootView.findViewById(R.id.eff_picker);
        tDouble = df.format(recipe.getEfficiency());
        og.setText(tDouble + "%");

        og = (EditText) rootView.findViewById(R.id.att_picker);
        tDouble = df.format(recipe.getAttenuation());
        og.setText(tDouble + "%");


        // Volumes
        ArrayAdapter<String> volAdapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_expandable_list_item_1);
        List<String> s = Quantity.getListofUnits("volume", true);
        volAdapter.addAll(s);
        Spinner volSpin = (Spinner) rootView.findViewById(R.id.preb_vol_spinner);
        volSpin.setAdapter(volAdapter);
        int pos = volAdapter.getPosition(Quantity.getVolAbrv(recipe.getVolUnits()));
        volSpin.setSelection(pos);

        /*
        ArrayAdapter<String> postVolAdapter = new ArrayAdapter<String>(
                 getActivity(), android.R.layout.simple_expandable_list_item_1);
        postVolAdapter.addAll(s);
        Spinner postVolSpin = (Spinner) rootView.findViewById(R.id.postb_vol_spinner);
        postVolSpin.setAdapter(postVolAdapter);
        pos = postVolAdapter.getPosition(Quantity.getVolAbrv(recipe.getVolUnits()));
        postVolSpin.setSelection(pos);
        */

        og = (EditText) rootView.findViewById(R.id.preb_picker);
        tDouble = df.format(recipe.getPreBoilVol());
        og.setText(tDouble);

        og = (EditText) rootView.findViewById(R.id.postb_picker);
        tDouble = df.format(recipe.getFinalWortVol());
        og.setText(tDouble);

        // Build up the malt tables
        LinearLayout maltLayout = (LinearLayout) rootView.findViewById(R.id.malt_table);
        getFragmentManager().beginTransaction().add(R.id.malt_table, new ListFragment(), "malt_list").commit();

        com.dougedey.strangebrew.malt.Content.ITEM_MAP.clear();
        com.dougedey.strangebrew.malt.Content.ITEMS.clear();

        for (int i = 0; i < recipe.getMaltListSize(); i++) {
            Fermentable f = recipe.getFermentable(i);
            com.dougedey.strangebrew.malt.Content.addMalt(f);
        }

        // Build up the Hop Table
        LinearLayout hopsLayout = (LinearLayout) rootView.findViewById(R.id.hops_table);
        getFragmentManager().beginTransaction().add(R.id.hops_table, new com.dougedey.strangebrew.hops.ListFragment(), "hops_list").commit();

        com.dougedey.strangebrew.hops.Content.ITEM_MAP.clear();
        com.dougedey.strangebrew.hops.Content.ITEMS.clear();

        for (int i = 0; i < recipe.getHopsListSize(); i++) {
            Hop h = recipe.getHop(i);
            com.dougedey.strangebrew.hops.Content.addHop(h);
        }

        return rootView;
    }

    public void startCloudDownloads() {
        // Do the styles
        RemoteRecipes asyncTask = new RemoteRecipes();
        asyncTask.setActivity(this);
        asyncTask.execute("styles");

        RemoteRecipes brewerAsyncTask = new RemoteRecipes();
        brewerAsyncTask.setActivity(this);
        brewerAsyncTask.execute("brewers");
    }

    public void styleListDownloaded(ArrayList<Object> inList) {
        ArrayList<String> styleList = new ArrayList<String>();
        for (Object o: inList) {
            styleList.add((String) o);
        }
        Collections.sort(styleList);
        View rootView = this.getView();
        if (styleList != null) {
            Spinner spinner = (Spinner) rootView.findViewById(R.id.cloud_style);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    getActivity(), android.R.layout.simple_expandable_list_item_1);
            adapter.addAll(styleList);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(this);
        }
    }

    public void brewersListDownloaded(ArrayList<Object> inList) {
        ArrayList<String> brewerList = new ArrayList<String>();
        for (Object o: inList) {
            brewerList.add((String) o);
        }
        Collections.sort(brewerList);
        View rootView = this.getView();
        if (brewerList != null) {
            Spinner spinner = (Spinner) rootView.findViewById(R.id.cloud_brewer);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    getActivity(), android.R.layout.simple_expandable_list_item_1);
            adapter.addAll(brewerList);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(this);
        }
    }

    public void updateRecipeList(ArrayList<Object> inList) {

        if (inList == null || inList.size() == 0) {
            return;
        }

        ArrayList<BasicRecipe> recipeList = new ArrayList<BasicRecipe>();

        // Build up the Recipe Table
        LinearLayout hopsLayout = (LinearLayout) this.getView().findViewById(R.id.cloud_body);
        RemoteListFragment f = (RemoteListFragment) getFragmentManager().findFragmentByTag("cloud_list");
        if (f != null) {
            getFragmentManager().beginTransaction().remove(f).commit();
        }
        getFragmentManager().beginTransaction().add(R.id.cloud_body, new RemoteListFragment(), "cloud_list").commit();
        f = (RemoteListFragment) getFragmentManager().findFragmentByTag("cloud_list");


        com.dougedey.strangebrew.remote.Content.clear();

        for (int i = 0; i < inList.size(); i++) {
            BasicRecipe r = (BasicRecipe) inList.get(i);
            if (i == 0) {
                TextView styleView = (TextView) this.getView().findViewById(R.id.c_h_style);
                TextView brewerView = (TextView) this.getView().findViewById(R.id.c_h_brewer);
                if (r.search.equalsIgnoreCase("style")) {
                    styleView.setVisibility(View.GONE);
                    brewerView.setVisibility(View.VISIBLE);
                } else {
                    styleView.setVisibility(View.VISIBLE);
                    brewerView.setVisibility(View.GONE);
                }
            }
            com.dougedey.strangebrew.remote.Content.addRecipe(r);
        }

        try {
            synchronized (f) {
                f.notifyAll();
            }
        } catch (NullPointerException npe) {
            Debug.print(npe);
            return;
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        if (adapterView.getId() == R.id.cloud_brewer) {
            Spinner spinner = (Spinner) adapterView;
            String brewer = (String) spinner.getSelectedItem();
            RemoteRecipes brewerTask = new RemoteRecipes();
            brewerTask.setActivity(this);
            brewerTask.execute("brewer", brewer);
        }

        if (adapterView.getId() == R.id.cloud_style) {
            Spinner spinner = (Spinner) adapterView;
            String style = (String) spinner.getSelectedItem();
            RemoteRecipes styleTask = new RemoteRecipes();
            styleTask.setActivity(this);
            styleTask.execute("style", style);
        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    public class RemoteRecipes extends AsyncTask<String, Integer, ArrayList<Object>> {

        public String cloudURL = "strangebrewcloud.appspot.com";
        private ArrayList<String> input = null;
        public ArrayList<Object> results = null;

        private DetailFragment activity = null;
        private ProgressDialog pleaseWaitDialog = null;
        private boolean completed = false;

        protected ArrayList<Object> doInBackground(String... types) {
            input = new ArrayList<String>();

            int count = types.length;

            for (int i = 0; i < count; i++) {
                input.add(types[i]);
                if (types[i].equalsIgnoreCase("styles")) {
                    results = getListOfStyles();
                }

                if (types[i].equalsIgnoreCase("brewers")) {
                    results = getListOfBrewers();
                }

                if (types[i].equalsIgnoreCase("style")) {
                    if (count < 2 || types[++i].equalsIgnoreCase("")) {
                        return null;
                    }
                    results = loadRecipesByStyle(types[i]);

                }

                if (types[i].equalsIgnoreCase("brewer")) {
                    if (count < 2 || types[++i].equalsIgnoreCase("")) {
                        return null;
                    }
                    results = loadRecipesByBrewer(types[i]);
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
                        "Getting results...",
                        false);
            }
        }
        //Post execution actions
        @Override
        protected void onPostExecute(ArrayList<Object> response)
        {
            //Set task completed and notify the activity
            completed = true;
            results = response;
            notifyActivityTaskCompleted();
            //Close the splash screen
            if (pleaseWaitDialog != null)
            {
                pleaseWaitDialog.dismiss();
                pleaseWaitDialog = null;
            }
        }

        //Notify activity of async task completion
        private void notifyActivityTaskCompleted()
        {
            if (null != activity) {
                String action = input.get(0);
                if (action.equalsIgnoreCase("styles")) {
                    activity.styleListDownloaded(this.results);
                    return;
                }
                if (action.equalsIgnoreCase("brewers")) {
                    activity.brewersListDownloaded(this.results);
                    return;
                }

                if (action.equalsIgnoreCase("brewer") || action.equalsIgnoreCase("style")) {
                    activity.updateRecipeList(this.results);
                    return;
                }
            }
        }

        //for maintain attached the async task to the activity in phone states changes
        //Sets the current activity to the async task
        public void setActivity(DetailFragment activity)
        {
            this.activity = activity;
            if ( completed ) {
                notifyActivityTaskCompleted();
            }
        }

        public ArrayList<Object> loadRecipesByStyle(String style) {

            if (cloudURL == null || cloudURL.equals(""))
                return null;

            ArrayList<BasicRecipe> recipeList = new ArrayList<BasicRecipe>();

            try
            {
                //URL url = new URL(baseURL+"/style/" + style);
                URI rURI = new URI("http", null, cloudURL,
                        80, "/styles/"+style, null, null);
                URL url = rURI.toURL();

                InputStream response = url.openStream();
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
                NodeList childNodes = readXML.getElementsByTagName("recipe");

                Debug.print("Loading recipes from online: " + childNodes.getLength());
                for(int x = 0; x < childNodes.getLength(); x++ ) {
                    Node child = childNodes.item(x);
                    NamedNodeMap childAttr = child.getAttributes();

                    // generate the recipe list
                    long ID = Long.parseLong(childAttr.getNamedItem("id").getNodeValue() );
                    String Brewer = childAttr.getNamedItem("brewer").getNodeValue().toString();
                    String Title = childAttr.getNamedItem("name").getNodeValue().toString();
                    String Style = childAttr.getNamedItem("style").getNodeValue().toString();
                    int iteration = 0;//Integer.parseInt(childAttr.getNamedItem("iteration").getNodeValue());
                    Debug.print("Loading: " + Title);
                    BasicRecipe rRecipe = new BasicRecipe(ID, Brewer, Style, Title, iteration, "style");
                    recipeList.add(rRecipe);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            ArrayList<Object> retList = new ArrayList<Object>();
            for (BasicRecipe o: recipeList) {
                retList.add((Object) o);
            }
            return retList;
        }

        public ArrayList<Object> loadRecipesByBrewer(String brewer) {

            ArrayList<BasicRecipe> recipeList = new ArrayList<BasicRecipe>();
            try
            {
                URI rURI = new URI("http", null, cloudURL, 80, "/brewer/"+brewer, null, null);
                URL url = rURI.toURL();
                InputStream response = url.openStream();
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
                NodeList childNodes = readXML.getElementsByTagName("recipe");

                Debug.print("Loading recipes from online: "+ childNodes.getLength());
                for(int x = 0; x < childNodes.getLength(); x++ ) {
                    Node child = childNodes.item(x);
                    NamedNodeMap childAttr = child.getAttributes();

                    // generate the recipe list
                    long ID = Long.parseLong( childAttr.getNamedItem("id").getNodeValue() );
                    String Brewer = childAttr.getNamedItem("brewer").getNodeValue().toString();
                    String Title = childAttr.getNamedItem("name").getNodeValue().toString();
                    String Style = childAttr.getNamedItem("style").getNodeValue().toString();
                    int iteration = 0;//Integer.parseInt(childAttr.getNamedItem("iteration").getNodeValue());
                    Debug.print("Loading: " + Title);
                    BasicRecipe rRecipe = new BasicRecipe(ID, Brewer, Style, Title, iteration, "brewer");
                    recipeList.add(rRecipe);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            ArrayList<Object> retList = new ArrayList<Object>();
            for (BasicRecipe o: recipeList) {
                retList.add((Object) o);
            }
            return retList;
        }

        public ArrayList<Object> getListOfStyles() {

            ArrayList<String> styleList = new ArrayList<String>();
            styleList.add("");
            try
            {
                URI rURI = new URI("http", null, cloudURL, 80, "/styles/", null, null);
                URL url = rURI.toURL();
                //URL url = new URL(baseURL+"/styles/");
                InputStream response = url.openStream();
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

                dbf.setValidating(false);
                dbf.setIgnoringComments(false);
                dbf.setIgnoringElementContentWhitespace(true);
                dbf.setNamespaceAware(true);
                // dbf.setCoalescing(true);
                // dbf.setExpandEntityReferences(true);

                DocumentBuilder db = null;
                db = dbf.newDocumentBuilder();

                Document readXML = db.parse(response);
                NodeList childNodes = readXML.getElementsByTagName("style");


                Debug.print("Loading Styles from online: "+ childNodes.getLength());

                for(int x = 0; x < childNodes.getLength(); x++ ) {


                    Node child = childNodes.item(x);

                    // generate the style list
                    Debug.print("Style: " + child.getTextContent());
                    if(child.getTextContent() != null) {
                        styleList.add(child.getTextContent());
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            ArrayList<Object> retList = new ArrayList<Object>();
            for (String o: styleList) {
                retList.add((Object) o);
            }
            return retList;
        }

        public ArrayList<Object> getListOfBrewers() {

            ArrayList<String> brewerList = new ArrayList<String>();
            brewerList.add("");
            try
            {
                URI rURI = new URI("http", null, cloudURL, 80, "/brewer/", null, null);
                URL url = rURI.toURL();
                //URL url = new URL(baseURL+"/styles/");
                InputStream response = url.openStream();
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

                dbf.setValidating(false);
                dbf.setIgnoringComments(false);
                dbf.setIgnoringElementContentWhitespace(true);
                dbf.setNamespaceAware(true);
                // dbf.setCoalescing(true);
                // dbf.setExpandEntityReferences(true);

                DocumentBuilder db = null;
                db = dbf.newDocumentBuilder();

                Document readXML = db.parse(response);
                NodeList childNodes = readXML.getElementsByTagName("brewer");


                Debug.print("Loading Styles from online: "+ childNodes.getLength());

                for(int x = 0; x < childNodes.getLength(); x++ ) {


                    Node child = childNodes.item(x);

                    // generate the style list
                    Debug.print("Style: " + child.getTextContent());
                    if(child.getTextContent() != null) {
                        brewerList.add(child.getTextContent());
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            ArrayList<Object> retList = new ArrayList<Object>();
            for (String o: brewerList) {
                retList.add((Object) o);
            }
            return retList;
        }
    }
}
