package com.android.videogallery.providers

import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.videogallery.R
import com.android.videogallery.Utils
import com.android.videogallery.activities.CoreActivity
import com.android.videogallery.activities.VideoGalleryActivity
import com.android.videogallery.adapters.RecyclerAdapter
import com.android.videogallery.callbacks.ExoVideoPlayerInterface
import com.android.videogallery.models.VideoGallery
import com.android.videogallery.viewitems.VideoGalleryListItem
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.util.*

class VideoGalleryUiProvider(
    private val view: View,
    private val coreActivity: CoreActivity
) : View.OnClickListener {

    private val videoApp = coreActivity.videoApp

    private val recyclerView: RecyclerView? = view.findViewById(R.id.rv_video_list)

    private val nonVideoLayout: ViewGroup? = view.findViewById(R.id.fl_non_video_layout)

    private val videoLayout: FrameLayout? = view.findViewById(R.id.fl_video_layout)

    private val layoutIv: ImageView? =
        view.findViewById(R.id.layoutIv)

    private val slugTitleTv: TextView? = view.findViewById(R.id.tv_video_slug_title)

    private val viewMoreLessTv: TextView? = view.findViewById(R.id.tv_view_desc_more_less)

    private val descTv: TextView? = view.findViewById(R.id.tv_video_detail_desc)

    private val nestedScrollView: NestedScrollView? = view.findViewById(R.id.nsv_video_gallery)

    private val containerReadMoreShareBtns: ViewGroup? =
        view.findViewById(R.id.rl_read_more_share_btns)
    private val videoDetailLayout: ViewGroup? = view.findViewById(R.id.ll_video_gallery_detail)
    private val exoVideoGalleryContainer: ViewGroup? =
        view.findViewById(R.id.container_exo_video_gallery)

    private lateinit var currentVideoGallery: VideoGallery
    private var videoGalleryList: ArrayList<VideoGallery> = ArrayList<VideoGallery>()
    private var recyclerAdapter: RecyclerAdapter? = null
    private var videoLayoutParams: LinearLayout.LayoutParams? = null

    private var isScreenOrientationLocked: Boolean = false

    private var videoGalleryWeightSum = 0
    private var videoGalleryWeight: Int = 0

    private var exoVideoPlayerProvider: ExoVideoPlayerProvider? = null
    private var galleryRvLayoutManager: GridLayoutManager? = null
    private val TAG: String = VideoGalleryUiProvider::class.java.simpleName
    private val orientationHandler: Handler? = Handler()
    // Unused
    private var currentColorCode = Color.CYAN
    private var layoutType: Utils.LayoutType = Utils.LayoutType.VIDEO_GALLERY_RVL
    private val deviceWidth = 0

    fun initUi(
        layoutType: Utils.LayoutType,
        currentColorCode: Int,
        isScreenOrientationLocked: Boolean,
        savedInstanceState: Bundle?
    ) {
        this.currentColorCode = currentColorCode
        this.layoutType = layoutType
        this.isScreenOrientationLocked = isScreenOrientationLocked
        viewMoreLessTv?.setOnClickListener(this)
        layoutIv?.setOnClickListener(this)
        exoVideoGalleryContainer?.setBackgroundColor(
            ContextCompat.getColor(videoApp, R.color.back_theme_light)
        )

        videoLayoutParams = videoLayout!!.layoutParams as LinearLayout.LayoutParams
        ////implementCatColorOnUI(videoCatId)
        videoGalleryWeightSum =
            videoApp.resources.getInteger(R.integer.video_gallery_layout_weight_sum)
        videoGalleryWeight =
            videoApp.resources.getInteger(R.integer.video_gallery_layout_weight)

        recyclerAdapter = RecyclerAdapter()
        recyclerView?.adapter = recyclerAdapter
        setLayoutManager()
        exoVideoPlayerProvider = ExoVideoPlayerProvider(view, coreActivity, exoVideoPlayerInterface)
        exoVideoPlayerProvider?.initExoPlayerUI(currentColorCode, 0)
        readDataFromBundle()
        recyclerView?.layoutManager = galleryRvLayoutManager
        exoVideoPlayerProvider?.onActivityCreated(savedInstanceState)
        exoVideoPlayerProvider?.setPlayBackPosition(true, 0L)
        processUiAccordingToThemeType()
        processUiAccordingToMenuType()
        detectActivityCurrentOrientation()
    }

    private fun readDataFromBundle() {
        try {
            val videoJson = Utils.readJSONFromAssetFile(videoApp, "gallery.json")

            val typeToken = object : TypeToken<List<VideoGallery>>() {}.type
            videoGalleryList.addAll(
                GsonBuilder().create().fromJson<List<VideoGallery>>(
                    videoJson,
                    typeToken
                )
            )
            currentVideoGallery = videoGalleryList.removeAt(0)
            processDataOnVidoeGalleryDetailUI()
            processDataOnVideoGalleryListUI()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setVideoGalleyListOnRv() {
        recyclerAdapter?.clearAndRemove()
        for (videoGallery in videoGalleryList) {

            val videoGalleryListItem = VideoGalleryListItem(
                currentColorCode,
                layoutType, deviceWidth,
                onVideoGalleryListItemClickListener
            )
            //setCatColorCodeOnVideoGallery(videoGallery)
            videoGalleryListItem.setData(videoGallery)
            recyclerAdapter?.add(videoGalleryListItem)
        }
        //videoDetailListingLayout.setVisibility(View.VISIBLE);
        //videoPBarMsgLayout.setVisibility(View.GONE)
    }

    private fun processUiAccordingToThemeType() {
        slugTitleTv!!.setTextColor(Color.BLACK)
        descTv!!.setTextColor(ContextCompat.getColor(videoApp, android.R.color.darker_gray))
    }

    private fun processUiAccordingToMenuType() {
        viewMoreLessTv!!.text =
            Html.fromHtml("<U>" + videoApp.getString(R.string.label_aur_paden).toString() + "</U>")
        viewMoreLessTv.setCompoundDrawablesRelativeWithIntrinsicBounds(
            0,
            0,
            R.drawable.ic_arrow_down,
            0
        )
        containerReadMoreShareBtns!!.visibility = View.VISIBLE
        videoDetailLayout!!.visibility = View.VISIBLE
    }


    /***
     * @param videoUrl
     */
    private fun playVideoInExoPlayer(videoUrl: String) {
        exoVideoPlayerProvider?.playVideoUrlInExoPlayer(videoUrl, true)
    }

    private fun detectActivityCurrentOrientation() {
        val currentOrientation = coreActivity.resources.configuration.orientation
        Log.d(
            TAG, "detectActivityCurrentOrientation(currentOrientation)$currentOrientation"
        )
        setLayoutWithCurrentOrientation(currentOrientation)
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setLayoutWithCurrentOrientation(currentOrientation)
        }
    }

    /**
     * @param currentOrientation
     */
    private fun setLayoutWithCurrentOrientation(currentOrientation: Int) {
        try {
            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                videoLayoutParams!!.weight = videoGalleryWeightSum.toFloat()
                videoLayout!!.layoutParams = videoLayoutParams
                nonVideoLayout!!.visibility = View.GONE
            } else {
                videoLayoutParams!!.weight = videoGalleryWeight.toFloat()
                videoLayout!!.layoutParams = videoLayoutParams
                nonVideoLayout!!.visibility = View.VISIBLE
            }
        } catch (e: java.lang.Exception) {
            Log.e(TAG, e.message + "")
        }
    }

    private fun showVideoDetailLayout() {
        videoDetailLayout!!.visibility = View.VISIBLE
    }

    private fun hideVideoDetailLayout() {
        videoDetailLayout!!.visibility = View.GONE
    }

    private fun processDataOnVideoGalleryListUI() {
        setVideoGalleyListOnRv()
        nestedScrollView?.post { nestedScrollView.scrollTo(0, 0) }
    }

    private fun processDataOnVidoeGalleryDetailUI() {
        setVideoDetailOnDetailView()
        if (currentVideoGallery != null) {
            val videoUrl: String? = currentVideoGallery?.videoUrl
            videoUrl?.let { exoVideoPlayerProvider?.onActivityCreate(it) }
        }
    }

    private fun setVideoDetailOnDetailView() {
        currentVideoGallery?.let {

            val heading: String? = currentVideoGallery.title
            slugTitleTv?.text = Html.fromHtml(heading)

            val description: String? = it.description
            if (!TextUtils.isEmpty(description)) {
                descTv!!.text = Html.fromHtml(description)
                viewMoreLessTv!!.visibility = View.VISIBLE
            } else {
                descTv!!.text = ""
                viewMoreLessTv!!.visibility = View.INVISIBLE
            }
            descTv.visibility = View.GONE
            viewMoreLessTv.isSelected = false
            try {
                ImageViewCompat.setImageTintList(
                    layoutIv!!,
                    ColorStateList.valueOf(currentColorCode)
                )
            } catch (e: java.lang.Exception) {
            }
            viewMoreLessTv.text =
                Html.fromHtml("<U>" + videoApp.getString(R.string.label_aur_paden) + "</U>")
            viewMoreLessTv.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_arrow_down,
                0
            )
        }
    }

    fun clearAdapter() {
        recyclerAdapter?.clearAndRemove()
    }

    private fun setLayoutManager() {
        galleryRvLayoutManager = when {
            layoutType === Utils.LayoutType.VIDEO_GALLERY_RVL -> {
                GridLayoutManager(videoApp, 1)
            }
            layoutType === Utils.LayoutType.VIDEO_GALLERY_RVG -> {
                GridLayoutManager(videoApp, 2)
            }
            layoutType === Utils.LayoutType.VIDEO_GALLERY_RVH -> {
                GridLayoutManager(videoApp, 1)
            }
            else -> {
                GridLayoutManager(videoApp, 1)
            }
        }
    }


    /***
     *
     * @param catId
     */
    private fun implementCatColorOnUI(catId: String) {
        /*val videoCatColor: String = DbColorThemeHelper.getInstance().getCategoryColor(catId)
        val newColorCode: Int = CommonUtils.getColorCodeFromColorName(videoCatColor)
        if (newColorCode != currentColorCode) {
            currentColorCode = newColorCode
            videoGalleyListingPBar.setIndicatorColor(currentColorCode)
        }*/
    }

    fun doOnOrientationChanged() {
        try {
            val currentOrientation = coreActivity.resources.configuration.orientation
            setLayoutWithCurrentOrientation(currentOrientation)
            exoVideoPlayerProvider?.onConfigurationChanged(currentOrientation)
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "doOnOrientationChanged: " + e.message)
        }
    }

    fun getExoVideoPlayerProvider(): ExoVideoPlayerProvider? {
        return exoVideoPlayerProvider
    }

    fun onBackPressed(): Boolean {
        var isHandled = true
        val currentOrientation = coreActivity.resources.configuration.orientation
        val userOrientationLockStatus = Settings.System.getInt(
            coreActivity.contentResolver,
            Settings.System.ACCELEROMETER_ROTATION,
            1
        )
        Log.d(
            TAG,
            "userOrientationLockStatus" + if (userOrientationLockStatus == 1) "Unlocked" else "Locked"
        )
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (isScreenOrientationLocked) {
                coreActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                coreActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
                orientationHandler!!.postDelayed({
                    if (userOrientationLockStatus == 1) {
                        coreActivity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_FULL_USER
                    }
                }, 500)
            }
        } else {
            isHandled = false
        }
        return isHandled
    }

    /***
     *
     * @param selectedVideoGalleryListItem
     */
    private fun doOnVideoGalleryListItemClicked(selectedVideoGalleryListItem: VideoGalleryListItem) {
        val selectedVideoGallery: VideoGallery = selectedVideoGalleryListItem.data
        videoGalleryList.remove(selectedVideoGallery)
        recyclerAdapter?.removeItem(selectedVideoGalleryListItem)
        if (videoGalleryList.indexOf(currentVideoGallery) == -1) {
            videoGalleryList.add(currentVideoGallery)
            val videoGalleryListItem =
                VideoGalleryListItem(
                    currentColorCode,
                    layoutType,
                    deviceWidth,
                    onVideoGalleryListItemClickListener
                )
            videoGalleryListItem.data = currentVideoGallery

            recyclerAdapter?.add(videoGalleryListItem)
        }
        currentVideoGallery = selectedVideoGallery
        setVideoDetailOnDetailView()
        playVideoInExoPlayer(currentVideoGallery.videoUrl!!)
        try {
            nestedScrollView!!.post { nestedScrollView.scrollTo(0, 0) }
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "nestedScrollView.post: " + e.message)
        }
    }

    private val exoVideoPlayerInterface: ExoVideoPlayerInterface =
        object : ExoVideoPlayerInterface {
            override fun onFullScreenBtnTapped() {
                val currentOrientation =
                    coreActivity.resources.configuration.orientation
                if (isScreenOrientationLocked) {
                    if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                        coreActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    } else {
                        coreActivity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                } else {
                    val userOrientationLockStatus = Settings.System.getInt(
                        coreActivity.contentResolver,
                        Settings.System.ACCELEROMETER_ROTATION,
                        1
                    )
                    Log.d(
                        TAG,
                        "userOrientationLockStatus" + if (userOrientationLockStatus == 1) "Unlocked" else "Locked"
                    )
                    if (userOrientationLockStatus == 1) {
                        Log.d(TAG, "userOrientationLockStatus Unlocked")
                        // rotation is Unlocked
                        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                            coreActivity.requestedOrientation =
                                ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
                        } else {
                            coreActivity.requestedOrientation =
                                ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
                        }
                    } else {
                        Log.d(TAG, "userOrientationLockStatus Locked")
                        // rotation is Locked
                        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                            coreActivity.requestedOrientation =
                                ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
                        } else {
                            coreActivity.requestedOrientation =
                                ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
                        }
                    }
                    orientationHandler!!.postDelayed({
                        if (userOrientationLockStatus == 1) {
                            Log.d(TAG, "userOrientationLockStatus Unlocked")
                            // rotation is Unlocked
                            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                                coreActivity.requestedOrientation =
                                    ActivityInfo.SCREEN_ORIENTATION_FULL_USER
                            }
                        }
                    }, 500)
                }
            }

            override fun onVideoPlaybackEnded() {
                Log.d(TAG, "onVideoPlaybackEnded()")
                try {
                    if (!videoGalleryList.isNullOrEmpty()) {
                        doOnVideoGalleryListItemClicked(
                            (recyclerAdapter!!.getItemAtPosition(0)
                                    as VideoGalleryListItem)
                        )
                    }
                } catch (e: java.lang.Exception) {
                    Log.e(TAG, e.message + "")
                }
            }

            override fun onVideoPlaybackReady() {}
        }

    interface OnVideoGalleryListItemClickListener {
        fun onVideoGalleryListItemClicked(videoGalleryListItem: VideoGalleryListItem)
    }

    private val onVideoGalleryListItemClickListener: OnVideoGalleryListItemClickListener =
        object : OnVideoGalleryListItemClickListener {
            override fun onVideoGalleryListItemClicked(videoGalleryListItem: VideoGalleryListItem) {
                doOnVideoGalleryListItemClicked(videoGalleryListItem)
            }
        }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.layoutIv -> {


            }

            R.id.tv_view_desc_more_less -> {
                if (view.isSelected) {
                    descTv!!.visibility = View.GONE
                    viewMoreLessTv!!.text =
                        Html.fromHtml("<U>" + view.context.getString(R.string.label_aur_paden) + "</U>")
                    viewMoreLessTv.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_arrow_down,
                        0
                    )
                } else {
                    descTv!!.visibility = View.VISIBLE
                    viewMoreLessTv!!.text =
                        Html.fromHtml("<U>" + view.context.getString(R.string.label_band_karen) + "</U>")
                    viewMoreLessTv.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_arrow_up,
                        0
                    )
                }
                view.isSelected = !view.isSelected
            }
        }
    }

}