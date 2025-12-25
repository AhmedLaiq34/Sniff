package com.ahmedProjects.captionscraperapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostItem(
    val caption: String,
    val url: String,
    val timestamp: String? = null,
    var username: String
) : Parcelable