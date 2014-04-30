package com.dougedey.strangebrew.remote;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dougedey.strangebrew.R;

import java.util.List;

import ca.strangebrew.Debug;
import ca.strangebrew.Fermentable;
import ca.strangebrew.SBStringUtils;

/**
 * Created by doug on 22/04/14.
 */
public class ListAdapter extends ArrayAdapter<BasicRecipe> {

    private final Context context;
    private final List<BasicRecipe> values;

    public ListAdapter(Context context, List<BasicRecipe> values) {
        super(context, R.layout.malt_row, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.remote_row, parent, false);

        BasicRecipe r = values.get(position);

        TextView textView = (TextView) rowView.findViewById(R.id.c_r_name);
        textView.setText((CharSequence) r.title);

        TextView styleView = (TextView) rowView.findViewById(R.id.c_r_style);
        TextView brewerView = (TextView) rowView.findViewById(R.id.c_r_brewer);
        if (r.search.equalsIgnoreCase("style")) {
            styleView.setVisibility(View.GONE);
            brewerView.setVisibility(View.VISIBLE);
        } else {
            styleView.setVisibility(View.VISIBLE);
            brewerView.setVisibility(View.GONE);
        }
        Debug.print("Style " + r.style + " Brewer " + r.brewer);
        styleView.setText(r.style);
        brewerView.setText(r.brewer);

        return rowView;
    }
}
