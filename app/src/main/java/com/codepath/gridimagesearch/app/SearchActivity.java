package com.codepath.gridimagesearch.app;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class SearchActivity extends ActionBarActivity implements SearchFilterDialog.SearchFilterDialogListener {
    // grid view
    GridView gvResults;
    ImageResultArrayAdapter imageAdapter;
    ArrayList<ImageResult> imageResults = new ArrayList<ImageResult>();

    // states
    ImageFiltering imageFiltering;
    Boolean hasReachedPageLimit;
    String queryText;

    @Override
    public void onFinishEditingSearchFilterDialog(ImageFiltering imageFiltering) {
        this.imageFiltering = imageFiltering;
        performNewSearch();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setupGridView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem searchItem = menu.findItem(R.id.miSearch);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                queryText = s;
                performNewSearch();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.miSettings) {
            showSearchFilterDialog(imageFiltering);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupGridView() {
        gvResults = (GridView) findViewById(R.id.gvResults);
        gvResults.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to AdapterView
                customLoadMoreDataFromApi(totalItemsCount);
            }
        });
        imageAdapter = new ImageResultArrayAdapter(this, imageResults);
        gvResults.setAdapter(imageAdapter);

        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // display image full screen
                Intent intent = new Intent(getApplicationContext(), ImageDisplayActivity.class);
                ImageResult imageResult = imageResults.get(i);
                intent.putExtra("result", imageResult);
                startActivity(intent);
            }
        });
    }

    private void showSearchFilterDialog(ImageFiltering imageFiltering) {
        android.app.FragmentManager fm = this.getFragmentManager();
        SearchFilterDialog searchFilterDialog = SearchFilterDialog.newInstance(imageFiltering);
        searchFilterDialog.show(fm, "fragment_search_filter");
    }

    private void performNewSearch() {
        imageAdapter.clear();
        hasReachedPageLimit = false;
        performSearch(0);
    }

    private String apiQueryStringForOffset(int offset) {
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
        apiString += "&v=1.0&q=" + Uri.encode(queryText);
        return apiString;
    }

    private  void performSearch(final int offset) {
        if (hasReachedPageLimit) {
            Log.d("DEBUG", "IMAGE_SEARCH: has reached page limit, not searching");
            return;
        }

        AsyncHttpClient client = new AsyncHttpClient();
        String apiString = apiQueryStringForOffset(offset);

        // check connectivity
        if (!isConnectedToNetWork()) {
            Toast.makeText(this, R.string.no_wifi, Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("DEBUG", "IMAGE_SEARCH: sending http request for " + apiString);
        client.get(apiString,
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        Log.d("DEBUG", "IMAGE_SEARCH: response on success");
                        JSONArray imageJsonResults;
                        try {
                            if (response.isNull("responseData")) {
                                hasReachedPageLimit = true;
                                Log.d("DEBUG", "IMAGE_SEARCH: has reached page limit");
                                return;
                            }
                            imageJsonResults = response.getJSONObject(
                                    "responseData").getJSONArray("results");
                            // check if is first page
                            JSONObject cursor = response.getJSONObject("responseData").getJSONObject("cursor");
                            int currentPageIndex = cursor.getInt("currentPageIndex");
                            JSONArray pages = cursor.getJSONArray("pages");

                            // reset if first page
                            if (currentPageIndex == 0) {
                                imageAdapter.clear();
                            }
                            // always append
                            imageAdapter.addAll(ImageResult.fromJSONArray(imageJsonResults));
                            if (!hasReachedPageLimit) {
                                if (pages.length() == currentPageIndex + 1) {
                                    hasReachedPageLimit = true;
                                    Log.d("DEBUG", "IMAGE_SEARCH: very small number of results. Has reached limit");
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
                        Toast.makeText(getApplicationContext(), R.string.network_request_error, Toast.LENGTH_SHORT).show();
                        // retry
                        performSearch(offset);
                    }
                });
    }

    // Append more data into the adapter
    public void customLoadMoreDataFromApi(int offset) {
        performSearch(offset);
    }

    private boolean isConnectedToNetWork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
