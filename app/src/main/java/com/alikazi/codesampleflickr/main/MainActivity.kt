package com.alikazi.codesampleflickr.main

import android.app.FragmentTransaction
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SnapHelper
import android.view.View
import com.alikazi.codesampleflickr.BuildConfig
import com.alikazi.codesampleflickr.R
import com.alikazi.codesampleflickr.constants.AppConstants
import com.alikazi.codesampleflickr.models.ImageItem
import com.alikazi.codesampleflickr.models.Items
import com.alikazi.codesampleflickr.network.RequestQueueHelper
import com.alikazi.codesampleflickr.network.RequestsProcessor
import com.alikazi.codesampleflickr.utils.CustomAnimationUtils
import com.alikazi.codesampleflickr.utils.DLog
import com.alikazi.codesampleflickr.utils.LeftSnapHelper
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
    private var mLayoutManager: LinearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    private var mRequestsProcessor: RequestsProcessor = RequestsProcessor(this, this)

    private val isNetworkConnected: Boolean
        get() {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnectedOrConnecting
        }

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

        // TODO REFRESH MENU BUTTON
    }

    private fun startAppCenter() {
        if (!BuildConfig.DEBUG) {
            AppCenter.start(application, AppConstants.APP_CENTER_SECRET, Crashes::class.java)
        }
    }

    private fun initUi() {
        setSupportActionBar(toolbar)
        main_swipe_refresh_layout.setOnRefreshListener { makeRequest() }
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

    private fun instantiateFragment() {
        val fragment = DetailsFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(DetailsFragment.INTENT_EXTRA_PAGER_ITEMS, mListItems)
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
        instantiateFragment()
        mDefaultChildCount = 0
        mRecyclerAdapter.setListItems(mListItems)
        mRecyclerAdapter.setSelectedPositionFromViewPager(mPreviouslySelectedPosition)
        main_swipe_refresh_layout.isRefreshing = false
        showHideEmptyListMessage(false)
    }

    override fun onToolbarAnimationEnd() {
        DLog.i(LOG_TAG, "onToolbarAnimationEnd")
        toolbar.title = getString(R.string.main_title)
        recycler_view_empty_text_view.text = getString(R.string.feed_empty_list_message)
        makeRequest()
    }

    private fun makeRequest() {
        mRequestsProcessor.getProperties()
        main_swipe_refresh_layout.isRefreshing = true
        recycler_view_empty_text_view.setText(R.string.feed_empty_list_message)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        DLog.i(LOG_TAG, "onSaveInstanceState")
        outState?.putParcelableArrayList(SAVE_INSTANCE_KEY_FEED, mListItems)
        outState?.putInt(SAVE_INSTANCE_KEY_SCROLL_POSITION, mPreviouslySelectedPosition)
    }

    override fun responseOk(items: Items) {
        DLog.i(LOG_TAG, "responseOk")
        mListItems = items.images
        mRecyclerAdapter.setListItems(mListItems)
        instantiateFragment()
        main_swipe_refresh_layout.isRefreshing = false
        showHideEmptyListMessage(false)
    }

    override fun responseError(error: VolleyError) {
        DLog.i(LOG_TAG, "responseError: " + error.toString())
        recycler_view_empty_text_view.setText(R.string.feed_empty_list_error_message)
        val snackbarMessage = when(isNetworkConnected) {
            true -> getString(R.string.snackbar_feed_load_error)
            false -> getString(R.string.snackbar_network_error_message)
        }

        Snackbar.make(main_recycler_view, snackbarMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.refresh, { makeRequest() })
                .show()

        showHideEmptyListMessage(true)
        main_swipe_refresh_layout.isRefreshing = false
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
         * If diff < 0 then user scrolled to the right (position increased)
         * If diff > 0 then user scrolled to the left (position decreased)
         *
         * If diff > 0 && comingFromRecyclerItemClick then...
         * ...user scrolled to the left but tapped on a recycler item
         *
         * If diff > 0 && !comingFromRecyclerItemClick then...
         * ...user scrolled to the left but by swiping left on the ViewPager
         */
        if (diff < 0 || (diff > 0 && comingFromRecyclerItemClick)) {
            val scrollToPosition = position + mDefaultChildCount
            DLog.d(LOG_TAG, "scrollToPosition: $scrollToPosition")
            // Prevent IndexOutOfBoundsException
            if (scrollToPosition < mLayoutManager.itemCount) {
                main_recycler_view.smoothScrollToPosition(scrollToPosition)
            }
        } else if (diff > 0 && !comingFromRecyclerItemClick) {
            main_recycler_view.smoothScrollToPosition(position)
        }

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
        DLog.d(LOG_TAG, "mDefaultChildCount + $mDefaultChildCount")
    }

    override fun onStop() {
        super.onStop()
        RequestQueueHelper.getInstance(this).cancelAllRequests()
    }
}
