package com.alikazi.codesample_flickr.main

import android.app.FragmentTransaction
import android.content.Context
import android.graphics.Rect
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SnapHelper
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.alikazi.codesample_flickr.BuildConfig
import com.alikazi.codesample_flickr.R
import com.alikazi.codesample_flickr.constants.AppConstants
import com.alikazi.codesample_flickr.models.ImageItem
import com.alikazi.codesample_flickr.models.Items
import com.alikazi.codesample_flickr.network.RequestQueueHelper
import com.alikazi.codesample_flickr.network.RequestsProcessor
import com.alikazi.codesample_flickr.utils.CustomAnimationUtils
import com.alikazi.codesample_flickr.utils.DLog
import com.alikazi.codesample_flickr.utils.LeftSnapHelper
import com.android.volley.VolleyError
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.crashes.Crashes
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.toolbar.*

/**
 * Created by kazi_ on 22-May-18.
 */
class MainActivity : AppCompatActivity(),
        CustomAnimationUtils.ToolbarAnimationListener,
        RequestsProcessor.RequestResponseListener,
        DetailsFragment.OnViewPagerImageChangeListener {

    companion object {
        private const val LOG_TAG = AppConstants.LOG_TAG_MAIN
        private const val SAVE_INSTANCE_KEY_FEED = "SAVE_INSTANCE_KEY_FEED"
        private const val SAVE_INSTANCE_KEY_SCROLL_POSITION = "SAVE_INSTANCE_KEY_SCROLL_POSITION"
    }

    private var mDefaultChildCount = 0
    private var mPreviouslySelectedPosition = 0
    private var mListItems: ArrayList<ImageItem>? = ArrayList()
    private var mRecyclerAdapter: RecyclerAdapter = RecyclerAdapter(this)
    private var mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    private var mRequestsProcessor: RequestsProcessor = RequestsProcessor(this, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DLog.i(LOG_TAG, "onCreate")
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)
        startAppCenter()
        initUi()

        if (savedInstanceState == null) {
            DLog.i(LOG_TAG,"savedInstanceState == null")
            // Fresh launch
            instantiateFragment()
            CustomAnimationUtils.animateToolbar(this, toolbar, this)
        } else {
            // Orientation changed
            mListItems = savedInstanceState.getParcelableArrayList(SAVE_INSTANCE_KEY_FEED)
            mPreviouslySelectedPosition = savedInstanceState.getInt(SAVE_INSTANCE_KEY_SCROLL_POSITION)
            handleOrientationChange()
        }
    }

    private fun startAppCenter() {
        if (!BuildConfig.DEBUG) {
            AppCenter.start(application, AppConstants.APP_CENTER_SECRET, Crashes::class.java)
        }
    }

    private fun initUi() {
        setSupportActionBar(toolbar)
        setupRecyclerView()
        showHideEmptyListMessage(true)
    }

    private fun setupRecyclerView() {
        main_recycler_view.layoutManager = mLayoutManager
        main_recycler_view.adapter = mRecyclerAdapter
        val snapHelper: SnapHelper = LeftSnapHelper()
        snapHelper.attachToRecyclerView(main_recycler_view)
        main_recycler_view.setItemViewCacheSize(0)
        main_recycler_view.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                mRecyclerAdapter.setItemsClickable(newState == RecyclerView.SCROLL_STATE_IDLE)
            }
        })
    }

    private fun resetSomeValuesToDefault(userClickedRefresh: Boolean) {
        if (userClickedRefresh) {
            mPreviouslySelectedPosition = 0
            main_recycler_view.smoothScrollToPosition(0)
        }
        if (tablet_layout != null) {
            mLayoutManager.orientation = LinearLayoutManager.VERTICAL
        }
        instantiateFragment()
        mDefaultChildCount = 0
        mRecyclerAdapter.setSelectedPositionFromViewPager(mPreviouslySelectedPosition)
        mRecyclerAdapter.setListItems(mListItems)
        showHideEmptyListMessage(false)
    }

    private fun instantiateFragment() {
        val fragment = DetailsFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(DetailsFragment.INTENT_EXTRA_PAGER_ITEMS, mListItems)
                putInt(DetailsFragment.INTENT_EXTRA_PREVIOUSLY_SELECTED_POSITION, mPreviouslySelectedPosition)
            }
         }

        supportFragmentManager
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.detail_view_container, fragment)
                .commit()

        mRecyclerAdapter.setRecyclerItemClickListener(fragment)
        fragment.setImageChangeListener(this)
    }

    private fun handleOrientationChange() {
        DLog.i(LOG_TAG, "handleOrientationChange")
        // Toolbar should not re-animate on orientation change
        val layoutParams = toolbar.layoutParams
        layoutParams?.height = CustomAnimationUtils.getDefaultActionBarHeightInPixels(this).toInt()
        resetSomeValuesToDefault(false)
    }

    override fun onToolbarAnimationEnd() {
        DLog.i(LOG_TAG, "onToolbarAnimationEnd")
        toolbar.title = getString(R.string.main_title)
        recycler_view_empty_text_view.text = getString(R.string.feed_empty_list_message)
        makeRequest()
    }

    private fun makeRequest() {
        if (checkNetwork()) {
            mRequestsProcessor.getFeed()
        } else {
            showSnackbar()
        }
        recycler_view_empty_text_view.setText(R.string.feed_empty_list_message)
    }

    override fun responseOk(items: Items) {
        DLog.i(LOG_TAG, "responseOk")
        mListItems = items.images
        resetSomeValuesToDefault(true)
    }

    override fun responseError(error: VolleyError) {
        DLog.i(LOG_TAG, "responseError: " + error.toString())
        recycler_view_empty_text_view.setText(R.string.feed_empty_list_error_message)
        showSnackbar()
        showHideEmptyListMessage(true)
    }

    private fun showSnackbar() {
        val snackbarMessage = when(checkNetwork()) {
            true -> getString(R.string.snackbar_feed_load_error)
            false -> getString(R.string.snackbar_network_error_message)
        }

        Snackbar.make(main_recycler_view, snackbarMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.refresh, { makeRequest() })
                .show()
    }

    private fun showHideEmptyListMessage(showMessage: Boolean) {
        recycler_view_empty_text_view.visibility = if (showMessage) View.VISIBLE else View.GONE
        main_recycler_view.visibility = if (showMessage) View.GONE else View.VISIBLE
    }

    override fun onPageSelected(position: Int, diff: Int, comingFromRecyclerItemClick: Boolean) {
        if (mDefaultChildCount == 0) {
            getDefaultNumberOfVisibleViews()
        }

        /**
         * Conditions:
         * A: Scroll right on RecyclerView then click recycler item
         * B: Scroll left on RecyclerView then click recycler item
         * C: Dont scroll then click recycler item
         * D: Scroll right on RecyclerView then swipe ViewPager right
         * E: Scroll right then swipe ViewPager left
         * F: Scroll left then swipe ViewPager right
         * G: Scroll left then swipe ViewPager left
         */
        var scrollToPosition = 0
        if (comingFromRecyclerItemClick) {
            // A, B, C
            scrollToPosition = position + mDefaultChildCount
            DLog.d(LOG_TAG, "Conditions A, B, C")
        } else if (Math.abs(diff) == 1) {
            for (index in 0 until mLayoutManager.childCount) {
                // Find last visible child that is not null
                val child: View? = mLayoutManager.getChildAt(index)
                if (position >= child?.tag.toString().toInt()) {
                    scrollToPosition = position + mDefaultChildCount
                    DLog.d(LOG_TAG, "Conditions F, G")
                    break
                } else {
                    scrollToPosition = position
                    DLog.d(LOG_TAG, "Conditions D, E")
                }
            }
        }

        main_recycler_view.smoothScrollToPosition(scrollToPosition)
        DLog.d(LOG_TAG, "scrollToPosition: $scrollToPosition")
        mRecyclerAdapter.setItemsClickable(main_recycler_view.scrollState == RecyclerView.SCROLL_STATE_IDLE)
        mRecyclerAdapter.setSelectedPositionFromViewPager(position)
        mPreviouslySelectedPosition = position
    }

    /**
     * Used by [onPageSelected]
     * This gives how many items of recycler view are visible to the user
     * in this particular device and orientation.
     * We calculate this only once to prevent extra calculation during scroll
     * and also because the methods used are not reliable for use during scroll
     * due to how RecyclerView recycles views
     * Make sure to reset mDefaultChildCount when orientation changes
     */
    private fun getDefaultNumberOfVisibleViews() {
        mDefaultChildCount = mLayoutManager.findLastCompletelyVisibleItemPosition() -
                mLayoutManager.findFirstCompletelyVisibleItemPosition()

        for (index in 0 until mLayoutManager.childCount) {
            // Find last visible child that is not null
            val lastChild: View? = mLayoutManager.getChildAt(index)
            if (index == mDefaultChildCount + 1 && lastChild != null) {
                // Get the visible portion of the last visible child
                val rect = Rect()
                lastChild.getGlobalVisibleRect(rect)

                // If last child is more than 50% visible then it is a good idea to scroll 1 child extra
                // to make sure that the left-est child in recycler view is the highlighted one
                val shouldIncludeLastChild =
                        rect.width() >= mLayoutManager.getDecoratedMeasuredWidth(lastChild) / 2
                if (shouldIncludeLastChild) {
                    DLog.i(LOG_TAG, "shouldIncludeLastChild: $shouldIncludeLastChild")
                    ++mDefaultChildCount
                }
            }
        }

        DLog.w(LOG_TAG, "mDefaultChildCount: $mDefaultChildCount")
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        DLog.i(LOG_TAG, "onSaveInstanceState")
        outState?.putParcelableArrayList(SAVE_INSTANCE_KEY_FEED, mListItems)
        outState?.putInt(SAVE_INSTANCE_KEY_SCROLL_POSITION, mPreviouslySelectedPosition)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = MenuInflater(this)
        menuInflater.inflate(R.menu.menu_refresh, menu)
        val max: Int = when(menu?.size()) {
            null -> 0
            else -> menu.size() - 1
        }
        // Android bug: Menu action does not show even though
        // set to showAsAction="always" in xml
        for (index: Int in 0..max) {
            val menuItem = menu?.getItem(index)
            if (menuItem != null && menuItem.itemId == R.id.action_refresh) {
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when(item?.itemId){
        R.id.action_refresh -> {
            makeRequest()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun checkNetwork(): Boolean {
        DLog.i(LOG_TAG, "checkNetwork")
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isAvailable && networkInfo.isConnectedOrConnecting
    }

    override fun onStop() {
        super.onStop()
        RequestQueueHelper.getInstance(this).cancelAllRequests()
    }
}
