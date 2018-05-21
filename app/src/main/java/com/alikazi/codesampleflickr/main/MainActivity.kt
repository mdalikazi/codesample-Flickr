package com.alikazi.codesampleflickr.main

import android.app.FragmentTransaction
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.alikazi.codesampleflickr.R
import com.alikazi.codesampleflickr.constants.AppConstants
import com.alikazi.codesampleflickr.models.ImageItem
import com.alikazi.codesampleflickr.models.Items
import com.alikazi.codesampleflickr.network.RequestQueueHelper
import com.alikazi.codesampleflickr.network.RequestsProcessor
import com.alikazi.codesampleflickr.utils.CustomAnimationUtils
import com.alikazi.codesampleflickr.utils.DLog
import com.android.volley.VolleyError
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.toolbar.*

class MainActivity : AppCompatActivity(),
        CustomAnimationUtils.ToolbarAnimationListener,
        RequestsProcessor.RequestResponseListener,
        RecyclerAdapter.RecyclerItemClickListener {

    companion object {

        private const val LOG_TAG = AppConstants.LOG_TAG_MAIN

        private const val SAVE_INSTANCE_KEY_FEED = "SAVE_INSTANCE_KEY_FEED"
    }

    private var mIsTabletMode = false
    private var mRecyclerAdapter: RecyclerAdapter? = null
    private var mRequestsProcessor: RequestsProcessor? = null
    private var mListItems: ArrayList<ImageItem>? = ArrayList()

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
        initUi()

        mRequestsProcessor = RequestsProcessor(this, this)
        if (savedInstanceState == null) {
            DLog.i(LOG_TAG,"savedInstanceState == null")
            // Start from scratch
            CustomAnimationUtils.animateToolbar(this, toolbar, this)
        } else {
            mListItems = savedInstanceState.getParcelableArrayList(SAVE_INSTANCE_KEY_FEED)
            handleOrientationChange()
        }
    }

    private fun initUi() {
        setSupportActionBar(toolbar)
        main_swipe_refresh_layout.setOnRefreshListener { makeRequest() }
//        if (property_detail_container != null) {
//            mIsTabletMode = true
//        }
        mRecyclerAdapter = RecyclerAdapter(this, this)
        main_recycler_view.adapter = mRecyclerAdapter
        showHideEmptyListMessage(true)
    }

    private fun handleOrientationChange() {
        DLog.i(LOG_TAG, "handleOrientationChange")
        val layoutParams = toolbar.layoutParams
        layoutParams?.height = CustomAnimationUtils.getDefaultActionBarHeightInPixels(this).toInt()
        main_swipe_refresh_layout.isRefreshing = false
        mRecyclerAdapter?.setListItems(mListItems)
        showHideEmptyListMessage(false)
    }

    override fun onRecyclerItemClick(image: ImageItem?) {
        DLog.i(LOG_TAG, "onRecyclerItemClick")
        DLog.i(LOG_TAG, "item?.title " + image?.title)

        val fragment = DetailsFragment().apply {
            arguments = Bundle().apply {
                putParcelable(DetailsFragment.INTENT_EXTRA_RECYCLER_ITEM, image)
            }
        }
        supportFragmentManager
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.detail_view_container, fragment)
                .commit()
    }

    override fun onToolbarAnimationEnd() {
        DLog.i(LOG_TAG, "onToolbarAnimationEnd")
        recycler_view_empty_text_view.text = getString(R.string.feed_empty_list_message)
        makeRequest()
    }

    private fun makeRequest() {
        if (mRequestsProcessor != null) {
            mRequestsProcessor?.getProperties()
            main_swipe_refresh_layout.isRefreshing = true
            recycler_view_empty_text_view.setText(R.string.feed_empty_list_message)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        DLog.i(LOG_TAG, "onSaveInstanceState")
        outState?.putParcelableArrayList(SAVE_INSTANCE_KEY_FEED, mListItems)
    }

    override fun responseOk(items: Items) {
        DLog.i(LOG_TAG, "responseOk")
        mListItems = items.items
        mRecyclerAdapter?.setListItems(mListItems)
        toolbar.title = items.title
        main_swipe_refresh_layout.isRefreshing = false
        showHideEmptyListMessage(false)
    }

    override fun responseError(error: VolleyError) {
        DLog.i(LOG_TAG, "responseError: " + error.toString())
        recycler_view_empty_text_view.setText(R.string.feed_empty_list_error_message)
        val snackbarMessage = if (isNetworkConnected)
            getString(R.string.snackbar_feed_load_error)
        else
            getString(R.string.snackbar_network_error_message)
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

    override fun onStop() {
        super.onStop()
        RequestQueueHelper.getInstance(this).cancelAllRequests()
    }
}
