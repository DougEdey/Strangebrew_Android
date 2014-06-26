package com.dougedey.strangebrew.recipe;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dougedey.strangebrew.R;

import java.util.List;

import ca.strangebrew.BrewCalcs;
import ca.strangebrew.Options;

/**
 * Created by doug on 15/04/14.
 */
public class ListAdapter extends ArrayAdapter<Content.RecipeItem> {
    private final Context context;
    private final List<Content.RecipeItem> values;

    public ListAdapter(Context context, List<Content.RecipeItem> values) {
        super(context, R.layout.rowlayout, values);
        this.context = context;
        this.values = values;
        // Force the creation of the options singleton
        Options.getInstance(this.getContext());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
        int rColor =  BrewCalcs.calcRGB(1, values.get(position).color, 8, 30, 20, 255);
        Log.i("Color", "Recipe color is: " + rColor);
        TextView textView = (TextView) rowView.findViewById(R.id.recipe_name);
        textView.setText((CharSequence) values.get(position).name);

        View colourBox = (View) rowView.findViewById(R.id.recipe_colour);
        colourBox.setBackgroundColor(rColor);
        colourBox.setVisibility(View.VISIBLE);

        return rowView;
    }

}
