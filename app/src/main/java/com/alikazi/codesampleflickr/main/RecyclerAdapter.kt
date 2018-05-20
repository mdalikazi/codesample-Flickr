package com.alikazi.codesampleflickr.main

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import com.alikazi.codesampleflickr.R
import com.alikazi.codesampleflickr.constants.AppConstants
import com.alikazi.codesampleflickr.models.ImageItem
import com.alikazi.codesampleflickr.utils.CustomAnimationUtils
import com.alikazi.codesampleflickr.utils.DLog
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
class RecyclerAdapter(context: Context, itemClickListener: RecyclerItemClickListener) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>(),
        CustomAnimationUtils.ListAnimationListener {

    companion object {
        private const val LOG_TAG = AppConstants.LOG_TAG_MAIN
        private const val VIEW_TYPE_ITEM = 0
    }

    private var mContext = context
    private var mAnimate: Boolean = false
    private var mListItems: ArrayList<ImageItem>? = null
    private var mItemClickListener = itemClickListener

    fun setListItems(listItems: ArrayList<ImageItem>?) {
        DLog.i(LOG_TAG, "setListItems")
        mListItems?.clear()
        notifyDataSetChanged()
        mListItems = listItems
        mAnimate = true
        notifyDataSetChanged()
    }

    override fun onListAnimationEnd() {
        // Animate only once at the start
        mAnimate = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        when (viewType) {
            VIEW_TYPE_ITEM -> {
                view = LayoutInflater.from(mContext).inflate(R.layout.item_recycler_view, parent, false)
                return ImageItemViewHolder(view)
            }
            else -> throw RuntimeException("There are invalid views inside RecyclerAdapter!")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (mAnimate) {
            CustomAnimationUtils.animateList(holder.itemView, this)
        }
        val adapterPosition = holder.adapterPosition
        val image: ImageItem? = mListItems?.get(adapterPosition)
        holder.itemView.setOnClickListener({ mItemClickListener.onPropertyItemClick(image)})

        when (holder.itemViewType) {
            VIEW_TYPE_ITEM -> {
                val viewHolder: ImageItemViewHolder = holder as ImageItemViewHolder
                Glide.with(mContext)
                        .load(image?.media?.m)
                        .transition(DrawableTransitionOptions().crossFade())
                        .apply(RequestOptions().encodeQuality(100).diskCacheStrategy(DiskCacheStrategy.ALL))
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                viewHolder.progressBar.visibility = View.GONE
                                return false
                            }

                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                viewHolder.progressBar.visibility = View.GONE
                                return false
                            }
                        })
                        .into(viewHolder.imageView)
            }
        }
    }

    override fun getItemCount(): Int = when(mListItems) {
        null -> 0
        else -> mListItems!!.size
    }

    override fun getItemViewType(position: Int): Int = when(mListItems) {
        null -> super.getItemViewType(position)
        else -> VIEW_TYPE_ITEM
    }

    private class ImageItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.recycler_item_image_view)
        var progressBar: ProgressBar = itemView.findViewById(R.id.recycler_item_progress_bar)
    }

    interface RecyclerItemClickListener {
        fun onPropertyItemClick(image: ImageItem?)
    }

}
