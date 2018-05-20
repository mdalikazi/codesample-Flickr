package com.alikazi.codesampleflickr.main

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.util.Property
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.alikazi.codesampleflickr.R
import com.alikazi.codesampleflickr.constants.AppConstants
import com.alikazi.codesampleflickr.utils.DLog
import com.alikazi.codesampleflickr.utils.CustomViewUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

/**
 * Created by kazi_ on 15-Apr-18.
 */
class RecyclerAdapter(context: Context, itemClickListener: RecyclerItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val LOG_TAG = AppConstants.LOG_TAG_MAIN
        private const val VIEW_TYPE_ITEM = 0
    }

    private var mContext = context
    private var mAnimate: Boolean = false
    private var mListItems: Properties? = null
    private var mItemClickListener = itemClickListener

    fun setListItems(listItems: Properties?) {
        DLog.i(LOG_TAG, "setListItems")
        mListItems?.data?.clear()
        notifyDataSetChanged()
        mListItems = listItems
        mAnimate = true
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        when (viewType) {
            VIEW_TYPE_ITEM -> {
                view = LayoutInflater.from(mContext).inflate(R.layout.item_recycler_view, parent, false)
                return BasicropertyViewHolder(view)
            }
            else -> throw RuntimeException("There are invalid views inside RecyclerAdapter!")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        animateList(holder.itemView)
        val adapterPosition = holder.adapterPosition
        val property: Property? = mListItems?.data?.get(adapterPosition)
        holder.itemView.setOnClickListener({ mItemClickListener.onPropertyItemClick(property)})

        when (holder.itemViewType) {
            VIEW_TYPE_ITEM -> {
                val viewHolder: BasicropertyViewHolder = holder as BasicropertyViewHolder
                Glide.with(mContext)
                        .load(property?.photo?.image?.url)
                        .transition(DrawableTransitionOptions().crossFade())
                        .apply(RequestOptions().encodeQuality(100).diskCacheStrategy(DiskCacheStrategy.ALL))
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                viewHolder.basicPhotoProgressBar.visibility = View.GONE
                                return false
                            }

                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                viewHolder.basicPhotoProgressBar.visibility = View.GONE
                                return false
                            }
                        })
                        .into(viewHolder.basicPropertyPhoto)
                viewHolder.basicTitle.text = property?.title
                viewHolder.basicAddress.text = mContext.getString(R.string.property_address_combined,
                        property?.location?.address_1,
                        property?.location?.suburb,
                        property?.location?.postcode)
                viewHolder.basicOwnerName.text = mContext.getString(R.string.property_owner_name_combined,
                        property?.owner?.first_name,
                        property?.owner?.last_name)
                CustomViewUtils.showCircularPhotoWithGlide(mContext, property?.owner?.avatar?.url, R.drawable.ic_account, viewHolder.basicOwnerAvatar)
                viewHolder.basicPropertyBedrooms.text = property?.bedrooms.toString()
                viewHolder.basicPropertyBathrooms.text = property?.bathrooms.toString()
                viewHolder.basicPropertyCarspots.text = property?.carspots.toString()
            }
        }
    }

    override fun getItemCount(): Int = when(mListItems) {
        null -> 0
        else -> mListItems!!.data.size
    }

    override fun getItemViewType(position: Int): Int = when(mListItems) {
        null -> super.getItemViewType(position)
        else -> when (mListItems?.data?.get(position)?.is_premium) {
            true -> VIEW_TYPE_PREMIUM
            false -> VIEW_TYPE_ITEM
            else -> super.getItemViewType(position)
        }
    }

    private fun animateList(view: View) {
        if (!mAnimate) {
            return
        }
        val translateAnimation = TranslateAnimation(0f, 0f, 500f, 0f)
        translateAnimation.interpolator = DecelerateInterpolator()
        translateAnimation.duration = 500
        translateAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                view.animate().alpha(1f).duration = 250
            }

            override fun onAnimationEnd(animation: Animation) {
                // Animate only once at the start
                mAnimate = false
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
        view.alpha = 0f
        view.startAnimation(translateAnimation)
    }

    private class RecyclerItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.recycler_item_image_view)
        var progressBar: ProgressBar = itemView.findViewById(R.id.recycler_item_progress_bar)
    }

    interface RecyclerItemClickListener {
        fun onPropertyItemClick(property: Property?)
    }

}
