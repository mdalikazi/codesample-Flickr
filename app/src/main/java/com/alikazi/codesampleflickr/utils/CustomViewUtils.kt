package com.alikazi.codesampleflickr.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import com.alikazi.codesampleflickr.R

class CustomViewUtils {

    companion object {

        fun getTintedIcon(context: Context, drawableResId: Int) : Drawable? {
            return getTintedIconWithColor(context, drawableResId, R.color.colorIconInactive)
        }

        fun getTintedIconWithColor(context: Context, @DrawableRes drawableResId: Int, @ColorRes color: Int) : Drawable? {
            val drawable: Drawable? = ContextCompat.getDrawable(context, drawableResId)
            drawable?.setTint(ContextCompat.getColor(context, color))
            return drawable
        }
    }
}
