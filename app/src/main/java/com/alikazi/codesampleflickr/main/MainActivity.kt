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
import com.alikazi.codesampleflickr.utils.CustomViewUtils
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
    }

    private var mMeasuredWithPx = 0
    private var mDefaultChildCount = 0
    private var mListItems: ArrayList<ImageItem>? = ArrayList()
    private var mDetailsFragment: DetailsFragment = DetailsFragment()
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
        supportFragmentManager
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(R.id.detail_view_container, mDetailsFragment)
                .commit()

        mRecyclerAdapter.setRecyclerItemClickListener(mDetailsFragment)
        mDetailsFragment.setImageChangeListener(this)
    }

    private fun handleOrientationChange() {
        DLog.i(LOG_TAG, "handleOrientationChange")
        // Toolbar should not re-animate on orientation change
        val layoutParams = toolbar.layoutParams
        layoutParams?.height = CustomAnimationUtils.getDefaultActionBarHeightInPixels(this).toInt()
        mDetailsFragment = DetailsFragment()
        instantiateFragment()
        mRecyclerAdapter.setListItems(mListItems)
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
    }

    override fun responseOk(items: Items) {
        DLog.i(LOG_TAG, "responseOk")
        mListItems = items.images
        mRecyclerAdapter.setListItems(mListItems)
        mDetailsFragment.setPagerItems(mListItems)
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

    override fun onPageSelected(position: Int) {
        if (mMeasuredWithPx <= 0) {
            calculateScrollByXForOneChild()
        }
        if (mDefaultChildCount == 0) {
            getDefaultNumberOfVisibleViews()
        }

        val scrollToPosition = position + mDefaultChildCount
        DLog.d(LOG_TAG, "scrollToPosition: $scrollToPosition")
        // Prevent IndexOutOfBoundsException
        if (scrollToPosition < mLayoutManager.itemCount) {
            main_recycler_view.smoothScrollToPosition(scrollToPosition)
        }
        mRecyclerAdapter.setItemsClickable(main_recycler_view.scrollState == RecyclerView.SCROLL_STATE_IDLE)
        mRecyclerAdapter.setSelectedPositionFromViewPager(position)
    }

    /**
     * This gives how many items of recycler view are visible to the user.
     * We calculate this only once to prevent extra calculation and the methods used
     * are not reliable due to the nature of how RecyclerView recycles views
     */
    private fun getDefaultNumberOfVisibleViews() {
        mDefaultChildCount = mLayoutManager.findLastCompletelyVisibleItemPosition() - mLayoutManager.findFirstCompletelyVisibleItemPosition()
        DLog.d(LOG_TAG, "mDefaultChildCount + $mDefaultChildCount")
    }

    /**
     * Taking measuredWidth of only the first child is enough
     * because in our case all children are the same size
     */
    private fun calculateScrollByXForOneChild() {
        val measuredWidth = main_recycler_view.getChildAt(0).measuredWidth
        mMeasuredWithPx = CustomViewUtils.getComplexUnitPx(this, measuredWidth.toFloat()).toInt()
    }

    override fun onStop() {
        super.onStop()
        RequestQueueHelper.getInstance(this).cancelAllRequests()
    }
}
