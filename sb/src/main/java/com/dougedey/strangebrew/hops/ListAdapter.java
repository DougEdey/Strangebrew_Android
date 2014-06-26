package com.dougedey.strangebrew.hops;

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
import ca.strangebrew.Hop;
import ca.strangebrew.SBStringUtils;

/**
 * Created by doug on 22/04/14.
 */
public class ListAdapter extends ArrayAdapter<Hop> {

    private final Context context;
    private final List<Hop> values;

    public ListAdapter(Context context, List<Hop> values) {
        super(context, R.layout.hop_row, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.hop_row, parent, false);

        Hop h = values.get(position);

        TextView textView = (TextView) rowView.findViewById(R.id.name);
        textView.setText((CharSequence) h.getName());

        if (h.getName().equals(this.context.getString(R.string.new_string))) {
            return rowView;
        }

        textView = (TextView) rowView.findViewById(R.id.weight);
        textView.setText(SBStringUtils.format(h.getAmountAs(), 2));

        textView = (TextView) rowView.findViewById(R.id.unit);
        textView.setText(h.getUnitsAbrv());

        textView = (TextView) rowView.findViewById(R.id.ibu);
        textView.setText(SBStringUtils.format(h.getIBU(), 2));

        textView = (TextView) rowView.findViewById(R.id.addition);
        textView.setText(Integer.toString(h.getMinutes()));

        return rowView;
    }

}
