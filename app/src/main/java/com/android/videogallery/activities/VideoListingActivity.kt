package com.android.videogallery.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.android.videogallery.R

class VideoListingActivity : CoreActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_listing)
    }

    companion object{
        /**
         * @param context
         * @param bundle
         */
        fun startActivity(context: Activity, bundle: Bundle?) {
            val intent =
                Intent(context, VideoListingActivity::class.java)
            if (bundle != null) {
                intent.putExtras(bundle)
            }
            context.startActivity(intent)
        }
    }
}