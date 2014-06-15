package com.codepath.gridimagesearch.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class SearchActivity extends ActionBarActivity {
    EditText etQuery;
    GridView gvResults;
    Button btnSearch;
    ArrayList<ImageResult> imageResults = new ArrayList<ImageResult>();
    ImageResultArrayAdapter imageAdapter;
    ImageFiltering imageFiltering;
    Boolean hasReachedPageLimit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setupViews();
        // the deprecated google image search API displays a maximum of 64 pictures
        gvResults.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                customLoadMoreDataFromApi(totalItemsCount);
            }
        });
        imageAdapter = new ImageResultArrayAdapter(this, imageResults);
        gvResults.setAdapter(imageAdapter);

        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), ImageDisplayActivity.class);
                ImageResult imageResult = imageResults.get(i);
                intent.putExtra("result", imageResult);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    private final int REQUEST_CODE = 20;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.miSettings) {
            Intent intent = new Intent(this, SearchFilterActivity.class);
            if (imageFiltering != null) {
                intent.putExtra("imageFiltering", imageFiltering);
            }
            startActivityForResult(intent, REQUEST_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            imageFiltering = (ImageFiltering)data.getSerializableExtra("newImageFiltering");
            performNewSearch();
        }
    }

    public void setupViews() {
        etQuery = (EditText) findViewById(R.id.etQuery);
        gvResults = (GridView) findViewById(R.id.gvResults);
        btnSearch = (Button) findViewById(R.id.btnSearch);
    }

    public void onImageSearch(View v) {
        performNewSearch();
    }

    public void performNewSearch() {
        imageAdapter.clear();
        hasReachedPageLimit = false;
        performSearch(0);
    }

    private  void performSearch(final int offset) {
        if (hasReachedPageLimit) {
            Log.d("DEBUG", "IMAGE_SEARCH: has reached page limit, not searching");
            return;
        }
        String query = etQuery.getText().toString();
        AsyncHttpClient client = new AsyncHttpClient();
        // http://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=android
        String apiString = "https://ajax.googleapis.com/ajax/services/search/images?rsz=8&" +
                "start=" + offset;
        if (imageFiltering != null) {
            String imageSizePreference = imageFiltering.getImageSize();
            if (imageSizePreference != null && !imageSizePreference.isEmpty()) {
                String searchParam;
                // translating to query param input
                if (imageSizePreference == "small") {
                    searchParam = "icon";
                } else if (imageSizePreference == "medium") {
                    searchParam = "small";
                } else if (imageSizePreference == "large") {
                    searchParam = "xxlarge";
                } else if (imageSizePreference == "xlarge") {
                    searchParam = "huge";
                }

                apiString += "&imgsz=" + imageSizePreference;
            }
            String colorFilterPreference = imageFiltering.getColorFilter();
            if (colorFilterPreference != null && !colorFilterPreference.isEmpty()) {
                apiString += "&imgcolor=" + colorFilterPreference;
            }
            String imageTypePreference = imageFiltering.getImageType();
            if (imageTypePreference != null && !imageTypePreference.isEmpty()) {
                apiString += "&imgtype=" + imageTypePreference;
            }
            String siteFilterPreference = imageFiltering.getSiteFilter();
            if (siteFilterPreference != null && !siteFilterPreference.isEmpty()) {
                apiString += "&as_sitesearch=" + siteFilterPreference;
            }
        }
        apiString += "&v=1.0&q=" + Uri.encode(query);
        Log.d("DEBUG", "IMAGE_SEARCH: sending http request for " + apiString);
        client.get(apiString,
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        Log.d("DEBUG", "IMAGE_SEARCH: heard a response on success");
                        JSONArray imageJsonResults = null;
                        try {
                            if (response.isNull("responseData")) {
                                hasReachedPageLimit = true;
                                Log.d("DEBUG", "IMAGE_SEARCH: has reached page limit!");
                                return;
                            }
                            imageJsonResults = response.getJSONObject(
                                    "responseData").getJSONArray("results");
                            // check if is first page
                            JSONObject cursor = response.getJSONObject("responseData").getJSONObject("cursor");
                            int currentPageIndex = cursor.getInt("currentPageIndex");
                            JSONArray pages = cursor.getJSONArray("pages");

                            Log.d("DEBUG", "IMAGE_SEARCH: http response back" + pages.toString() + " has reached page limit is " + hasReachedPageLimit);
                            Log.d("DEBUG", "IMAGE_SEARCH: current page index is " + currentPageIndex + " results count is " + ImageResult.fromJSONArray(imageJsonResults).size());
                            // reset if first page
                            if (currentPageIndex == 0) {
                                imageAdapter.clear();
                            }
                            // always append
                            imageAdapter.addAll(ImageResult.fromJSONArray(imageJsonResults));
                            Log.d("DEBUG", "IMAGE_SEARCH, hasReachedPageLimit is " + hasReachedPageLimit +
                                    " pages.length is " + pages.length() + " images results array count is " + imageResults.size());
                            if (!hasReachedPageLimit) {
                                if (pages.length() == currentPageIndex + 1) {
                                    hasReachedPageLimit = true;
                                    Log.d("DEBUG", "IMAGE_SEARCH: not much result. has reached limit");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(Throwable e, JSONArray errorResponse) {
                        Log.d("DEBUG", "IMAGE_SEARCH: heard a response on failure");
                        Log.d("ERROR", e.toString());
                        Toast.makeText(getApplicationContext(), "Network request failed", Toast.LENGTH_SHORT).show();
                        // retry
                        performSearch(offset);
                    }
                });
    }
    // Append more data into the adapter
    public void customLoadMoreDataFromApi(int offset) {
        Log.d("DEBUG", "IMAGE_SEARCH: firing search at offset " + offset);
        performSearch(offset);
    }
}
