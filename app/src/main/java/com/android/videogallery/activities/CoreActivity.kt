package com.android.videogallery.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.VideoApp

abstract class CoreActivity : AppCompatActivity() {

    lateinit var videoApp: VideoApp
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        videoApp = application as VideoApp
    }
}