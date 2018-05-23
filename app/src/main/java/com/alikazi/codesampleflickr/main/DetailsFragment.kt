package com.alikazi.codesampleflickr.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.alikazi.codesampleflickr.R
import com.alikazi.codesampleflickr.constants.AppConstants
import com.alikazi.codesampleflickr.models.ImageItem
import com.alikazi.codesampleflickr.utils.DLog
import kotlinx.android.synthetic.main.content_detail.view.*

/**
 * Created by kazi_ on 22-May-18.
 */
class DetailsFragment : Fragment(),
        RecyclerAdapter.RecyclerItemClickListener {

    companion object {
        private const val LOG_TAG = AppConstants.LOG_TAG_MAIN
        private const val SAVE_INSTANCE_KEY_PAGER_ITEMS = "SAVE_INSTANCE_KEY_PAGER_ITEMS"
    }

    fun setImageChangeListener(listener: OnViewPagerImageChangeListener) {
        mImageChangeListener = listener
    }

    private var mSelectedPosition = 0
    private var mDiff = 0
    private var mComingFromRecyclerItemClick = false
    private var mViewPager: ViewPager? = null
    private var mEmptyTextView: TextView? = null
    private var mImages: ArrayList<ImageItem>? = ArrayList()
    private var mImageChangeListener: OnViewPagerImageChangeListener? = null

    fun setPagerItems(images: ArrayList<ImageItem>?) {
        DLog.i(LOG_TAG, "setPagerItems")
        mImages?.clear()
        mImages = images
        if (images != null && !images.isEmpty()) {
            mEmptyTextView?.visibility = View.GONE
            mViewPager?.adapter = ImagePagerAdapter(activity!!, mImages)
        }
    }

    /*override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(SAVE_INSTANCE_KEY_PAGER_ITEMS, mImages)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        mImages = savedInstanceState?.getParcelableArrayList(SAVE_INSTANCE_KEY_PAGER_ITEMS)
        mViewPager?.adapter = ImagePagerAdapter(activity!!, mImages)
    }*/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        DLog.i(LOG_TAG, "onCreateView")
        val view = inflater.inflate(R.layout.content_detail, container, false)
        mViewPager = view.view_pager
        mEmptyTextView = view.view_pager_empty_text_view
        setupViewPager()
        return view
    }

    private fun setupViewPager() {
        DLog.i(LOG_TAG, "setupViewPager")
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
