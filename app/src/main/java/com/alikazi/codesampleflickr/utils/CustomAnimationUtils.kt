package com.alikazi.codesampleflickr.utils

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.View
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import com.alikazi.codesampleflickr.constants.AppConstants

/**
 * Created by kazi_ on 15-Apr-18.
 */
class CustomAnimationUtils {

    companion object {

        private const val DURATION_LONG: Long = 400
        private const val DURATION_SHORT: Long = 250

        fun animateToolbar(context: Context, toolbar: Toolbar?, listener: ToolbarAnimationListener?) {
            DLog.i(AppConstants.LOG_TAG_MAIN, "animateToolbar")
            if (toolbar != null) {
                val layoutParams = toolbar.layoutParams
                val toolbarHeight: Float = layoutParams.height.toFloat()
                val animator: ValueAnimator = ValueAnimator.ofFloat(toolbarHeight, getDefaultActionBarHeightInPixels(context))
                animator.duration = DURATION_LONG
                animator.startDelay = 500
                animator.interpolator = DecelerateInterpolator()
                animator.setTarget(toolbar)
                animator.addUpdateListener { valueAnimator ->
                    val lp = toolbar.layoutParams
                    val animatedHeight = valueAnimator.animatedValue
                    if (animatedHeight is Float) {
                        lp.height = animatedHeight.toInt()
                    }
                    toolbar.layoutParams = lp
                }

                animator.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animator: Animator) {

                    }

                    override fun onAnimationEnd(animator: Animator) {
                        listener?.onToolbarAnimationEnd()
                    }

                    override fun onAnimationCancel(animator: Animator) {

                    }

                    override fun onAnimationRepeat(animator: Animator) {

                    }
                })
                animator.start()
            }
        }

        fun getDefaultActionBarHeightInPixels(context: Context): Float {
            DLog.i(AppConstants.LOG_TAG_MAIN, "getDefaultActionBarHeightInPixels")
            val typedValue = TypedValue()
            val canGetValue = context.theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)
            return if (canGetValue) {
                TypedValue.complexToDimensionPixelSize(typedValue.data, context.resources.displayMetrics).toFloat()
            } else 0f
        }

        fun animateList(view: View, listener: ListAnimationListener?) {
            val translateAnimation = TranslateAnimation(500f, 0f, 0f, 0f)
            translateAnimation.interpolator = DecelerateInterpolator()
            translateAnimation.duration = DURATION_LONG
            translateAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    view.animate().alpha(1f).duration = DURATION_SHORT
                }

                override fun onAnimationEnd(animation: Animation) {
                    listener?.onListAnimationEnd()
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
            view.alpha = 0f
            view.startAnimation(translateAnimation)
        }
    }

    interface ToolbarAnimationListener {
        fun onToolbarAnimationEnd()
    }

    interface ListAnimationListener {
        fun onListAnimationEnd()
    }
}
