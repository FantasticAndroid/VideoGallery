package com.android.videogallery.activities

import android.graphics.Color
import android.os.Bundle
import com.android.videogallery.R
import com.android.videogallery.Utils
import kotlinx.android.synthetic.main.activity_main.*

class DashboardActivity : CoreActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playVideo.setOnClickListener {
            val bundle = Bundle()
            val videoUrl = "https://www.radiantmediaplayer.com/media/bbb-360p.mp4"
            bundle.putString(Utils.VIDEO_URL, videoUrl)
            ExoVideoPlayerActivity.startExoPlayerActivity(this, bundle)
        }

        playVideoGalleryList.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable(
                Utils.KEY_GALLERY_LAYOUT_TYPE,
                Utils.LayoutType.VIDEO_GALLERY_RVL
            )
            bundle.putInt(Utils.KEY_GALLERY_LAYOUT_COLOR, Color.MAGENTA)
            VideoGalleryActivity.startExoVideoGalleryActivity(this, bundle)
        }

        playVideoGalleryBigList.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable(
                Utils.KEY_GALLERY_LAYOUT_TYPE,
                Utils.LayoutType.VIDEO_GALLERY_RVH
            )
            bundle.putInt(Utils.KEY_GALLERY_LAYOUT_COLOR, Color.CYAN)
            VideoGalleryActivity.startExoVideoGalleryActivity(this, bundle)
        }

        playVideoGalleryGrid.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable(
                Utils.KEY_GALLERY_LAYOUT_TYPE,
                Utils.LayoutType.VIDEO_GALLERY_RVG
            )
            bundle.putInt(Utils.KEY_GALLERY_LAYOUT_COLOR, Color.BLUE)
            VideoGalleryActivity.startExoVideoGalleryActivity(this, bundle)
        }

        playVideoGalleryListing.setOnClickListener {
            /*val bundle = Bundle()
            bundle.putSerializable(
                Utils.KEY_GALLERY_LAYOUT_TYPE,
                Utils.LayoutType.VIDEO_GALLERY_RVG
            )
            bundle.putInt(Utils.KEY_GALLERY_LAYOUT_COLOR, Color.BLUE)*/
            VideoListingActivity.startActivity(this, null)
        }


    }
}
