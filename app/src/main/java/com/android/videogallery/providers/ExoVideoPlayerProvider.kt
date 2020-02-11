package com.android.videogallery.providers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.widget.ImageViewCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.videogallery.R
import com.android.videogallery.Utils
import com.android.videogallery.activities.CoreActivity
import com.android.videogallery.callbacks.ExoVideoPlayerInterface
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class ExoVideoPlayerProvider(
    view: View,
    private val coreActivity: CoreActivity,
    private val exoVideoPlayerInterface: ExoVideoPlayerInterface
) : PlaybackPreparer, View.OnClickListener {

    private val videoApp = coreActivity.videoApp

    private val playerView: PlayerView = view.findViewById(R.id.video_view)

    private val videoProgressBar: ProgressBar = view.findViewById(R.id.progress_bar_videoplayer)

    private val fullScreenIBtn: ImageView = view.findViewById(R.id.exo_full_screen)

    private val exoPlayIv: ImageView = view.findViewById(R.id.exo_play)

    private val exoPauseIv: ImageView = view.findViewById(R.id.exo_pause)

    private val defaultTimeBar: DefaultTimeBar = view.findViewById(R.id.exo_progress)

    private val durationTv: TextView = view.findViewById(R.id.exo_duration)

    private val positionTv: TextView = view.findViewById(R.id.exo_position)

    private val exoplayerController: RelativeLayout =
        view.findViewById(R.id.rl_exoplayer_controller)

    private val TAG = ExoVideoPlayerProvider::class.java.simpleName
    private lateinit var player: SimpleExoPlayer
    var currentWindow = 0
    private var playWhenReady = true
    private var mIsVideoStartedTracked = false
    private var mIsPlayed25PercTracked = false
    var playbackPosition: Long = 0

    private var videoUrl: String? = null

    private var dataSourceFactory = DefaultDataSourceFactory(
        coreActivity.videoApp,
        Util.getUserAgent(videoApp, videoApp.getString(R.string.app_name))
    )
    private val KEY_WINDOW = "window"
    private val KEY_POSITION = "position"
    private val KEY_AUTO_PLAY = "auto_play"
    private var videoDuration = 0
    private var isBackPressed = false
    private var colorCode = Color.CYAN

    init {
        checkAndStopAudioMediaService()
    }

    /***
     * @param colorCode
     * @param videoDuration
     */
    fun initExoPlayerUI(colorCode: Int, videoDuration: Int) {
        this.colorCode = colorCode
        this.videoDuration = videoDuration
        fullScreenIBtn.setOnClickListener(this)
        videoProgressBar.visibility = View.VISIBLE
        exoplayerController.visibility = View.GONE
        setThemeColorInExoPlayer()
    }

    /***
     *
     * @param videoUrl
     */
    private fun initializePlayer(videoUrl: String?) {
        try {
            ////checkPercentageView()
            this.videoUrl = videoUrl
            /*DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(dbApplication);
            renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON);
            renderersFactory.setMediaCodecSelector(MediaCodecSelector.DEFAULT);*/
            player = ExoPlayerFactory.newSimpleInstance(videoApp)
            //, renderersFactory, null, new ExoLoadControl());
            playerView.player = player
            playerView.setPlaybackPreparer(this)
            player.repeatMode = Player.REPEAT_MODE_OFF
            player.addListener(playerEventListener)
            player.playWhenReady = playWhenReady
            playExoPlayer(videoUrl, false)
        } catch (e: java.lang.Exception) {
            Log.e(TAG, e.message + "")
        }
    }


    /***
     *
     * @param colorCode
     */
    private fun setThemeColorInExoPlayer() {
        videoProgressBar.progressTintList = ColorStateList.valueOf(colorCode)
        defaultTimeBar.setPlayedColor(colorCode)
        durationTv.setTextColor(colorCode)
        positionTv.setTextColor(colorCode)
        try {
            ImageViewCompat.setImageTintList(exoPlayIv, ColorStateList.valueOf(colorCode))
            ImageViewCompat.setImageTintList(exoPauseIv, ColorStateList.valueOf(colorCode))
            ImageViewCompat.setImageTintList(fullScreenIBtn, ColorStateList.valueOf(colorCode))
        } catch (e: Exception) {
            Log.e(TAG, e.message + "")
        }
    }

    /***
     *
     * @param videoUrl
     */
    private fun playVideoUrlInExoPlayer(videoUrl: String?) {
        try {
            videoUrl?.let {
                this.videoUrl = it
                player.stop()
                currentWindow = player.currentWindowIndex
                playbackPosition = player.currentPosition
                player.seekTo(currentWindow, playbackPosition)
                Log.d(TAG, "playVideoUrlInExoPlayer: $videoUrl")
                val uri = Uri.parse(videoUrl)
                val mediaSource: MediaSource = buildMediaSource(uri)
                val haveStartPosition = currentWindow != C.INDEX_UNSET
                if (haveStartPosition) {
                    player.seekTo(currentWindow, playbackPosition)
                }
                player.prepare(mediaSource, !haveStartPosition, false)
            }
        } catch (e: java.lang.Exception) {
            Log.e(TAG, e.message + "")
        }
    }

    /**
     *
     * @param videoUrl String
     * @param isResetPlayer Boolean
     */
    fun playVideoUrlInExoPlayer(videoUrl: String, isResetPlayer: Boolean) {
        Log.d(TAG, "playVideoUrlInExoPlayer()")
        try {
            this.videoUrl = videoUrl
            mIsVideoStartedTracked = false
            mIsPlayed25PercTracked = false
            player.stop()
            clearStartPosition()
            player.seekTo(currentWindow, playbackPosition)
            playExoPlayer(videoUrl, isResetPlayer)
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "playVideoUrlInExoPlayer()" + e.message)
        }
    }

    /**
     * @param videoUrl
     * @param isResetPlayer
     */
    private fun playExoPlayer(videoUrl: String?, isResetPlayer: Boolean) {
        Log.d(
            TAG, "playExoPlayer: currentWindow$currentWindow playbackPosition: $playbackPosition"
        )
        player.seekTo(currentWindow, playbackPosition)
        if (!TextUtils.isEmpty(videoUrl)) {
            Log.d(TAG, "playExoPlayer: $videoUrl")
            val uri = Uri.parse(videoUrl)
            val mediaSource: MediaSource = buildMediaSource(uri)
            /*boolean haveStartPosition = (currentWindow != C.INDEX_UNSET);
            if (haveStartPosition) {
                player.seekTo(currentWindow, playbackPosition);
            }*/if (isResetPlayer) {
                player.prepare(mediaSource, isResetPlayer, false)
            } else {
                val isReset = currentWindow == 0 && playbackPosition == 0L
                player.prepare(mediaSource, isReset, false)
            }
            /** Add this to Start Video when last video is in Pause State  */
            player.playWhenReady = true
            /** Add this to Start Video when last video is in Pause State  */
            setThemeColorInExoPlayer()
        }
    }

    /***
     *
     * @param uri
     * @return
     */
    private fun buildMediaSource(uri: Uri): MediaSource {
        @C.ContentType val type = Util.inferContentType(uri)
        return when (type) {
            C.TYPE_DASH -> {
                Log.d(TAG, "buildMediaSource: TYPE = TYPE_DASH")
                DashMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
            }
            C.TYPE_SS -> {
                Log.d(TAG, "buildMediaSource: TYPE = TYPE_SS")
                SsMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
            }
            C.TYPE_HLS -> {
                Log.d(TAG, "buildMediaSource: TYPE = TYPE_HLS")
                HlsMediaSource.Factory(dataSourceFactory).setAllowChunklessPreparation(true)
                    .createMediaSource(uri)
            }
            C.TYPE_OTHER -> {
                Log.d(TAG, "buildMediaSource: TYPE = TYPE_OTHER")
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
            }
            else -> {
                Log.d(TAG, "buildMediaSource: TYPE = Unsupported type$type")
                throw IllegalStateException("Unsupported type: $type")
            }
        }
    }

    fun hideExoPlayerView() {
        playerView?.visibility = View.INVISIBLE
    }

    fun showExoPlayerView() {
        playerView?.visibility = View.VISIBLE
    }

    /**
     * @param currentOrientation
     */
    fun onConfigurationChanged(currentOrientation: Int) {
        Log.d(TAG, "onConfigurationChanged(currentOrientation)$currentOrientation")
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUiFullScreen()
            fullScreenIBtn.setImageResource(R.drawable.ic_video_small)
            val decorView = coreActivity.window.decorView
            val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
            decorView.systemUiVisibility = uiOptions
        } else {
            hideSystemUi()
            fullScreenIBtn.setImageResource(R.drawable.ic_video_full)
            val decorView = coreActivity.window.decorView
            val uiOptions = View.SYSTEM_UI_FLAG_VISIBLE
            decorView.systemUiVisibility = uiOptions
        }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        /*playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );*/
        playerView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUiFullScreen() {
        playerView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private fun releasePlayer() {
        try {
            playbackPosition = player?.currentPosition
            currentWindow = player?.currentWindowIndex
            playWhenReady = player?.playWhenReady
            //player.removeListener(componentListener);
            player?.release()
        } catch (e: java.lang.Exception) {
            Log.e(TAG, e.message + "")
        }
    }

    private fun removePlayer() {
        try {
            player?.let {
                /*clearHandler()*/
                try {
                    exoPauseIv.performClick()
                } catch (e: java.lang.Exception) {
                }
                try {
                    it.playWhenReady = false
                    it.stop(true)
                    it.seekTo(0)
                } catch (e: Exception) {
                    Log.e(TAG, e.message + "")
                }
                it.removeListener(playerEventListener)
                it.release()
            }
        } catch (e: java.lang.Exception) {
            Log.e(TAG, e.message + "")
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.exo_full_screen -> exoVideoPlayerInterface.onFullScreenBtnTapped()
        }
    }

    fun getVideoPlayedPercentage(): Int {
        val position = player?.currentPosition
        val duration = player?.duration
        return (position * 100 / duration).toInt()
    }


    private fun checkAndStopAudioMediaService() {
        // Here you can stop your other Media Services to play this video
    }


    fun pauseVideo() {
        exoPauseIv.performClick()
    }

    private val playerEventListener: Player.EventListener = object : Player.EventListener {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            var stateString: String
            when (playbackState) {
                Player.STATE_IDLE -> {
                    stateString = "ExoPlayer.STATE_IDLE      -"
                    videoProgressBar.visibility = View.GONE
                    exoplayerController.visibility = View.VISIBLE
                }
                Player.STATE_BUFFERING -> {
                    stateString = "ExoPlayer.STATE_BUFFERING -"
                    videoProgressBar.visibility = View.VISIBLE
                    exoplayerController.visibility = View.GONE
                }
                Player.STATE_READY -> {
                    stateString = "ExoPlayer.STATE_READY -"
                    videoProgressBar.visibility = View.GONE
                    exoplayerController.visibility = View.VISIBLE
                    checkAndStopAudioMediaService()
                    showExoPlayerView()
                    exoVideoPlayerInterface.onVideoPlaybackReady()
                }
                Player.STATE_ENDED -> {
                    stateString = "ExoPlayer.STATE_ENDED     -"
                    videoProgressBar.visibility = View.GONE
                    exoplayerController.visibility = View.VISIBLE
                    videoProgressBar.visibility = View.GONE
                    exoVideoPlayerInterface.onVideoPlaybackEnded()
                }
                else -> {
                    stateString = "UNKNOWN_STATE             -"
                    videoProgressBar.visibility = View.GONE
                    exoplayerController.visibility = View.VISIBLE
                    videoProgressBar.visibility = View.GONE
                }
            }
            Log.d(TAG, "onPlayerStateChanged()" + stateString + " playWhenReady: " + playWhenReady)
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            Log.d(TAG, "onPlayerError()" + error.message)
            Log.d(TAG, "onPlayerError() playedUrl: $videoUrl")
        }
    }

    private val playBackReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(
                TAG, "playBackReceiver onReceive()"
            )
            val playbackState =
                intent.getIntExtra(Utils.KEY_AUDIO_PLAYBACK_EVENT, -1)
            Log.d(
                TAG, "playBackReceiver playbackState: $playbackState"
            )
            when (playbackState) {
                PlaybackState.STATE_PLAYING -> pauseVideo()
                PlaybackState.STATE_PAUSED -> try {
                    playerView.showController()
                    player.playWhenReady = true
                } catch (e: Exception) {
                    Log.e(
                        TAG, "playBackReceiver onReceive()" + e.message
                    )
                }
                PlaybackState.STATE_STOPPED -> {
                }
            }
        }
    }

    fun setPlayWhenReady(playWhenReady: Boolean) {
        this.playWhenReady = playWhenReady
    }

    /***
     *
     * @param videoUrl
     */
    fun onActivityCreate(videoUrl: String) {
        this.videoUrl = videoUrl
    }

    fun onActivityStart() {
        Log.d(TAG, "onActivityStart()")
        if (Util.SDK_INT > 23) {
            initializePlayer(videoUrl!!)
            //            loadDashFile();
            playerView?.onResume()
        }
    }

    fun onActivityResume() {
        Log.d(TAG, "onActivityResume()")
        hideSystemUi()
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer(videoUrl!!)
            //            loadDashFile();
            playerView?.onResume()
        }
        val filter = IntentFilter(Utils.ACTION_BROADCAST_PLAYBACK_CONTROL)

        LocalBroadcastManager.getInstance(videoApp)
            .registerReceiver(playBackReceiver, filter)
    }

    fun onActivityPause() {
        Log.d(TAG, "onActivityPause()")
        if (Util.SDK_INT <= 23) {
            playerView?.onPause()
            releasePlayer()
        }
        LocalBroadcastManager.getInstance(videoApp)
            .unregisterReceiver(playBackReceiver)
    }

    fun onActivityStop() {
        Log.d(TAG, "onActivityStop()")
        if (Util.SDK_INT > 23) {
            playerView?.onPause()
            releasePlayer()
        }
    }

    fun onActivityDestroy() {
        Log.d(TAG, "onActivityDestroy()")
        try {
        } catch (e: java.lang.Exception) {
            Log.e(TAG, e.message + "")
        }
    }

    fun onBackPressed() {
        try {
            Log.d(TAG, "onBackPressed()")
            //releasePlayer();
            isBackPressed = true
            removePlayer()
        } catch (e: java.lang.Exception) {
            Log.e(TAG, e.message + "")
        }
    }

    private fun updateStartPosition() {
        if (player != null) {
            playWhenReady = player.playWhenReady
            currentWindow = player.currentWindowIndex
            playbackPosition = Math.max(0, player.contentPosition)
        }
    }

    /***
     *
     * @param outState
     */
    fun onActivitySaveInstanceState(outState: Bundle) {
        updateStartPosition()
        outState.putBoolean(KEY_AUTO_PLAY, playWhenReady)
        outState.putInt(KEY_WINDOW, currentWindow)
        outState.putLong(KEY_POSITION, playbackPosition)
    }

    /***
     *
     * @param savedInstanceState
     */
    fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            playWhenReady = savedInstanceState.getBoolean(KEY_AUTO_PLAY)
            currentWindow = savedInstanceState.getInt(KEY_WINDOW)
            playbackPosition = savedInstanceState.getLong(KEY_POSITION)
        }
    }

    fun dispatchActivityKeyEvent(event: KeyEvent?): Boolean {
        return playerView?.dispatchKeyEvent(event)
    }

    /***
     *
     * @param savedInstanceState
     */
    fun onActivityCreated(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            playWhenReady = savedInstanceState!!.getBoolean(KEY_AUTO_PLAY)
            currentWindow = savedInstanceState!!.getInt(KEY_WINDOW)
            playbackPosition = savedInstanceState!!.getLong(KEY_POSITION)
        }
        detectActivityCurrentOrientation()
    }

    private fun detectActivityCurrentOrientation() {
        try {
            val currentOrientation = coreActivity.resources.configuration.orientation
            Log.d(TAG, "detectActivityCurrentOrientation(currentOrientation)$currentOrientation")
            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                onConfigurationChanged(currentOrientation)
                val decorView = coreActivity.window.decorView
                val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                decorView.systemUiVisibility = uiOptions
            }
        } catch (e: Exception) {
            Log.e(TAG, "detectActivityCurrentOrientation()".plus(e.message))
        }
    }

    /***
     *
     * @param playWhenReady
     * @param playbackPosition
     */
    fun setPlayBackPosition(playWhenReady: Boolean, playbackPosition: Long) {
        this.playWhenReady = playWhenReady
        this.playbackPosition = playbackPosition
    }

    fun onActivityNewIntent(intent: Intent?) {
        releasePlayer()
        clearStartPosition()
    }

    override fun preparePlayback() {
        initializePlayer(videoUrl)
    }

    private fun clearStartPosition() {
        playWhenReady = true
        /*currentWindow = C.INDEX_UNSET;
        playbackPosition = C.TIME_UNSET;*/
        currentWindow = 0
        playbackPosition = 0
    }
}