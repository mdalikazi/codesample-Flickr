package com.alikazi.codesample_flickr.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Items(var title: String,
                 var link: String,
                 var description: String,
                 var modified: String,
                 var generator: String,
                 @SerializedName("items")
                 var images: ArrayList<ImageItem>) : Parcelable
