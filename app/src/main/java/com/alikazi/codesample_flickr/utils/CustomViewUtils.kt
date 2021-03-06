package com.alikazi.codesample_flickr.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.alikazi.codesample_flickr.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

class CustomViewUtils {

    companion object {

        fun getStandardTintedIcon(context: Context, drawableResId: Int) : Drawable? {
            return getTintedIconWithColor(context, drawableResId, R.color.colorIconInactive)
        }

        fun getTintedIconWithColor(context: Context, @DrawableRes drawableResId: Int, @ColorRes color: Int) : Drawable? {
            val drawable: Drawable? = ContextCompat.getDrawable(context, drawableResId)
            drawable?.setTint(ContextCompat.getColor(context, color))
            return drawable
        }

        fun showImageWithGlide(context: Context,
                               @Nullable url: String?,
                               @NotNull imageView: ImageView,
                               @Nullable progressBar: ProgressBar?,
                               @Nullable errorImageView: ImageView?) {
            Glide.with(context)
                    .load(url)
                    .transition(DrawableTransitionOptions().crossFade())
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            progressBar?.visibility = View.GONE
                            errorImageView?.visibility = View.VISIBLE
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            progressBar?.visibility = View.GONE
                            errorImageView?.visibility = View.GONE
                            return false
                        }
                    })
                    .into(imageView)
        }

        /**
         * Gives measuredWidth of first child of recycler view.
         * This can be used to caluculate how much to scroll.
         */
        private fun calculateScrollByXForOneChild(recyclerView: RecyclerView) : Int {
            val measuredWidth = recyclerView.getChildAt(0).measuredWidth
            return CustomViewUtils.getComplexUnitDip(recyclerView.context, measuredWidth.toFloat()).toInt()
        }

        fun getComplexUnitDip(context: Context, measuredWidth: Float) : Float {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, measuredWidth, context.resources.displayMetrics)
        }
    }
}
