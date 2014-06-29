package com.dougedey.strangebrew.recipe;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dougedey.strangebrew.R;

import ca.strangebrew.Hop;
import ca.strangebrew.Recipe;

/**
 * Created by doug on 24/06/14.
 */
public class HopsFragment extends Fragment {

    public Recipe RECIPE = null;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.hops_layout, container, false);
        Bundle args = getArguments();
        try {
            RECIPE = (Recipe) args.get("RECIPE_OBJ");
        } catch (NullPointerException npe) {
            RECIPE = this.getRecipe();
        }

        // Build up the Hop Table
        com.dougedey.strangebrew.hops.ListFragment hopsList =
                new com.dougedey.strangebrew.hops.ListFragment();
        getFragmentManager().beginTransaction().add(R.id.hops_table, hopsList, "hops_list").commit();

        hopsList.setHopsFragment(this);

        com.dougedey.strangebrew.hops.Content.setResources(getResources());
        com.dougedey.strangebrew.hops.Content.clear();

        for (int i = 0; i < RECIPE.getHopsListSize(); i++) {
            Hop h = RECIPE.getHop(i);
            com.dougedey.strangebrew.hops.Content.addHop(h);
        }

        return rootView;
    }

    public View updateView(View rootView) {

        DetailActivity dActivity = (DetailActivity) this.getActivity();

        if (rootView == null) {
            dActivity.updateView(null, "");
        } else {
            dActivity.updateView(null, "HOPS");
        }

        return rootView;
    }

    public Recipe getRecipe() {
        DetailActivity dActivity = (DetailActivity) this.getActivity();
        return dActivity.getRecipe();
    }

}