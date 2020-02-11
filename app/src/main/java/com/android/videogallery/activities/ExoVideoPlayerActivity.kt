package com.android.videogallery.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import com.android.videogallery.R
import com.android.videogallery.Utils
import com.android.videogallery.callbacks.ExoVideoPlayerInterface
import com.android.videogallery.providers.ExoVideoPlayerProvider

class ExoVideoPlayerActivity : CoreActivity() {
    private var exoVideoPlayerProvider: ExoVideoPlayerProvider? = null
    private var orientationHandler: Handler? = null
    private var videoUrlActual: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orientationHandler = Handler()
        val view = layoutInflater.inflate(R.layout.activity_video_player, null)
        setContentView(view)
        readDataFromBundle(view, savedInstanceState)
    }

    private fun readDataFromBundle(view: View, savedInstanceState: Bundle?) {
        val bundle = intent.extras
        if (null != bundle) {
            videoUrlActual = bundle.getString(Utils.VIDEO_URL)

            val playbackPosition =
                bundle.getLong(Utils.KEY_EXO_PLAYER_POSITION, 0)
            val videoDuration =
                bundle.getInt(Utils.KEY_VIDEO_DURATION, 0)

            exoVideoPlayerProvider = ExoVideoPlayerProvider(view, this, exoVideoPlayerInterface)
            exoVideoPlayerProvider!!.initExoPlayerUI(Color.MAGENTA, videoDuration)
            exoVideoPlayerProvider!!.onActivityCreate(videoUrlActual!!)
            exoVideoPlayerProvider!!.onActivityCreated(savedInstanceState)
            exoVideoPlayerProvider!!.setPlayBackPosition(true, playbackPosition)
        }
    }

    private val exoVideoPlayerInterface: ExoVideoPlayerInterface =
        object : ExoVideoPlayerInterface {
            override fun onVideoPlaybackReady() {}
            override fun onFullScreenBtnTapped() {
                val currentOrientation = resources.configuration.orientation
                val userOrientationLockStatus = Settings.System.getInt(
                    contentResolver,
                    Settings.System.ACCELEROMETER_ROTATION,
                    1
                )
                Log.d(
                    TAG,
                    "userOrientationLockStatus" + if (userOrientationLockStatus == 1) "Unlocked" else "Locked"
                )
                requestedOrientation = if (userOrientationLockStatus == 1) {
                    Log.d(TAG, "userOrientationLockStatus Unlocked")
                    // rotation is Unlocked
                    if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                        ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
                    }
                } else {
                    Log.d(TAG, "userOrientationLockStatus Locked")
                    // rotation is Locked
                    if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                        ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
                    }
                }
                orientationHandler!!.postDelayed({
                    if (userOrientationLockStatus == 1) {
                        Log.d(TAG, "userOrientationLockStatus Unlocked")
                        // rotation is Unlocked
                        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_USER
                        }
                    }
                }, 500)
            }

            override fun onVideoPlaybackEnded() {
                Log.d(TAG, "onVideoPlaybackEnded")
                finishThisScreen()
            }
        }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val currentOrientation = resources.configuration.orientation
        exoVideoPlayerProvider?.onConfigurationChanged(currentOrientation)
    }

    public override fun onPause() {
        super.onPause()
        exoVideoPlayerProvider?.onActivityPause()
    }

    public override fun onStop() {
        super.onStop()
        exoVideoPlayerProvider?.onActivityStop()
    }

    public override fun onStart() {
        super.onStart()
        exoVideoPlayerProvider?.onActivityStart()
    }

    public override fun onResume() {
        super.onResume()
        exoVideoPlayerProvider?.onActivityResume()
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        exoVideoPlayerProvider?.onActivitySaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        exoVideoPlayerProvider?.onRestoreInstanceState(savedInstanceState)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean { // See whether the player view wants to handle media or DPAD keys events.
        return if (exoVideoPlayerProvider != null) {
            exoVideoPlayerProvider!!.dispatchActivityKeyEvent(event) || super.dispatchKeyEvent(event)
        } else super.dispatchKeyEvent(event)
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        exoVideoPlayerProvider?.onActivityNewIntent(intent)
        setIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        exoVideoPlayerProvider?.onActivityDestroy()
    }

    override fun onBackPressed() {
        val currentOrientation = resources.configuration.orientation
        val userOrientationLockStatus =
            Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 1)
        Log.d(
            TAG,
            "userOrientationLockStatus" + if (userOrientationLockStatus == 1) "Unlocked" else "Locked"
        )
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
            orientationHandler?.postDelayed({
                if (userOrientationLockStatus == 1) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_USER
                }
            }, 500)
        } else {
            exoVideoPlayerProvider?.onBackPressed()
            finishThisScreen()
        }
    }

    private fun finishThisScreen() {
        val intent = Intent()

        exoVideoPlayerProvider?.let {
            val currentPlayBackPosition: Long = it.playbackPosition
            val bundle = Bundle()
            bundle.putLong(Utils.KEY_EXO_PLAYER_POSITION, currentPlayBackPosition)
            bundle.putInt(Utils.KEY_EXO_PLAYER_WINDOW, it.currentWindow)
            intent.putExtra(Utils.VIDEO_URL, videoUrlActual)
            intent.putExtra(Utils.KEY_EXO_PLAYER_POSITION, currentPlayBackPosition)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    companion object {
        private val TAG = ExoVideoPlayerActivity::class.java.simpleName
        /**
         * @param context
         * @param bundle
         */
        fun startExoPlayerActivity(context: Context, bundle: Bundle?) {
            val intent = Intent(context, ExoVideoPlayerActivity::class.java)
            if (bundle != null) {
                intent.putExtras(bundle)
            }
            context.startActivity(intent)
        }

        /***
         *
         * @param context
         * @param bundle
         * @param requestCode
         */
        fun startExoPlayerActivityWithResult(context: Activity, bundle: Bundle?, requestCode: Int) {
            val intent = Intent(context, ExoVideoPlayerActivity::class.java)
            if (bundle != null) {
                intent.putExtras(bundle)
            }
            context.startActivityForResult(intent, requestCode)
        }
    }
}