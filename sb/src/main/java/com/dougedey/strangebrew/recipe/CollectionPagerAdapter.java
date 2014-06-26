package com.dougedey.strangebrew.recipe;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.dougedey.strangebrew.R;

import ca.strangebrew.Recipe;

/**
 * Created by doug on 24/06/14.
 */
public class CollectionPagerAdapter extends FragmentPagerAdapter {

    private Recipe RECIPE = null;

    private Resources resources;

    public CollectionPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.resources = context.getResources();
    }

    public void setRecipe(Recipe r) {
        this.RECIPE = r;
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = null;
        if (i == 0) {
            fragment = new OverviewFragment();
        }

        if (i == 1) {
            fragment = new MaltFragment();
        }

        if (i == 2) {
            fragment = new HopsFragment();
        }

        if (fragment == null) {
            return null;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return resources.getString(R.string.title_overview);
            case 1:
                return resources.getString(R.string.title_malts);
            case 2:
                return resources.getString(R.string.title_hops);
            default:
                return "UNKNOWN " + position;
        }
    }
}
