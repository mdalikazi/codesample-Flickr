package com.alikazi.codesample_flickr.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.alikazi.codesample_flickr.R
import com.alikazi.codesample_flickr.constants.AppConstants
import com.alikazi.codesample_flickr.models.ImageItem
import com.alikazi.codesample_flickr.utils.DLog
import kotlinx.android.synthetic.main.content_detail.view.*

/**
 * Created by kazi_ on 22-May-18.
 */
class DetailsFragment : Fragment(),
        RecyclerAdapter.RecyclerItemClickListener {

    companion object {
        private const val LOG_TAG = AppConstants.LOG_TAG_MAIN
        const val INTENT_EXTRA_PAGER_ITEMS = "INTENT_EXTRA_PAGER_ITEMS"
        const val INTENT_EXTRA_PREVIOUSLY_SELECTED_POSITION = "INTENT_EXTRA_PREVIOUSLY_SELECTED_POSITION"
    }

    fun setImageChangeListener(listener: OnViewPagerImageChangeListener) {
        mImageChangeListener = listener
    }

    private var mSelectedPosition = 0
    private var mDiff = 0
    private var mComingFromRecyclerItemClick = false
    private var mViewPager: ViewPager? = null
    private var mImages: ArrayList<ImageItem>? = ArrayList()
    private var mImageChangeListener: OnViewPagerImageChangeListener? = null
    private var mEmptyTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DLog.i(LOG_TAG, "onCreate")
        arguments?.let {
            if (it.containsKey(INTENT_EXTRA_PAGER_ITEMS)) {
                mImages?.clear()
                mImages = it.getParcelableArrayList(INTENT_EXTRA_PAGER_ITEMS)
                DLog.d(LOG_TAG, "mImages != null. size: ${mImages?.size}")
            }
            if (it.containsKey(INTENT_EXTRA_PREVIOUSLY_SELECTED_POSITION)) {
                mSelectedPosition = it.getInt(INTENT_EXTRA_PREVIOUSLY_SELECTED_POSITION)
                DLog.d(LOG_TAG, "mSelectedPosition: $mSelectedPosition")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        DLog.i(LOG_TAG, "onCreateView")
        val view = inflater.inflate(R.layout.content_detail, container, false)
        mEmptyTextView = view.view_pager_empty_text_view
        mViewPager = view.view_pager
        setupViewPager()
        return view
    }

    private fun setupViewPager() {
        DLog.i(LOG_TAG, "setupViewPager")
        if (mImages != null && !mImages!!.isEmpty()) {
            mEmptyTextView?.visibility = View.GONE
        }
        mViewPager?.adapter = ImagePagerAdapter(activity!!, mImages)
        if (mSelectedPosition != 0) {
            mViewPager?.setCurrentItem(mSelectedPosition, true)
        }
        mViewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                DLog.i(LOG_TAG, "onPageSelected: $position")
                mDiff = mSelectedPosition - position
                mSelectedPosition = position
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    // If mDiff == 0 user started scrolling but did not finish -> dont do anything
                    if (mDiff != 0) {
                        mImageChangeListener?.onPageSelected(mSelectedPosition, mDiff, mComingFromRecyclerItemClick)
                        // Reset mDiff in case user does not fully scroll the page next time
                        // in which case mDiff would carry the previous value and wrongly trigger onPageSelected
                        mDiff = 0
                        mComingFromRecyclerItemClick = false
                    }
                }
            }
        })
    }

    override fun onRecyclerItemClick(position: Int) {
        DLog.i(LOG_TAG, "onRecyclerItemClick")
        mViewPager?.setCurrentItem(position, true)
        mComingFromRecyclerItemClick = true
    }

    interface OnViewPagerImageChangeListener {
        fun onPageSelected(position: Int, diff: Int, comingFromRecyclerItemClick: Boolean)
    }
}
