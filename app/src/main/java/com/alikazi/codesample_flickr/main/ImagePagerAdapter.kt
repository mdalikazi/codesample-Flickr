package com.alikazi.codesample_flickr.main

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alikazi.codesample_flickr.R
import com.alikazi.codesample_flickr.constants.AppConstants
import com.alikazi.codesample_flickr.models.ImageItem
import com.alikazi.codesample_flickr.utils.CustomViewUtils
import kotlinx.android.synthetic.main.item_view_pager.view.*

class ImagePagerAdapter(context: Context, images: ArrayList<ImageItem>?) : PagerAdapter() {

    companion object {
        private const val LOG_TAG = AppConstants.LOG_TAG_MAIN
    }

    private var mContext: Context = context
    private var mImages: ArrayList<ImageItem>? = images

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getCount(): Int = when(mImages) {
        null -> 0
        else -> {
            mImages!!.size
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int) : Any {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.item_view_pager, container, false)

        CustomViewUtils.showImageWithGlide(mContext,
                mImages?.get(position)?.media?.url,
                view.pager_item_image_view,
                view.pager_item_progress_bar,
                view.pager_item_error_image_view)

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as ViewGroup)
    }
}
