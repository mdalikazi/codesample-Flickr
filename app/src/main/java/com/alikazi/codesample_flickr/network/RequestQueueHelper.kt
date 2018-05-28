package com.alikazi.codesample_flickr.network

import android.content.Context
import com.alikazi.codesample_flickr.constants.AppConstants
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

/**
 * Created by kazi_ on 15-Apr-18.
 */
class RequestQueueHelper(context: Context) {

    private var mContext = context
    private var mRequestQueue: RequestQueue = getRequestQueue()

    companion object {
        private val LOG_TAG = AppConstants.LOG_TAG_NETWORK

        var mInstance: RequestQueueHelper? = null

        @Synchronized
        fun getInstance(context: Context): RequestQueueHelper {
            if (mInstance == null) {
                mInstance = RequestQueueHelper(context)
            }

            return mInstance!!
        }
    }

    fun getRequestQueue(): RequestQueue {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext.applicationContext)
        }

        return mRequestQueue
    }

    fun <T> addToRequestQueue(request: Request<T>) {
        getRequestQueue().add(request)
    }

    fun cancelAllRequests() {
        getRequestQueue().cancelAll(mContext.applicationContext)
    }
}
