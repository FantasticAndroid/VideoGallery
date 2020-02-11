package com.android.videogallery.activities

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import com.android.videogallery.R
import com.android.videogallery.Utils
import com.android.videogallery.providers.ExoVideoPlayerProvider
import com.android.videogallery.providers.VideoGalleryUiProvider

class VideoGalleryActivity : CoreActivity() {

    private var videoGalleryUiProvider: VideoGalleryUiProvider? = null
    private var exoVideoPlayerProvider: ExoVideoPlayerProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view =
            layoutInflater.inflate(R.layout.activity_video_gallery, null)
        setContentView(view)

        videoGalleryUiProvider = VideoGalleryUiProvider(view, this)

        val bundle = intent.extras
        bundle?.let {
            videoGalleryUiProvider!!.initUi(
                bundle.getSerializable(Utils.KEY_GALLERY_LAYOUT_TYPE) as Utils.LayoutType,
                bundle.getInt(Utils.KEY_GALLERY_LAYOUT_COLOR),
                false,
                savedInstanceState
            )

        }?:run {
            videoGalleryUiProvider!!.initUi(
                Utils.LayoutType.VIDEO_GALLERY_RVL,
                Color.CYAN,
                false,
                savedInstanceState
            )
        }

        exoVideoPlayerProvider = videoGalleryUiProvider!!.getExoVideoPlayerProvider()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        videoGalleryUiProvider?.doOnOrientationChanged()
    }

    override fun onPause() {
        super.onPause()
        exoVideoPlayerProvider?.onActivityPause()
    }

    override fun onStop() {
        super.onStop()
        exoVideoPlayerProvider?.onActivityStop()
    }

    override fun onStart() {
        super.onStart()
        exoVideoPlayerProvider?.onActivityStart()
    }

    override fun onResume() {
        super.onResume()
        exoVideoPlayerProvider?.onActivityResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        exoVideoPlayerProvider?.onActivitySaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        exoVideoPlayerProvider?.onRestoreInstanceState(savedInstanceState)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean { // See whether the player view wants to handle media or DPAD keys events.
        return if (exoVideoPlayerProvider != null) {
            exoVideoPlayerProvider!!.dispatchActivityKeyEvent(event) || super.dispatchKeyEvent(
                event
            )
        } else super.dispatchKeyEvent(event)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (exoVideoPlayerProvider != null) {
            exoVideoPlayerProvider!!.onActivityNewIntent(intent)
        }
        setIntent(intent)
    }

    override fun onBackPressed() {
        if (videoGalleryUiProvider != null) {
            if (!videoGalleryUiProvider!!.onBackPressed()) {
                if (exoVideoPlayerProvider != null) {
                    exoVideoPlayerProvider!!.onBackPressed()
                }
                super.onBackPressed()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (exoVideoPlayerProvider != null) {
            exoVideoPlayerProvider!!.onActivityDestroy()
        }
        videoGalleryUiProvider?.clearAdapter()
    }

    companion object{
        /**
         * @param context
         * @param bundle
         */
        fun startExoVideoGalleryActivity(context: Activity, bundle: Bundle?) {
            val intent =
                Intent(context, VideoGalleryActivity::class.java)
            if (bundle != null) {
                intent.putExtras(bundle)
            }
            context.startActivity(intent)
        }
    }
}
