package com.alikazi.codesample_flickr.main

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import com.alikazi.codesample_flickr.R
import com.alikazi.codesample_flickr.constants.AppConstants
import com.alikazi.codesample_flickr.models.ImageItem
import com.alikazi.codesample_flickr.utils.CustomAnimationUtils
import com.alikazi.codesample_flickr.utils.CustomViewUtils
import com.alikazi.codesample_flickr.utils.DLog

/**
 * Created by kazi_ on 22-May-18.
 */
class RecyclerAdapter(context: Context) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>(),
        CustomAnimationUtils.ListAnimationListener {

    companion object {
        private const val LOG_TAG = AppConstants.LOG_TAG_MAIN
        private const val VIEW_TYPE_ITEM = 0
    }

    private var mContext = context
    private var mAnimate: Boolean = false
    private var mClickable: Boolean = true
    private var mSelectedItemPosition: Int = 0
    private var mListItems: ArrayList<ImageItem>? = null
    private var mItemClickListener: RecyclerItemClickListener? = null

    fun setListItems(listItems: ArrayList<ImageItem>?) {
        DLog.i(LOG_TAG, "setListItems")
        mListItems?.clear()
        notifyDataSetChanged()
        mListItems = listItems
        mAnimate = true
        notifyDataSetChanged()
    }

    fun setRecyclerItemClickListener(itemClickListener: RecyclerItemClickListener) {
        mItemClickListener = itemClickListener
    }

    /**
     * When user swipes ViewPager, recycler adapter should highlight the current image
     * @param selectedPosition: Position of the new image in ViewPager
     */
    fun setSelectedPositionFromViewPager(selectedPosition: Int) {
        if (mSelectedItemPosition != selectedPosition) {
            notifyItemChanged(mSelectedItemPosition)
            mSelectedItemPosition = selectedPosition
            notifyItemChanged(selectedPosition)
        }
    }

    fun setItemsClickable(clickable: Boolean) {
        mClickable = clickable
    }

    /**
     * Animation should happen only once at the start
     */
    override fun onListAnimationEnd() {
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
        holder.itemView.tag = adapterPosition
        holder.itemView.isSelected = (adapterPosition == mSelectedItemPosition)
        holder.itemView.setOnClickListener({
            if (mClickable) {
                // Unselect previous item
                notifyItemChanged(mSelectedItemPosition)
                // Select clicked item
                holder.itemView.isSelected = true
                // Save selected position
                mSelectedItemPosition = adapterPosition
                // Update ViewPager
                mItemClickListener?.onRecyclerItemClick(adapterPosition)
            }
        })

        when (holder.itemViewType) {
            VIEW_TYPE_ITEM -> {
                val viewHolder: ImageItemViewHolder = holder as ImageItemViewHolder
                CustomViewUtils.showImageWithGlide(mContext,
                        image?.media?.url,
                        viewHolder.imageView,
                        viewHolder.progressBar,
                        viewHolder.errorImageView)
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
        var errorImageView: ImageView = itemView.findViewById(R.id.recycler_item_error_image_view)
    }

    interface RecyclerItemClickListener {
        /**
         * Let ViewPager know recycler adapter selected image
         */
        fun onRecyclerItemClick(position: Int)
    }

}
