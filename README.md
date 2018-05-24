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
    - The biggest challenge was to ensure that the highlighted image is always the leftmost image.  
      
        First, I added a custom [SnapHelper](https://developer.android.com/reference/android/support/v7/widget/SnapHelper "SnapHelper in Android Docs")
        which by default ensures that views snap to center of RecyclerView, but  
        [my LeftSnapHelper](/app/src/main/java/com/alikazi/codesampleflickr/utils/LeftSnapHelper.kt) 
        ensures that the leftmost child is always shown in full.  

        Then I tried few different ways: Get the last visible child with `LayoutManager.getChildAt()`, calculate its width and then scroll RecyclerView using `scrollByX()`
        but it was not always reliable because RecyclerView does not always hold same number of children offscreen if the user scrolls RecyclerView itself.  

        Finally, I settled with a simple `smoothScrollToPosition` and calculated the position with some math which you can see in
        [MainActivity.getDefaultNumberOfVisibleViews()](/app/src/main/java/com/alikazi/codesampleflickr/main/MainActivity.kt)
        
    - The second challenge was to survive orientation changes gracefully and restore positions
        I had already used a fragment for the `ViewPager` to support master-detail workflow but it was fairly easy by saving and restoring instances.

* With more time, I would add 
    - Unit and UI tests
    - MVP or MVVM pattern
    - API such as SightEngine to filter out explicit images or mark them in the list.
    
* Git flow
	- I have used standard git flow method (pull requests, rebasing etc.).

### Who do I talk to? ###

* Repo owner or admin  
Ali Kazi  
[LinkedIn](linkedin.com/in/mdalikazi)  
