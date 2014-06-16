package com.codepath.gridimagesearch.app;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


public class SearchFilterDialog extends DialogFragment implements View.OnClickListener {
    Spinner spImageSize;
    Spinner spColorFilter;
    Spinner spImageType;
    EditText etSiteFilter;
    Button btnSave;

    public SearchFilterDialog() {
        // Empty constructor required for DialogFragment
    }

    public interface SearchFilterDialogListener {
        void onFinishEditingSearchFilterDialog(ImageFiltering imageFiltering);
    }

    public static SearchFilterDialog newInstance(ImageFiltering imageFiltering) {
        SearchFilterDialog frag = new SearchFilterDialog();
        if (imageFiltering != null) {
            Bundle args = new Bundle();
            args.putSerializable("imageFiltering", imageFiltering);
            frag.setArguments(args);
        }
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_filter, container);
        Bundle args = getArguments();
        ImageFiltering imageFiltering = null;
        if (args != null) {
            imageFiltering = (ImageFiltering) getArguments().getSerializable("imageFiltering");
        }
        setupViewsInView(view, imageFiltering);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onClick (View v) {
        if (v == btnSave) {
            ImageFiltering imageFiltering = new ImageFiltering(
                    spImageSize.getSelectedItem().toString(),
                    spColorFilter.getSelectedItem().toString(),
                    spImageType.getSelectedItem().toString(),
                    etSiteFilter.getText().toString());
            SearchFilterDialogListener listener = (SearchFilterDialogListener) getActivity();
            listener.onFinishEditingSearchFilterDialog(imageFiltering);
            dismiss();
        }
    }

    public void setupViewsInView(View view, ImageFiltering imageFiltering) {
        spImageSize = setUpSpinnerInView(view, R.id.spImageSize, R.array.image_size_array, imageFiltering != null ? imageFiltering.getImageSize() : null);
        spColorFilter = setUpSpinnerInView(view, R.id.spColorFilter, R.array.color_filter_array, imageFiltering != null ? imageFiltering.getColorFilter() : null);
        spImageType = setUpSpinnerInView(view, R.id.spImageType, R.array.image_type_array, imageFiltering != null ? imageFiltering.getImageType() : null);
        etSiteFilter = (EditText) view.findViewById(R.id.etSiteFilter);
        btnSave = (Button) view.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);
        if (imageFiltering != null) {
            etSiteFilter.setText(imageFiltering.getSiteFilter());
        }
    }

    public Spinner setUpSpinnerInView(View view, int spinnerResId, int textArrayResId, String defaultValue) {
        Spinner spinner = (Spinner) view.findViewById(spinnerResId);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                textArrayResId, R.layout.spinner_dropdown_align_right_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (defaultValue != null && !defaultValue.isEmpty()) {
            int selection = adapter.getPosition(defaultValue);
            spinner.setSelection(selection);
        }
        return spinner;
    }
}
