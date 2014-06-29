package com.dougedey.strangebrew.recipe;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dougedey.strangebrew.R;

import ca.strangebrew.Fermentable;
import ca.strangebrew.Recipe;

/**
 * Created by doug on 24/06/14.
 */
public class MaltFragment extends Fragment {

    public Recipe RECIPE = null;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.malt_layout, container, false);
        Bundle args = getArguments();
        try {
            RECIPE = (Recipe) args.get("RECIPE_OBJ");
        } catch (NullPointerException npe) {
            RECIPE = this.getRecipe();
        }

        // Build up the malt tables
        com.dougedey.strangebrew.malt.ListFragment maltList =
                new com.dougedey.strangebrew.malt.ListFragment();
        getFragmentManager().beginTransaction().add(R.id.malt_table, maltList, "malt_list").commit();

        maltList.setMaltFragment(this);
        com.dougedey.strangebrew.malt.Content.setResources(getResources());
        com.dougedey.strangebrew.malt.Content.clear();

        for (int i = 0; i < RECIPE.getMaltListSize(); i++) {
            Fermentable f = RECIPE.getFermentable(i);
            com.dougedey.strangebrew.malt.Content.addMalt(f);
        }

        return rootView;
    }

    public View updateView(View rootView) {


        DetailActivity dActivity = (DetailActivity) this.getActivity();
        if (rootView == null) {
            dActivity.updateView(null, "");
        } else {
            dActivity.updateView(null, "MALT");
        }
        return rootView;
    }

    public Recipe getRecipe() {
        DetailActivity dActivity = (DetailActivity) this.getActivity();
        return dActivity.getRecipe();
    }
}