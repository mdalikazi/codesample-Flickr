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
    }

    fun setImageChangeListener(listener: OnViewPagerImageChangeListener) {
        mImageChangeListener = listener
    }

    private var mSelectedPosition = 0
    private var mDiff = 0
    private var mViewPager: ViewPager? = null
    private var mEmptyTextView: TextView? = null
    private var mImages: ArrayList<ImageItem>? = ArrayList()
    private var mImageChangeListener: OnViewPagerImageChangeListener? = null

    fun setPagerItems(images: ArrayList<ImageItem>?) {
        mImages = images
        if (images != null && !images.isEmpty()) {
            mEmptyTextView?.visibility = View.GONE
        }
        setupViewPager()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        DLog.i(LOG_TAG, "onCreateView")
        val view = inflater.inflate(R.layout.content_detail, container, false)
        mViewPager = view.view_pager
        mEmptyTextView = view.view_pager_empty_text_view
        return view
    }

    private fun setupViewPager() {
        DLog.i(LOG_TAG, "setupViewPager")
        mViewPager?.offscreenPageLimit = 0
        mViewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                DLog.i(LOG_TAG, "onPageSelected: $position")
                mDiff = mSelectedPosition - position
                DLog.i(LOG_TAG, "mDiff: $mDiff")
                mSelectedPosition = position
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    // If mDiff < 0 then user scrolled to the right (position increased)
                    // If mDiff > 0 then user scrolled to the left (position decreased)
                    // If mDiff == 0 user started scrolling but did not finish -> dont do anything
                    if (mDiff != 0) {
                        mImageChangeListener?.onPageSelected(mSelectedPosition, mDiff < 0)
                        // Reset mDiff in case user does not fully scroll the next time
                        // in which case mDiff would carry the previous value and trigger onPageSelected
                        mDiff = 0
                    }
                }
            }
        })
        mViewPager?.adapter = ImagePagerAdapter(activity!!, mImages)
    }

    override fun onRecyclerItemClick(position: Int) {
        DLog.i(LOG_TAG, "onRecyclerItemClick")
        mViewPager?.setCurrentItem(position, true)

    }

    interface OnViewPagerImageChangeListener {
        fun onPageSelected(position: Int, scrollToRight: Boolean)
    }
}
