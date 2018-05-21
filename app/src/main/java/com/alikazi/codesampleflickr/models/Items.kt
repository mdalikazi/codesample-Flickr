package com.alikazi.codesampleflickr.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Items(var title: String,
                 var link: String,
                 var description: String,
                 var modified: String,
                 var generator: String,
                 var items: ArrayList<ImageItem>) : Parcelable
