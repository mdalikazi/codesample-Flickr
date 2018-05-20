package com.alikazi.codesampleflickr.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ImageItem(var title: String = "Title missing",
                     var link: String,
                     var media: Media,
                     @SerializedName("date_taken")
                 var dateTaken: String,
                     var description: String = "Description missing",
                     var published: String,
                     var author: String,
                     @SerializedName("author_id")
                 var authorId: String,
                     var tags: String) : Parcelable