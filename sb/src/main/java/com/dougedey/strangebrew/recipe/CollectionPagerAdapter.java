package com.dougedey.strangebrew.recipe;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.dougedey.strangebrew.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    private HashMap<Integer, Fragment> registeredFragments = new HashMap<Integer, Fragment>();

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

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public void updateFragments(String position) {

        Fragment f = this.registeredFragments.get(0);
        if (f != null && !position.equals("OVERVIEW")) {
            ((OverviewFragment) f).updateView(null, null);
        }
        f = this.registeredFragments.get(1);
        if (f != null && !position.equals("MALT")) {
            ((MaltFragment) f).updateView(null);
        }
        f = this.registeredFragments.get(2);
        if (f != null && !position.equals("HOPS")) {
            ((HopsFragment) f).updateView(null);
        }
    }
}
