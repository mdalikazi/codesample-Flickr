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

class DetailsFragment : Fragment() {

    companion object {
        const val LOG_TAG = AppConstants.LOG_TAG_MAIN

        const val INTENT_EXTRA_IMAGE_ITEM = "INTENT_EXTRA_IMAGE_ITEM"
        const val INTENT_EXTRA_IMAGES = "INTENT_EXTRA_IMAGES"
    }

    fun setImageChangeListener(listener: OnViewPagerImageChangeListener) {
        mImageChangeListener = listener
    }

    private var mSelectedPosition = 0
    private var mViewPager: ViewPager? = null
    private var mEmptyTextView: TextView? = null
    private var mImage: ImageItem? = null
    private var mImages: ArrayList<ImageItem>? = ArrayList()
    private var mImageChangeListener: OnViewPagerImageChangeListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DLog.i(LOG_TAG, "onCreate")
        /*arguments?.let {
            if (it.containsKey(INTENT_EXTRA_IMAGE_ITEM)) {
                mImage = it.getParcelable(INTENT_EXTRA_IMAGE_ITEM)
                DLog.d(LOG_TAG, "mImage != null")
                DLog.d(LOG_TAG, "mImage?.title: " + mImage?.title)
            }
            if (it.containsKey(INTENT_EXTRA_IMAGES)) {
                mImages = it.getParcelableArrayList(INTENT_EXTRA_IMAGES)
                DLog.d(LOG_TAG, "mImages != null ${mImages?.size}")
            }
        }*/
    }

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
//        setupViewPager(view)
        /*Glide.with(activity!!)
                .load(mImage?.media?.m)
                .transition(DrawableTransitionOptions().crossFade())
                .apply(RequestOptions().encodeQuality(100).diskCacheStrategy(DiskCacheStrategy.ALL))
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, isFirstResource: Boolean): Boolean {
//                        view.progressBar.visibility = View.GONE
                        view.view_pager_empty_text_view.visibility = View.VISIBLE
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
//                        viewHolder.progressBar.visibility = View.GONE
                        return false
                    }
                })
                .into(view.test_image_view)*/
        return view
    }

    private fun setupViewPager() {
        DLog.i(LOG_TAG, "setupViewPager")
        mViewPager?.offscreenPageLimit = 0
        mViewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                DLog.i(LOG_TAG, "onPageSelected: $position")
                mSelectedPosition = position
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    DLog.i(LOG_TAG, "onPageScrollStateChanged")
                    mImageChangeListener?.onPageSelected(mSelectedPosition)
                }
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }
        })
        mViewPager?.adapter = ImagePagerAdapter(activity!!, mImages)
    }

    interface OnViewPagerImageChangeListener {
        fun onPageSelected(position: Int)
    }
}
