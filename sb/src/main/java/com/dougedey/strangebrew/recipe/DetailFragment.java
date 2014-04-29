package com.dougedey.strangebrew.recipe;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.dougedey.strangebrew.R;
import com.dougedey.strangebrew.StyleListAdapter;
import com.dougedey.strangebrew.YeastListAdapter    ;
import com.dougedey.strangebrew.malt.ListFragment;

import java.text.DecimalFormat;
import java.util.List;

import ca.strangebrew.Database;
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
public class DetailFragment extends Fragment {
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
        View rootView = inflater.inflate(R.layout.fragment_recipe_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null && !mItem.file.equals("")) {
            ImportXml importXml = new ImportXml(mItem.file);
            if (importXml.handler != null) {
                recipe = importXml.handler.getRecipe();
                recipe.calcFermentTotals();
                recipe.calcHopsTotals();
                recipe.calcMaltTotals();
                recipe.calcPrimeSugar();
            }
            ((TextView) rootView.findViewById(R.id.title_text)).setText(recipe.getName());
        } else if (mItem.file.equals("")) {
            // Popup the remote recipe dialogue
            View popupView = inflater.inflate(R.layout.cloud_recipes, null);
            final PopupWindow popupWindow = new PopupWindow(
                    popupView);
            popupWindow.
        }

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
}
