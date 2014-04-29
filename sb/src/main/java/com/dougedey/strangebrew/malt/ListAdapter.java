package com.dougedey.strangebrew.malt;

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
import ca.strangebrew.Fermentable;
import ca.strangebrew.Hop;
import ca.strangebrew.SBStringUtils;

/**
 * Created by doug on 22/04/14.
 */
public class ListAdapter extends ArrayAdapter<Fermentable> {

    private final Context context;
    private final List<Fermentable> values;

    public ListAdapter(Context context, List<Fermentable> values) {
        super(context, R.layout.malt_row, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.malt_row, parent, false);

        Fermentable f = values.get(position);

        TextView textView = (TextView) rowView.findViewById(R.id.name);
        textView.setText((CharSequence) f.getName());

        textView = (TextView) rowView.findViewById(R.id.weight);
        textView.setText(SBStringUtils.format(f.getAmountAs(), 2));

        textView = (TextView) rowView.findViewById(R.id.unit);
        textView.setText(f.getUnitsAbrv());

        textView = (TextView) rowView.findViewById(R.id.lov);
        textView.setText(SBStringUtils.format(f.getLov(), 0));

        textView = (TextView) rowView.findViewById(R.id.percentage);
        textView.setText(SBStringUtils.format(f.getPercent(), 0));

        textView = (TextView) rowView.findViewById(R.id.pppg);
        textView.setText(SBStringUtils.format(f.getPppg(), 3));

        return rowView;
    }
}
