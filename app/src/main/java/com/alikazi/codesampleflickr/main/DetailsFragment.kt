package com.alikazi.codesampleflickr.main

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alikazi.codesampleflickr.R
import com.alikazi.codesampleflickr.constants.AppConstants
import com.alikazi.codesampleflickr.models.ImageItem
import com.alikazi.codesampleflickr.utils.DLog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.content_detail.view.*

class DetailsFragment : Fragment() {

    companion object {
        const val LOG_TAG = AppConstants.LOG_TAG_MAIN
        const val INTENT_EXTRA_RECYCLER_ITEM = "INTENT_EXTRA_RECYCLER_ITEM"
    }

    private var mImage: ImageItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DLog.i(LOG_TAG, "onCreate")
        arguments?.let {
            if (it.containsKey(INTENT_EXTRA_RECYCLER_ITEM)) {
                mImage = it.getParcelable(INTENT_EXTRA_RECYCLER_ITEM) as ImageItem?
                DLog.d(LOG_TAG, "mImage != null")
                DLog.d(LOG_TAG, "mImage?.title: " + mImage?.title)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        DLog.i(LOG_TAG, "onCreateView")
        val view = inflater.inflate(R.layout.content_detail, container, false)
        Glide.with(activity!!)
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
                .into(view.test_image_view)
        /*view.property_detail_empty_message.visibility = if (mProperty == null) View.VISIBLE else View.GONE
        view.property_detail_title.text = mProperty?.title
        view.property_detail_address.text = getString(R.string.property_address_combined,
                        mProperty?.location?.address_1,
                        mProperty?.location?.suburb,
                        mProperty?.location?.postcode)
        view.property_detail_description.text = mProperty?.description
        try {
            view.property_detail_price.text = NumberFormat.getCurrencyInstance().format(mProperty?.price)
        } catch (e: Exception) {
            view.property_detail_price.text = ""
        }*/
        return view
    }
}
