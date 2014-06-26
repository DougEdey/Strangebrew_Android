package com.dougedey.strangebrew.hops;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.dougedey.strangebrew.HopsListAdapter;
import com.dougedey.strangebrew.R;
import com.dougedey.strangebrew.recipe.HopsFragment;

import java.util.List;

import ca.strangebrew.Database;
import ca.strangebrew.Debug;
import ca.strangebrew.Hop;
import ca.strangebrew.Quantity;
import ca.strangebrew.SBStringUtils;


/**
 * A list fragment representing a list of Hops. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link com.dougedey.strangebrew.recipe.DetailFragment}.
 * <p>

 */
public class ListFragment extends android.support.v4.app.ListFragment {

    private boolean autoEdit = false;
    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";


    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */

    private Hop currentHop = null;
    private Hop originalHop = null;

    private static ListAdapter listAdapter = null;
    HopsFragment hopsFragment = null;

    public ListFragment() {
    }

    public void setHopsFragment(HopsFragment hFragment) {
        this.hopsFragment = hFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Content.setResources(getResources());
        listAdapter = new ListAdapter(
                getActivity(),
                Content.ITEMS);

        setListAdapter(listAdapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    PopupWindow popupWindow = null;

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        currentHop = Content.ITEMS.get(position);

        if (popupWindow != null) {
            this.hopsFragment.getRecipe().calcHopsTotals();
            //this.hopsFragment.updateView(null);
            notifyAdapter();
            popupWindow.dismiss();
        }

        // TODO: Edit a row popup
        LayoutInflater layoutInflater = (LayoutInflater)getActivity().getBaseContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.hop_popup, null);

        ((EditText)popupView.findViewById(R.id.hop_time_picker)).addTextChangedListener(new HopTimeChanged());
        ((EditText)popupView.findViewById(R.id.hop_weight_picker)).addTextChangedListener(new HopWeightChanged());
        //((EditText)popupView.findViewById(R.id.hop_unit_spinner)).addTextChangedListener(new HopWeightChanged());
        ((EditText)popupView.findViewById(R.id.hop_alpha_picker)).addTextChangedListener(new HopAlphaChanged());
        popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int[] position = new int[2];
                View pView = popupWindow.getContentView();
                pView.getLocationOnScreen(position);

                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_OUTSIDE
                        || ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN
                            && (event.getX() > (pView.getRight())
                            || event.getX() < (pView.getLeft())
                            || event.getY() < (pView.getTop())
                            || event.getY() > (pView.getBottom()))
                        )
                    ) {

                    hopsFragment.getRecipe().calcHopsTotals();
                    hopsFragment.updateView(hopsFragment.getView());

                    notifyAdapter();
                    popupWindow.dismiss();
                    popupWindow = null;
                    return true;
                }
                return false;
            }
        });
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);


        // Setup the popup
        // Available hops
        Spinner hopsSpin = (Spinner) popupView.findViewById(R.id.hop_spinner);

        HopsListAdapter hopsAdapter =
                new HopsListAdapter(getActivity(), Database.getInstance().hopsDB);

        hopsSpin.setAdapter(hopsAdapter);
        hopsSpin.setOnItemSelectedListener(new ChangeHopListener());
        //hopsSpin.setOnTouchListener(new changeHopListener());


        // Volumes
        ArrayAdapter<String> weightAdapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_expandable_list_item_1);
        List<String> s = Quantity.getListofUnits("weight", true);

        for (String x: s) {
            weightAdapter.add(x);
        }
        Spinner weightSpin = (Spinner) popupView.findViewById(R.id.hop_unit_spinner);
        weightSpin.setAdapter(weightAdapter);
        weightSpin.setOnItemSelectedListener(new ChangeUnitListener());

        if (currentHop.getName().equals("New")) {
            currentHop = new Hop();

            Button button = (Button) popupView.findViewById(R.id.hop_delete_button);
            button.setOnClickListener(null);
            button.setVisibility(View.GONE);

            button = (Button) popupView.findViewById(R.id.hop_ok_button);
            button.setOnClickListener(new AddHopListener());
            button.setText(R.string.hop_add);

            popupWindow.update();
            return;
        }


        Button button = (Button) popupView.findViewById(R.id.hop_delete_button);
        button.setOnClickListener(new DelHopListener());

        button = (Button) popupView.findViewById(R.id.hop_ok_button);
        button.setOnClickListener(null);
        button.setVisibility(View.GONE);

        hopsSpin.setSelection(hopsAdapter.getPosition(currentHop));
        int pos = weightAdapter.getPosition(Quantity.getVolAbrv(currentHop.getUnits()));
        weightSpin.setSelection(pos);

        updateHopInPopup(popupView, position, true);
        popupWindow.update();
    }

    private void updateHopInPopup(View popupView, int position, boolean updateAll) {
        // Only Set the values if we need to
        autoEdit = true;
        EditText tv = null;
        if (currentHop == null) {
            return;
        }

        if (updateAll) {
            tv = (EditText) popupView.findViewById(R.id.hop_time_picker);
            tv.setText(Integer.toString(currentHop.getMinutes()));

            tv = (EditText) popupView.findViewById(R.id.hop_weight_picker);
            tv.setText(SBStringUtils.format(currentHop.getAmountAs(), 2));

            // if these have changed the IBU has.
            tv = (EditText) popupView.findViewById(R.id.hop_alpha_picker);
            tv.setText(SBStringUtils.format(currentHop.getAlpha(), 1));
        }

        hopsFragment.getRecipe().calcHopsTotals();
        notifyAdapter();

        TextView textView = (TextView) popupView.findViewById(R.id.current_ibu);
        textView.setText(SBStringUtils.format(currentHop.getIBU(), 2));

        if (position > -1) {
            textView = (TextView) popupView.findViewById(R.id.hop_id);
            textView.setText(Integer.toString(position));
        }

        autoEdit = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    private void notifyAdapter() {
        synchronized (this) {
            listAdapter.notifyDataSetChanged();
        }
    }

    // Adding a new Hop
    private class AddHopListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            View v = view.getRootView();
            Hop h = new Hop();

            try {
                Spinner spinner = (Spinner) v.findViewById(R.id.hop_spinner);
                h.setName(spinner.getSelectedItem().toString());

                EditText tv = (EditText) v.findViewById(R.id.hop_weight_picker);
                spinner = (Spinner) v.findViewById(R.id.hop_unit_spinner);
                h.setAmountAs(tv.getText().toString(), spinner.getSelectedItem().toString());

                tv = (EditText) v.findViewById(R.id.hop_time_picker);
                h.setMinutes(Integer.parseInt(tv.getText().toString()));

                tv = (EditText) v.findViewById(R.id.hop_alpha_picker);
                h.setAlpha(Double.parseDouble(tv.getText().toString()));
            } catch (NumberFormatException e) {
                // Couldn't parse a value or something, exit
                popupWindow.dismiss();
            }

            hopsFragment.getRecipe().addHop(h);
            Content.addHop(h);
            hopsFragment.getRecipe().calcHopsTotals();
            hopsFragment.updateView(hopsFragment.getView());

            notifyAdapter();

            popupWindow.dismiss();
        }
    };

    // Delete a Hop
    private class DelHopListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            View v = view.getRootView();
            TextView tv = (TextView) v.findViewById(R.id.hop_id);
            int id = Integer.parseInt(tv.getText().toString());

            Hop h = Content.ITEMS.remove(id);
            hopsFragment.getRecipe().delHop(id);
            hopsFragment.getRecipe().calcHopsTotals();
            hopsFragment.updateView(hopsFragment.getView());
            notifyAdapter();
            popupWindow.dismiss();
        }
    };

    // Update a Hop
    private class UpdateHopListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            View v = view.getRootView();
            TextView tv = (TextView) v.findViewById(R.id.hop_id);
            int id = Integer.parseInt(tv.getText().toString());

            //Content.ITEMS.remove(id);
            //Content.ITEMS.add(id, currentHop);

            /*
            Pretty sure this isn't needed for now
            */

            Spinner spinner = (Spinner) v.findViewById(R.id.hop_spinner);
            currentHop.setName(spinner.getSelectedItem().toString());

            EditText et = (EditText) v.findViewById(R.id.hop_weight_picker);
            spinner = (Spinner) v.findViewById(R.id.hop_unit_spinner);
            currentHop.setAmountAs(et.getText().toString(), spinner.getSelectedItem().toString());

            et = (EditText) v.findViewById(R.id.hop_time_picker);
            currentHop.setMinutes(Integer.parseInt(et.getText().toString()));

            et = (EditText) v.findViewById(R.id.hop_alpha_picker);
            currentHop.setAlpha(Double.parseDouble(et.getText().toString()));


            hopsFragment.getRecipe().calcHopsTotals();
            hopsFragment.updateView(null);

            notifyAdapter();
            popupWindow.dismiss();
        }
    };

    // Change a hop in the drop down
    private class ChangeHopListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            return;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View v, int position, long id) {

            View view = v.getRootView();

            Spinner spinner = (Spinner) view.findViewById(R.id.hop_spinner);
            TextView tv = (TextView) view.findViewById(R.id.hop_id);

            int hopID = 0;
            try {
                hopID = Integer.parseInt(tv.getText().toString());
            } catch (NumberFormatException nfe) {
                hopID = -1;
            }

            if (hopID > -1
                    && spinner.getSelectedItem().toString().equals(
                        Content.ITEMS.get(hopID).getName())) {
                return;
            }

            Hop h = new Hop();
            h.setName(spinner.getSelectedItem().toString());
            int index = Database.getInstance().find(h);

            h = Database.getInstance().hopsDB.get(index);

            currentHop.setName(h.getName());
            currentHop.setAlpha(h.getAlpha());

            // if these have changed the IBU has.
            tv = (EditText) v.getRootView().findViewById(R.id.hop_alpha_picker);
            tv.setText(SBStringUtils.format(currentHop.getAlpha(), 1));

            updateHopInPopup(view, -1, false);
            popupWindow.update();
            return;
        }
    }

    // Change a Weight in the drop down
    private class ChangeUnitListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            return;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View v, int position, long id) {

            View view = v.getRootView();

            Spinner spinner = (Spinner) view.findViewById(R.id.hop_unit_spinner);
            TextView tv = (TextView) view.findViewById(R.id.hop_id);

            int hopID = 0;
            try {
                hopID = Integer.parseInt(tv.getText().toString());
            } catch (NumberFormatException nfe) {
                hopID = -1;
            }

            if (hopID > -1
                    && spinner.getSelectedItem().toString().equals(
                    Content.ITEMS.get(hopID).getName())) {
                return;
            }

            currentHop.convertTo(spinner.getSelectedItem().toString());

            updateHopInPopup(view, -1, true);
            popupWindow.update();
            return;
        }
    }

    private class HopWeightChanged implements TextWatcher {
        public void afterTextChanged(Editable s) {
            if (autoEdit) return;
            try {
                currentHop.setAmount(Double.parseDouble(s.toString()));
            } catch (NumberFormatException nfe) {
                return;
            }
            updateHopInPopup(popupWindow.getContentView(), -1, false);
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
        public void onTextChanged(CharSequence s, int start, int before, int count){}
    }

    private class HopTimeChanged implements TextWatcher {
        public void afterTextChanged(Editable s) {
            if (autoEdit) return;
            try {
                currentHop.setMinutes(Integer.parseInt(s.toString()));

            } catch (NumberFormatException nfe) {
                return;
            }
            updateHopInPopup(popupWindow.getContentView(), -1, false);
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
        public void onTextChanged(CharSequence s, int start, int before, int count){}
    }

    private class HopAlphaChanged implements TextWatcher {
        public void afterTextChanged(Editable s) {
            if (autoEdit) return;
            try {
                currentHop.setAlpha(Double.parseDouble(s.toString()));
            } catch (NumberFormatException nfe) {
                return;
            }
            updateHopInPopup(popupWindow.getContentView(), -1, false);
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
        public void onTextChanged(CharSequence s, int start, int before, int count){}
    }
}
