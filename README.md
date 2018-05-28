# README #

### How do I get set up? ###

* If you have trouble compiling, try setting `versionCode = 1` in `app.gradle` line 15

### Summary ###

* Libraries used
    - Volley (https://github.com/google/volley)  
    I used volley for network operations as it is a recommended library, fast and easy to set up.

    - Glide (https://github.com/bumptech/glide)  
    I used Glide to load and display images because it is efficient. It loads images lazily which is good for app performance.
    
    - Gson (https://github.com/google/gson)
    Gson is the best library to consume or create JSON.
    
* Tablet landscape mode is supported with master-detail workflow.
    
* Challenges
    - The biggest challenge was to ensure that the highlighted image in RecyclerView is always the leftmost image.  
      
        Wrote a custom [SnapHelper](https://developer.android.com/reference/android/support/v7/widget/SnapHelper "SnapHelper in Android Docs")
        which by default snaps views to the center, but 
        [my LeftSnapHelper](/app/src/main/java/com/alikazi/codesampleflickr/utils/LeftSnapHelper.kt) 
        ensures that the leftmost child is always shown in full.  

        Finally, some math with `smoothScrollToPosition` in 
        [MainActivity.getDefaultNumberOfVisibleViews()](/app/src/main/java/com/alikazi/codesampleflickr/main/MainActivity.kt)
	and [MainActivity.onPageSelected()](/app/src/main/java/com/alikazi/codesampleflickr/main/MainActivity.kt) handle all
	possible user interactions.
        
    - The second challenge was to survive orientation changes and restore selected positions
        This was fairly easy by saving and restoring instances due to the master-detail workflow approach.

### Who do I talk to? ###

* Repo owner or admin  
Ali Kazi  
[LinkedIn](linkedin.com/in/mdalikazi)  
