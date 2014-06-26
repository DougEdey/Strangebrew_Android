package com.dougedey.strangebrew.recipe;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.dougedey.strangebrew.R;
import com.dougedey.strangebrew.StyleListAdapter;
import com.dougedey.strangebrew.YeastListAdapter;

import java.text.DecimalFormat;
import java.util.List;

import ca.strangebrew.Database;
import ca.strangebrew.Quantity;
import ca.strangebrew.Recipe;

/**
 * Created by doug on 24/06/14.
 */
public class OverviewFragment extends Fragment {

    private Recipe RECIPE = null;

    public boolean autoEdit = false;

    private OgChanged ogChanged = new OgChanged();
    private FgChanged fgChanged = new FgChanged();
    private PreBoilChanged preBoilChanged = new PreBoilChanged();
    private PostBoilChanged postBoilChanged = new PostBoilChanged();
    private VolUnitListener volUnitListener = new VolUnitListener();
    private EffChanged effChanged = new EffChanged();
    private AttChanged attChanged = new AttChanged();
    private StyleChangedListener styleChangedListener = new StyleChangedListener();
    private YeastChangedListener yeastChangedListener = new YeastChangedListener();


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.overview_layout, container, false);
        Bundle args = getArguments();
        try {
            RECIPE = (Recipe) args.get("RECIPE_OBJ");
        } catch (NullPointerException npe) {
            RECIPE = this.getRecipe();
        }

        rootView = updateView(rootView, "");

        Spinner yeastSpin = (Spinner) rootView.findViewById(R.id.yeast_spinner);
        if (yeastSpin.getOnItemSelectedListener() != yeastChangedListener)
            yeastSpin.setOnItemSelectedListener(yeastChangedListener);

        Spinner styleSpin = (Spinner) rootView.findViewById(R.id.style_spinner);
        if (yeastSpin.getOnItemSelectedListener() != styleChangedListener)
            yeastSpin.setOnItemSelectedListener(styleChangedListener);

        Spinner prebSpin = (Spinner) rootView.findViewById(R.id.preb_vol_spinner);
        if (prebSpin.getOnItemSelectedListener() != volUnitListener)
            prebSpin.setOnItemSelectedListener(volUnitListener);


        // Volumes
        ArrayAdapter<String> volAdapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_expandable_list_item_1);
        List<String> s = Quantity.getListofUnits("volume", true);
        for (String x : s) {
            volAdapter.add(x);
        }
        //volAdapter.addAll(s);
        Spinner volSpin = (Spinner) rootView.findViewById(R.id.preb_vol_spinner);
        volSpin.setAdapter(volAdapter);
        int pos = volAdapter.getPosition(Quantity.getVolAbrv(RECIPE.getVolUnits()));
        volSpin.setSelection(pos);

        return rootView;
    }

    public View updateView(View rootView, String position) {

        if (rootView == null) {
            rootView = getView();
        }
        if (RECIPE == null) {
            return rootView;
        }

        autoEdit = true;
        DecimalFormat df = new DecimalFormat("#.##");
        // Setup the style Dropdown
        Spinner styleSpin = (Spinner) rootView.findViewById(R.id.style_spinner);

        StyleListAdapter styleAdapter =
                new StyleListAdapter(getActivity(), Database.getInstance().styleDB);

        styleSpin.setAdapter(styleAdapter);
        styleSpin.setSelection(styleAdapter.getPosition(RECIPE.getStyleObj()));

        // Setup the Yeast Drop Down
        Spinner yeastSpin = (Spinner) rootView.findViewById(R.id.yeast_spinner);

        YeastListAdapter yeastAdapter =
                new YeastListAdapter(getActivity(), Database.getInstance().yeastDB);

        yeastSpin.setAdapter(yeastAdapter);
        yeastSpin.setSelection(yeastAdapter.getPosition(RECIPE.getYeastObj()));

        // Original Gravity
        EditText og = (EditText) rootView.findViewById(R.id.og_picker);
        DecimalFormat three_df = new DecimalFormat("#.###");
        String tDouble = three_df.format(RECIPE.getEstOg());

        if (!position.equalsIgnoreCase("og")) {
            if (!tDouble.equals(og.getText())) {
                og.setText(tDouble);
            }
            og.addTextChangedListener(ogChanged);
        }

        if (!position.equalsIgnoreCase("fg")) {
            og = (EditText) rootView.findViewById(R.id.fg_picker);
            tDouble = three_df.format(RECIPE.getEstFg());

            if (!tDouble.equals(og.getText())) {
                og.setText(tDouble);
            }
        }

        og.addTextChangedListener(fgChanged);

        og = (EditText) rootView.findViewById(R.id.eff_picker);
        tDouble = df.format(RECIPE.getEfficiency()) + "%";
        if (!tDouble.equals(og.getText()))
            og.setText(tDouble);
        og.addTextChangedListener(effChanged);

        og = (EditText) rootView.findViewById(R.id.att_picker);
        tDouble = df.format(RECIPE.getAttenuation()) + "%";
        if (!tDouble.equals(og.getText()))
            og.setText(tDouble  );
        og.addTextChangedListener(attChanged);

        og = (EditText) rootView.findViewById(R.id.preb_picker);
        tDouble = df.format(RECIPE.getPreBoilVol());
        if (!tDouble.equals(og.getText()))
            og.setText(tDouble);
        og.addTextChangedListener(preBoilChanged);

        og = (EditText) rootView.findViewById(R.id.postb_picker);
        tDouble = df.format(RECIPE.getFinalWortVol());
        if (!tDouble.equals(og.getText()))
            og.setText(tDouble);
        og.addTextChangedListener(postBoilChanged);

        synchronized (rootView) {
            rootView.notify();
        }
        autoEdit = false;
        return rootView;
    }

    private class OgChanged implements TextWatcher {
        public void afterTextChanged(Editable s) {
            if (autoEdit) return;
            try {
                RECIPE.setEstOg(Double.parseDouble(s.toString()));
            } catch (NumberFormatException nfe) {
                return;
            }
            updateView(null, "og");
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
        public void onTextChanged(CharSequence s, int start, int before, int count){}
    }

    private class FgChanged implements TextWatcher {
        public void afterTextChanged(Editable s) {
            if (autoEdit) return;
            try {
                RECIPE.setEstFg(Double.parseDouble(s.toString()));
            } catch (NumberFormatException nfe) {
                return;
            }
            updateView(null, "fg");
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
        public void onTextChanged(CharSequence s, int start, int before, int count){}
    }

    private class PreBoilChanged implements TextWatcher {
        public void afterTextChanged(Editable s) {
            if (autoEdit) return;
            try {
                Quantity temp = new Quantity();
                temp.setUnits(RECIPE.getVolUnits());
                temp.setAmount(Double.parseDouble(s.toString()));

                if (temp.getUnits().equals(RECIPE.getVolUnits())
                        && temp.getValue() == RECIPE.getPreBoilVol()) {
                    return;
                }
                RECIPE.setPreBoil(temp);
            } catch (NumberFormatException nfe) {
                return;
            }
            updateView(null, "preb");
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
        public void onTextChanged(CharSequence s, int start, int before, int count){}
    }

    private class PostBoilChanged implements TextWatcher {
        public void afterTextChanged(Editable s) {
            if (autoEdit) return;
            try {
                Quantity temp = new Quantity();
                temp.setUnits(RECIPE.getVolUnits());
                temp.setAmount(Double.parseDouble(s.toString()));
                Quantity rTemp = RECIPE.getPostBoilVol();
                if (temp.getUnits().equals(RECIPE.getVolUnits()) && temp.getValue() == rTemp.getValue()) {
                    return;
                }
                RECIPE.setPostBoil(temp);
            } catch (NumberFormatException nfe) {
                return;
            }
            updateView(null, "postb");
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
        public void onTextChanged(CharSequence s, int start, int before, int count){}
    }

    private class EffChanged implements TextWatcher {
        public void afterTextChanged(Editable s) {
            if (autoEdit) return;
            try {
                String temp = s.toString();

                // Remove the Percent
                if (temp.indexOf('%') > -1 ) {
                    temp = temp.substring(0, temp.indexOf('%'));
                }

                temp.trim();
                RECIPE.setEfficiency(Double.parseDouble(temp));
            } catch (NumberFormatException nfe) {
                return;
            }
            updateView(null, "eff");
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
        public void onTextChanged(CharSequence s, int start, int before, int count){}
    }

    private class AttChanged implements TextWatcher {
        public void afterTextChanged(Editable s) {
            if (autoEdit) return;
            try {
                String temp = s.toString();

                // Remove the Percent
                if (temp.indexOf('%') > -1 ) {
                    temp = temp.substring(0, temp.indexOf('%'));
                }

                temp.trim();
                RECIPE.setAttenuation(Double.parseDouble(temp));
            } catch (NumberFormatException nfe) {
                return;
            }
            updateView(null, "att");
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
        public void onTextChanged(CharSequence s, int start, int before, int count){}
    }

    // Change the weight unit in the drop down
    private class VolUnitListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            return;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View v, int position, long id) {
            if (v == null || autoEdit)
                return;
            View view = v.getRootView();

            Spinner spinner = (Spinner) view.findViewById(R.id.preb_vol_spinner);

            String temp = spinner.getItemAtPosition(position).toString();
            if (!temp.equals(RECIPE.getVolUnits())) {
                RECIPE.setVolUnits(temp);
                updateView(null, "wu");
            }

            return;
        }
    }

    // Change the weight unit in the drop down
    private class YeastChangedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            return;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View v, int position, long id) {
            if (v == null || autoEdit)
                return;

            View view = v.getRootView();

            Spinner spinner = (Spinner) view.findViewById(R.id.yeast_spinner);

            String temp = spinner.getItemAtPosition(position).toString();
            if (!temp.equals(RECIPE.getYeast())) {
                RECIPE.setYeast(temp);
                updateView(null, "yeast");
            }

            return;
        }
    }

    // Change the weight unit in the drop down
    private class StyleChangedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            return;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View v, int position, long id) {
            if (v == null || autoEdit)
                return;
            View view = v.getRootView();

            Spinner spinner = (Spinner) view.findViewById(R.id.style_spinner);

            String temp = spinner.getItemAtPosition(position).toString();
            if (!temp.equals(RECIPE.getStyle())) {
                RECIPE.setStyle(temp);
                updateView(null, "style");
            }
            return;
        }
    }

    public Recipe getRecipe() {
        DetailActivity dActivity = (DetailActivity) this.getActivity();
        return dActivity.getRecipe();
    }
}
