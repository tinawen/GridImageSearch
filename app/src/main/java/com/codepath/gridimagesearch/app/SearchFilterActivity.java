package com.codepath.gridimagesearch.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;


public class SearchFilterActivity extends ActionBarActivity {

    Spinner spImageSize;
    Spinner spColorFilter;
    Spinner spImageType;
    EditText etSiteFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_filter);
        ImageFiltering imageFiltering = (ImageFiltering) getIntent().getSerializableExtra("imageFiltering");
        setupViews(imageFiltering);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_filter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setupViews(ImageFiltering imageFiltering) {
        spImageSize = setUpSpinner(R.id.spImageSize, R.array.image_size_array, imageFiltering != null ? imageFiltering.getImageSize() : null);
        spColorFilter = setUpSpinner(R.id.spColorFilter, R.array.color_filter_array, imageFiltering != null ? imageFiltering.getColorFilter() : null);
        spImageType = setUpSpinner(R.id.spImageType, R.array.image_type_array, imageFiltering != null ? imageFiltering.getImageType() : null);
        etSiteFilter = (EditText) findViewById(R.id.etSiteFilter);
        if (imageFiltering != null) {
            etSiteFilter.setText(imageFiltering.getSiteFilter());
        }
    }

    public Spinner setUpSpinner(int spinnerResId, int textArrayResId, String defaultValue) {
        Spinner spinner = (Spinner) findViewById(spinnerResId);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                textArrayResId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (defaultValue != null && !defaultValue.isEmpty()) {
            int selection = adapter.getPosition(defaultValue);
            spinner.setSelection(selection);
        }
        return spinner;
    }

    public void onSaveFilters(View v) {
        Intent data = new Intent();
        ImageFiltering imageFiltering = new ImageFiltering(
                                            spImageSize.getSelectedItem().toString(),
                                            spColorFilter.getSelectedItem().toString(),
                                            spImageType.getSelectedItem().toString(),
                                            etSiteFilter.getText().toString());
        data.putExtra("newImageFiltering", imageFiltering);
        setResult(RESULT_OK, data);
        finish();
    }

}
