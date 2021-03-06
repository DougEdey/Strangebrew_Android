package com.dougedey.strangebrew;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;

import ca.strangebrew.Style;
import ca.strangebrew.Yeast;

/**
 * Created by doug on 18/04/14.
 */
public class YeastListAdapter extends ArrayAdapter<Yeast> {

    private final Context context;
    private final List<Yeast> values;

    public YeastListAdapter(Context context, List<Yeast> values) {
        super(context, R.layout.fragment_recipe_detail, values);
        this.context = context;
        this.values = values;
    }

    public int getCount(){
        return values.size();
    }

    public Yeast getItem(int position){
        return values.get(position);
    }

    public long getItemId(int position){
        return position;
    }

    // And the "magic" goes here
    // This is for the "passive" state of the spinner
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // I created a dynamic TextView here, but you can reference your own  custom layout for each spinner item
        TextView label = new TextView(context);
        label.setTextColor(Color.BLACK);
        // Then you can get the current item using the values array (Users array) and the current position
        // You can NOW reference each method you has created in your bean object (User class)
        label.setText(values.get(position).getName());

        // And finally return your dynamic (or custom) view for each spinner item
        return label;
    }

    // And here is when the "chooser" is popped up
    // Normally is the same view, but you can customize it if you want
    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        TextView label = new TextView(context);
        label.setTextColor(Color.BLACK);
        label.setText(values.get(position).getName());

        return label;
    }
}
