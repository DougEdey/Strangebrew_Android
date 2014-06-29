package com.dougedey.strangebrew.malt;

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

import com.dougedey.strangebrew.MaltsListAdapter;
import com.dougedey.strangebrew.R;
import com.dougedey.strangebrew.recipe.MaltFragment;

import java.util.List;

import ca.strangebrew.Database;
import ca.strangebrew.Fermentable;
import ca.strangebrew.Quantity;
import ca.strangebrew.SBStringUtils;


/**
 * A list fragment representing a list of Malts. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link com.dougedey.strangebrew.recipe.DetailFragment}.
 * <p>

 */
public class ListFragment extends android.support.v4.app.ListFragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private boolean autoEdit = false;
    private static ListAdapter listAdapter = null;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;
    private MaltFragment maltFragment = null;
    private PopupWindow popupWindow = null;
    private Fermentable currentMalt = null;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ListFragment() {
    }

    public void setMaltFragment(MaltFragment mFragment) {
        this.maltFragment = mFragment;
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

    private void notifyAdapter() {
        synchronized (this) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        currentMalt = Content.ITEMS.get(position);
        // TODO: Edit a row popup

        LayoutInflater layoutInflater = (LayoutInflater)getActivity().getBaseContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.malt_popup, null);

        ((EditText)popupView.findViewById(R.id.malt_lov)).addTextChangedListener(new MaltLovChanged());
        ((EditText)popupView.findViewById(R.id.malt_weight_picker)).addTextChangedListener(new MaltWeightChanged());
        ((EditText)popupView.findViewById(R.id.malt_pppg)).addTextChangedListener(new MaltPppgChanged());

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
                if (popupWindow == null) {
                    return false;
                }

                int[] position = new int[2];
                View pView = popupWindow.getContentView();
                pView.getLocationOnScreen(position);

                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_OUTSIDE
                        || ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN
                        && (event.getX() > (pView.getRight())
                        || event.getX() < (pView.getLeft())
                        || event.getY() < (pView.getTop())
                        || event.getY() > (pView.getBottom()))
                        )) {
                    maltFragment.getRecipe().calcMaltTotals();
                    maltFragment.getRecipe().calcHopsTotals();
                    maltFragment.updateView(maltFragment.getView());

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
        Spinner maltSpin = (Spinner) popupView.findViewById(R.id.malt_spinner);

        MaltsListAdapter maltAdapter =
                new MaltsListAdapter(getActivity(), Database.getInstance().fermDB);

        maltSpin.setAdapter(maltAdapter);
        maltSpin.setOnItemSelectedListener(new ChangeMaltListener());

        // Volumes
        ArrayAdapter<String> weightAdapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_expandable_list_item_1);
        List<String> s = Quantity.getListofUnits("weight", true);
        for (String x: s) {
            weightAdapter.add(x);
        }
        Spinner weightSpin = (Spinner) popupView.findViewById(R.id.malt_unit_spinner);
        weightSpin.setAdapter(weightAdapter);
        weightSpin.setOnItemSelectedListener(new ChangeUnitListener());

        if (currentMalt.getName().equals("New")) {
            currentMalt = new Fermentable();
            Button button = (Button) popupView.findViewById(R.id.malt_delete_button);
            button.setOnClickListener(null);
            button.setVisibility(View.GONE);

            button = (Button) popupView.findViewById(R.id.malt_ok_button);
            button.setOnClickListener(new AddMaltListener());
            button.setText(R.string.hop_add);

            popupWindow.update();
            return;
        }

        Button button = (Button) popupView.findViewById(R.id.malt_delete_button);
        button.setOnClickListener(new DelMaltListener());

        button = (Button) popupView.findViewById(R.id.malt_ok_button);
        button.setOnClickListener(null);
        button.setVisibility(View.GONE);

        maltSpin.setSelection(maltAdapter.getPosition(currentMalt));

        int pos = weightAdapter.getPosition(Quantity.getVolAbrv(currentMalt.getUnits()));
        weightSpin.setSelection(pos);

        updateMaltInPopup(popupView, position, true);
        popupWindow.update();
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

    // Adding a new Hop
    private class AddMaltListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            View v = view.getRootView();

            maltFragment.getRecipe().addMalt(currentMalt);
            Content.addMalt(currentMalt);
            maltFragment.getRecipe().calcMaltTotals();
            maltFragment.getRecipe().calcHopsTotals();
            maltFragment.updateView(maltFragment.getView());
            notifyAdapter();

            popupWindow.dismiss();
        }
    };

    // Delete a Hop
    private class DelMaltListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            View v = view.getRootView();
            TextView tv = (TextView) v.findViewById(R.id.malt_id);
            int id = Integer.parseInt(tv.getText().toString());

            Fermentable f = Content.ITEMS.remove(id);

            maltFragment.getRecipe().delMalt(id);
            maltFragment.getRecipe().calcMaltTotals();
            maltFragment.getRecipe().calcHopsTotals();
            maltFragment.updateView(maltFragment.getView());

            notifyAdapter();
            popupWindow.dismiss();
        }
    };

    private void updateMaltInPopup(View popupView, int position, boolean updateAll) {
        // Only Set the values if we need to
//        Spinner hopsSpin = (Spinner) popupView.findViewById(R.id.hop_spinner);
//        if (hopsSpin.getSelectedItem().toString().equals(currentHop.getName())) {
//            return;
//        }

        autoEdit = true;
        EditText tv = null;
        if (currentMalt == null) {
            return;
        }

        if (updateAll) {
            tv = (EditText) popupView.findViewById(R.id.malt_weight_picker);
            tv.setText(SBStringUtils.format(currentMalt.getAmountAs(), 2));

            tv = (EditText) popupView.findViewById(R.id.malt_percent);
            tv.setText(SBStringUtils.format(currentMalt.getPercent(), 1));

            tv = (EditText) popupView.findViewById(R.id.malt_pppg);
            tv.setText(SBStringUtils.format(currentMalt.getPppg(), 3));
        }

        maltFragment.getRecipe().calcMaltTotals();
        maltFragment.getRecipe().calcHopsTotals();
        notifyAdapter();

        tv = (EditText) popupView.findViewById(R.id.malt_lov);
        tv.setText(SBStringUtils.format(currentMalt.getLov(), 1));

        TextView textView = (TextView) popupView.findViewById(R.id.overall_og);
        textView.setText(SBStringUtils.format(maltFragment.getRecipe().getEstOg(), 3));

        if (position > -1) {
            textView = (TextView) popupView.findViewById(R.id.malt_id);
            textView.setText(Integer.toString(position));
        }

        popupWindow.update();
        autoEdit = false;
    }

    // Change a hop in the drop down
    private class ChangeMaltListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            return;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View v, int position, long id) {

            View view = v.getRootView();

            Spinner spinner = (Spinner) view.findViewById(R.id.malt_spinner);
            TextView tv = (TextView) view.findViewById(R.id.malt_id);

            int maltID = 0;
            try {
                maltID = Integer.parseInt(tv.getText().toString());
            } catch (NumberFormatException nfe) {
                maltID = -1;
            }

            if (maltID > -1
                    && spinner.getSelectedItem().toString().equals(
                        Content.ITEMS.get(maltID).getName())) {
                return;
            }

            Fermentable f = new Fermentable();
            f.setName(spinner.getSelectedItem().toString());
            int index = Database.getInstance().find(f);

            f = Database.getInstance().fermDB.get(index);

            currentMalt.setName(f.getName());
            currentMalt.setPppg(f.getPppg());
            currentMalt.setLov(f.getLov());
            updateMaltInPopup(view, -1, true);
            popupWindow.update();
            return;
        }
    }



    // Change a hop in the drop down
    private class ChangeUnitListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            return;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View v, int position, long id) {
            View view = v.getRootView();

            Spinner spinner = (Spinner) view.findViewById(R.id.malt_unit_spinner);

            // Convert the value to the new unit
            currentMalt.convertTo(spinner.getSelectedItem().toString());

            if (autoEdit) {
                return;
            }

            updateMaltInPopup(view, -1, true);
            popupWindow.update();
            return;
        }
    }

    private class MaltWeightChanged implements TextWatcher {
        public void afterTextChanged(Editable s) {
            if (autoEdit) return;


            try {
                currentMalt.setAmount(Double.parseDouble(s.toString()));
            } catch (NumberFormatException nfe) {
                return;
            }
            updateMaltInPopup(popupWindow.getContentView(), -1, false);
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
        public void onTextChanged(CharSequence s, int start, int before, int count){}
    }

    private class MaltPercentChanged implements TextWatcher {
        public void afterTextChanged(Editable s) {
            /** TODO: Allow Malt percent changes
            if (autoEdit) return;
            try {
                currentMalt.setAmount(Double.parseDouble(s.toString()));
            } catch (NumberFormatException nfe) {
                return;
            }**/
            updateMaltInPopup(popupWindow.getContentView(), -1, false);
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
        public void onTextChanged(CharSequence s, int start, int before, int count){}
    }


    private class MaltPppgChanged implements TextWatcher {
        public void afterTextChanged(Editable s) {
            if (autoEdit) return;
            try {
                currentMalt.setPppg(Integer.parseInt(s.toString()));

            } catch (NumberFormatException nfe) {
                return;
            }
            updateMaltInPopup(popupWindow.getContentView(), -1, false);
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
        public void onTextChanged(CharSequence s, int start, int before, int count){}
    }

    private class MaltLovChanged implements TextWatcher {
        public void afterTextChanged(Editable s) {
            if (autoEdit) return;
            try {
                currentMalt.setLov(Double.parseDouble(s.toString()));
            } catch (NumberFormatException nfe) {
                return;
            }
            updateMaltInPopup(popupWindow.getContentView(), -1, false);
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
        public void onTextChanged(CharSequence s, int start, int before, int count){}
    }

}
