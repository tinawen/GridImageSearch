# Google image search app

This is an Android demo application for using the Google image search API. Users can type in search teams in the action bar and optionally add filters in the settings dialog. Results are asynchronously loaded and displayed in the grid view. A full resolution image is lazily fetched and displayed after an image in the grid view is tapped. Users can then zoom in and out of the picture and share with their friends.

Time spent: 20 hours spent in total

Completed user stories:

 * [x] Required: User can enter a search query that will display a grid of image results from the Google Image API
 * [x] Required: User can click on "settings" which allows selection of advanced search options to filter results
 * [x] Required: User can configure advanced search filters
 * [x] Required: Subsequent searches will have any filters applied to the search results
 * [x] Required: User can tap on any image in results to see the image full-screen
 * [x] Required: User can scroll down “infinitely” to continue loading more image results (up to 8 pages)
 * [x] Advanced: Robust error handling, check if internet is available, handle error cases, network failures
 * [x] Advanced: Use the ActionBar SearchView or custom layout as the query box instead of an EditText
 * [x] Advanced: User can share an image to their friends or email it to themselves
 * [x] Advanced: Replace Filter Settings Activity with a lightweight modal overlay
 * [x] Advanced: Improve the user interface and experiment with image assets and/or styling and coloring
 * [x] Bonus: User can zoom or pan images displayed in full-screen detail view
 
Notes:

* I used SmartImageView for thumbnails (when displaying images on grid view) and Picasso for full-res images. Picasso plays nicely with TouchImageView. 
* I spent some time optimizing cell reuse to improve performance. I made sure to nil-out bitmaps when needed.
* I added a hint to educate users to pinch to zoom when full res image is displayed. This hint only fires the very first time

Walkthrough of all user stories:

![Video Walkthrough](anim_grid_image_search.gif)
