package com.android.videogallery.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class VideoGallery {
    @SerializedName("id")
    @Expose
    val id: String? = null
    @SerializedName("title")
    @Expose
    val title: String? = null
    @SerializedName("description")
    @Expose
    val description: String? = null
    @SerializedName("videoUrl")
    @Expose
    val videoUrl: String? = null
    @SerializedName("imageUrl")
    @Expose
    val imageUrl: String? = null
    @SerializedName("squareImageUrl")
    @Expose
    val squareImageUrl: String? = null
    @SerializedName("video_duration")
    @Expose
    val videoDuration: String? = null
}