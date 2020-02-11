package com.android.videogallery.views

import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.videogallery.R
import com.android.videogallery.Utils.Companion.KEY_EXO_PLAYER_POSITION
import com.android.videogallery.Utils.Companion.VIDEO_URL
import com.android.videogallery.activities.ExoVideoPlayerActivity.Companion.startExoPlayerActivity
import com.android.videogallery.models.VideoGallery
import com.android.videogallery.viewitems.ExoPlayerRecyclerViewItem.ExoPlayerRvHolder
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.util.*

class ExoPlayerRecyclerView : RecyclerView {
    private var videoApp: Context? = null
    private var videoSurfaceDefaultHeight = 0
    private var screenDefaultHeight = 0
    private var playPosition = -1
    private var isVideoViewAdded = false
    private var mediaCoverImage: ImageView? = null
    private var volumeControl: ImageView? = null
    private var pauseControl: ImageView? = null
    private var fullScreenControl: ImageView? = null
    private var videoSurfaceView: PlayerView? = null
    private var videoPlayer: SimpleExoPlayer? = null
    private var viewHolderParent: View? = null
    private var videoProgressBar: ProgressBar? = null
    private var mediaContainer: FrameLayout? = null
    private var exoControlsContainer: FrameLayout? = null
    private var isVolumeMute = false
    private var mediaObjects = ArrayList<VideoGallery>()

    private fun init(context: Context) {
        videoApp = context.applicationContext
        val display = (Objects.requireNonNull(
            context.getSystemService(Context.WINDOW_SERVICE)
        ) as WindowManager).defaultDisplay
        val point = Point()
        display.getSize(point)
        videoSurfaceDefaultHeight = point.x
        screenDefaultHeight = point.y
        videoSurfaceView =
            LayoutInflater.from(context).inflate(R.layout.layout_video_listing_exo_player, null) as PlayerView
        exoControlsContainer = videoSurfaceView!!.findViewById(R.id.exoplayer_controller)
        fullScreenControl = videoSurfaceView!!.findViewById(R.id.exo_full_screen)
        volumeControl = videoSurfaceView!!.findViewById(R.id.exo_volume)
        pauseControl = videoSurfaceView!!.findViewById(R.id.exo_pause)
        setVideoControlState()
        volumeControl?.setOnClickListener(exoControlsClickListener)
        fullScreenControl?.setOnClickListener(exoControlsClickListener)
        ////videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
//Create the player using ExoPlayerFactory
        videoPlayer = ExoPlayerFactory.newSimpleInstance(videoApp)
        // Disable Player Control
        videoSurfaceView!!.useController = true
        videoSurfaceView!!.controllerAutoShow = true
        // Bind the player to the view.
        videoSurfaceView!!.player = videoPlayer
        //setVolumeControl(VolumeState.ON);
        addOnScrollListener(onRecyclerViewScrollListener)
        addOnChildAttachStateChangeListener(onRvChildViewAttachStateChangeListener)
        videoPlayer?.addListener(exoPlayerEventsListener)
    }

