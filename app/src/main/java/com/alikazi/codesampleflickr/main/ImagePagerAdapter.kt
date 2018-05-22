package com.alikazi.codesampleflickr.main

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alikazi.codesampleflickr.R
import com.alikazi.codesampleflickr.constants.AppConstants
import com.alikazi.codesampleflickr.models.ImageItem
import com.alikazi.codesampleflickr.utils.CustomViewUtils
import com.alikazi.codesampleflickr.utils.DLog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
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
        DLog.i(LOG_TAG, "instantiateItem")
        var view: View = LayoutInflater.from(mContext).inflate(R.layout.item_view_pager, container, false)
        Glide.with(mContext)
                .load(mImages?.get(position)?.media?.url)
                .transition(DrawableTransitionOptions().crossFade())
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        view.pager_item_progress_bar.visibility = View.GONE
                        view.pager_item_image_view.setImageDrawable(CustomViewUtils.getTintedIconWithColor(mContext, R.drawable.ic_error, R.color.colorFavorite))
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        view.pager_item_progress_bar.visibility = View.GONE
                        return false
                    }
                })
                .into(view.pager_item_image_view)

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as ViewGroup)
    }
}
