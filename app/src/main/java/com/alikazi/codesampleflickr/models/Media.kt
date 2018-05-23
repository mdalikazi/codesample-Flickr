package com.alikazi.codesampleflickr.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Media(@SerializedName("m") var url: String) : Parcelable