    private val onRecyclerViewScrollListener: OnScrollListener = object : OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == SCROLL_STATE_IDLE) {

                mediaCoverImage?.let {
                    it.visibility = View.VISIBLE
                }

                // There's a special case when the end of the list has been reached.
                // Need to handle that with this bit of logic
                if (!recyclerView.canScrollVertically(1)) {
                    playVideo(true)
                } else {
                    playVideo(false)
                }
            }
        }
    }

    /**
     * @param isEndOfList
     */
    fun playVideo(isEndOfList: Boolean) {
        val targetPosition: Int
        if (!isEndOfList) {

            val lLayoutManager = layoutManager as LinearLayoutManager

            val startPosition = lLayoutManager.findFirstVisibleItemPosition()
            var endPosition = lLayoutManager.findLastVisibleItemPosition()
            // if there is more than 2 list-items on the screen, set the difference to be 1
            if (endPosition - startPosition > 1) {
                endPosition = startPosition + 1
            }
            // something is wrong. return.
            if (startPosition < 0 || endPosition < 0) {
                return
            }
            // if there is more than 1 list-item on the screen
            targetPosition = if (startPosition != endPosition) {
                val startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition)
                val endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition)
                if (startPositionVideoHeight > endPositionVideoHeight) startPosition else endPosition
            } else {
                startPosition
            }
        } else {
            targetPosition = mediaObjects.size - 1
        }
        Log.d(TAG, "playVideo: target position: $targetPosition")
        // video is already playing so return
        if (targetPosition == playPosition) {
            return
        }
        // set the position of the list-item that is to be played
        playPosition = targetPosition

        videoSurfaceView?.let {
            // remove any old surface views from previously playing videos
            it.visibility = View.INVISIBLE
            removeVideoView(it)
            val currentPosition = targetPosition - (Objects.requireNonNull(
                layoutManager
            ) as LinearLayoutManager).findFirstVisibleItemPosition()
            val child = getChildAt(currentPosition) ?: return
            val holder = child.tag as ExoPlayerRvHolder
            /*hol
            if (holder == null) {
                playPosition = -1
                return
            }*/
            mediaCoverImage = holder.mediaCoverImage
            viewHolderParent = holder.itemView
            mediaContainer = holder.mediaContainer
            videoProgressBar = holder.videoProgressBar
            videoSurfaceView!!.player = videoPlayer
            val defaultDataSourceFactory: DataSource.Factory =
                DefaultDataSourceFactory(
                    videoApp,
                    Util.getUserAgent(videoApp, videoApp!!.getString(R.string.app_name))
                )
            val videoGallery = mediaObjects[targetPosition]
            val mediaUrl = videoGallery.videoUrl
            if (!TextUtils.isEmpty(mediaUrl)) {
                val uri = Uri.parse(mediaUrl)
                val videoSource = buildMediaSource(defaultDataSourceFactory, uri)
                /*MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(mediaUrl));*/videoPlayer!!.prepare(videoSource)
                videoPlayer!!.playWhenReady = true
                fullScreenControl!!.tag = videoGallery
            } else {
                hideVideoProgressBar()
                onPausePlayer()
            }
        }
    }

    private val onRvChildViewAttachStateChangeListener: OnChildAttachStateChangeListener =
        object : OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {}
            override fun onChildViewDetachedFromWindow(view: View) {
                try {
                    hideVideoProgressBar()
                    if (isVideoViewAdded && videoSurfaceView != null && mediaCoverImage != null && viewHolderParent != null && viewHolderParent == view
                    ) {
                        removeVideoView(videoSurfaceView!!)
                        playPosition = -1
                        videoSurfaceView!!.visibility = View.INVISIBLE
                        mediaCoverImage!!.visibility = View.VISIBLE
                    }
                    Log.d(TAG, "onChildViewDetachedFromWindow()")
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "onChildViewDetachedFromWindow() " + e.message
                    )
                }
            }
        }

    private fun hideVideoProgressBar() {
        videoProgressBar?.visibility = View.GONE
    }

    private fun showVideoProgressBar() {
        videoProgressBar?.visibility = View.VISIBLE
    }

    private val exoPlayerEventsListener: Player.EventListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    Log.d(TAG, "onPlayerStateChanged: STATE_BUFFERING")
                    exoControlsContainer?.visibility = View.GONE
                    showVideoProgressBar()
                }
                Player.STATE_ENDED -> {
                    Log.d(TAG, "onPlayerStateChanged: STATE_ENDED")
                    exoControlsContainer?.visibility = View.VISIBLE
                    hideVideoProgressBar()
                    videoPlayer?.seekTo(0)
                }
                Player.STATE_IDLE -> {
                    Log.d(TAG, "onPlayerStateChanged: STATE_IDLE")
                    exoControlsContainer?.visibility = View.VISIBLE
                    hideVideoProgressBar()
                }
                Player.STATE_READY -> {
                    Log.d(TAG, "onPlayerStateChanged: STATE_READY")
                    exoControlsContainer?.visibility = View.VISIBLE

                    hideVideoProgressBar()
                    if (!isVideoViewAdded) {
                        addVideoView()
                    }
                    checkAndStopAudioMediaService()
                }
                else -> {
                    Log.d(TAG, "onPlayerStateChanged: STATE_default")
                    hideVideoProgressBar()
                    exoControlsContainer?.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun addVideoView() {
        try {
            mediaContainer?.addView(videoSurfaceView)
            isVideoViewAdded = true
            videoSurfaceView?.requestFocus()
            videoSurfaceView?.visibility = View.VISIBLE
            videoSurfaceView?.alpha = 1f
            mediaCoverImage?.visibility = View.GONE
        } catch (e: Exception) {
            Log.e(TAG, "addVideoView() " + e.message)
        }
    }
    //    /**
//     * @param videoView
//     */
//    private void removeVideoView(@NonNull PlayerView videoView) {
//        try {
//            ViewGroup parent = (ViewGroup) videoView.getParent();
//            if (parent == null) {
//                return;
//            }
//
//            int index = parent.indexOfChild(videoView);
//            if (index >= 0) {
//                parent.removeViewAt(index);
//                isVideoViewAdded = false;
//                viewHolderParent.setOnClickListener(null);
//            }
//        } catch (Exception e) {
//            Log.e(TAG, e.getMessage() + "");
//        }
//    }
    /**
     * @param videoView
     */
    private fun removeVideoView(videoView: PlayerView) {
        try {
            if (mediaContainer != null && viewHolderParent != null && videoView != null) {
                mediaContainer!!.removeView(videoView)
                isVideoViewAdded = false
                viewHolderParent!!.setOnClickListener(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message + "")
        }
    }

    /***
     *
     * @param playPosition
     * @return
     */
    private fun getVisibleVideoSurfaceHeight(playPosition: Int): Int {
        val at = playPosition - (Objects.requireNonNull(
            layoutManager
        ) as LinearLayoutManager).findFirstVisibleItemPosition()
        Log.d(TAG, "getVisibleVideoSurfaceHeight: at: $at")
        val child = getChildAt(at) ?: return 0
        val location = IntArray(2)
        child.getLocationInWindow(location)
        return if (location[1] < 0) {
            location[1] + videoSurfaceDefaultHeight
        } else {
            screenDefaultHeight - location[1]
        }
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context)
    }

    fun setMediaObjects(mediaObjects: ArrayList<VideoGallery>) {
        this.mediaObjects = mediaObjects
    }

    private val exoControlsClickListener =
        OnClickListener { v ->
            when (v.id) {
                R.id.exo_full_screen -> try {
                    val anyObject = v.tag
                    if (videoPlayer != null && anyObject is VideoGallery) {
                        val currentPosition = videoPlayer!!.currentPosition
                        showFullExoPlayerActivity(v.context, currentPosition, anyObject)
                    }
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "exoControlsClickListener: " + e.message
                    )
                }
                R.id.exo_volume -> try {
                    isVolumeMute = !isVolumeMute
                    setVolumeControl()
                    setVideoControlState()
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "exoControlsClickListener: " + e.message
                    )
                }
            }
        }

    /**
     *
     * @param context
     * @param currentPosition
     * @param videoGallery
     */
    private fun showFullExoPlayerActivity(
        context: Context,
        currentPosition: Long,
        videoGallery: VideoGallery
    ) {
        onPausePlayer()
        val bundle = Bundle()
        bundle.putString(VIDEO_URL, videoGallery.videoUrl)
        bundle.putLong(KEY_EXO_PLAYER_POSITION, currentPosition)
        startExoPlayerActivity(context, bundle)
    }

    private fun setVideoControlState() {
        if (isVolumeMute) {
            volumeControl!!.setImageResource(R.drawable.ic_volume_off)
        } else {
            volumeControl!!.setImageResource(R.drawable.ic_volume_on)
        }
    }

    private fun setVolumeControl() {
        if (videoPlayer != null) {
            if (isVolumeMute) {
                videoPlayer!!.volume = 0f
            } else {
                videoPlayer!!.volume = 0.5f
            }
        }
    }

    private fun buildMediaSource(
        dataSourceFactory: DataSource.Factory,
        uri: Uri
    ): MediaSource {
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
                ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
            }
            else -> {
                Log.d(
                    TAG,
                    "buildMediaSource: TYPE = Unsupported type$type"
                )
                throw IllegalStateException("Unsupported type: $type")
            }
        }
    }

    fun onPausePlayer() {
        pauseControl?.performClick()
    }

    fun onStopPlayer() {
        videoPlayer?.stop(true)
    }

    fun releasePlayer() {
        if (videoPlayer != null) {
            videoPlayer!!.release()
            videoPlayer = null
        }
        viewHolderParent = null
    }

    val currentVideoGallery: VideoGallery?
        get() = if (fullScreenControl!!.tag is VideoGallery) {
            fullScreenControl!!.tag as VideoGallery
        } else {
            null
        }

    val currentVideoPosition: Long
        get() = if (videoPlayer != null) {
            videoPlayer!!.currentPosition
        } else {
            0L
        }

    private fun checkAndStopAudioMediaService() { // Here you can stop your other Media Services to play this video
    }

    companion object {
        private val TAG = ExoPlayerRecyclerView::class.java.simpleName
    }
}