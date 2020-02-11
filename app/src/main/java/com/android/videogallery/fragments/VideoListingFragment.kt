package com.android.videogallery.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.videogallery.R
import com.android.videogallery.Utils
import com.android.videogallery.adapters.RecyclerAdapter
import com.android.videogallery.callbacks.OnVideoItemSelectionListener
import com.android.videogallery.models.VideoGallery
import com.android.videogallery.viewitems.ExoPlayerRecyclerViewItem
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_video_listing.*
import java.util.*

class VideoListingFragment : CoreFragment(), OnVideoItemSelectionListener {

    private var recyclerAdapter: RecyclerAdapter? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var deviceWidth: Int = 0
    private var firstTime = true
    private val videoGalleryArrayList = ArrayList<VideoGallery>()

    companion object {
        private val TAG = VideoListingFragment::class.java.simpleName
        fun newInstance(): VideoListingFragment {
            return VideoListingFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_video_listing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deviceWidth = Utils.getDeviceWidthAndHeight(coreActivity)[0]

        /*Drawable dividerDrawable = ContextCompat.getDrawable(dbApplication, R.drawable.drawable_divider_exo_player_rv);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(dbApplication, RecyclerView.VERTICAL);
        dividerItemDecoration.setDrawable(dividerDrawable);
        exoPlayerRv.addItemDecoration(dividerItemDecoration);
        exoPlayerRv.setItemAnimator(new DefaultItemAnimator());*/

        linearLayoutManager = LinearLayoutManager(videoApp, RecyclerView.VERTICAL, false)
        exoPlayerRv.layoutManager = linearLayoutManager

        /*val gestureDetector = GestureDetector(coreActivity, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                //return super.onFling(e1, e2, velocityX, velocityY)
                return true
            }
        })
        exoPlayerRv.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }*/

        setRecyclerAdapter()
        processData()
        onVideoListFetched(videoGalleryArrayList)
    }

    private fun playExoVideoAtFirst() {
        if (firstTime) {
            Handler(Looper.getMainLooper()).post { exoPlayerRv.playVideo(false) }
            firstTime = false
        }
    }

    private fun processData() {
        val videoJson = Utils.readJSONFromAssetFile(videoApp, "gallery.json")
        videoGalleryArrayList.clear()
        val typeToken = object : TypeToken<List<VideoGallery>>() {}.type
        videoGalleryArrayList.addAll(
            GsonBuilder().create().fromJson<List<VideoGallery>>(
                videoJson,
                typeToken
            )
        )
    }

    private fun setRecyclerAdapter() {
        exoPlayerRv.setMediaObjects(videoGalleryArrayList)
        recyclerAdapter = RecyclerAdapter()
        recyclerAdapter?.setItemAnimationType(RecyclerAdapter.ANIMATION_TYPE_LIST_ITEM)

        linearLayoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        exoPlayerRv.layoutManager = linearLayoutManager
        exoPlayerRv.adapter = recyclerAdapter
        exoPlayerRv.isNestedScrollingEnabled = false
    }

    /**
     * @param videoGallery
     */
    private fun launchVideoDetailScreen(videoGallery: VideoGallery) {
        try {
            val selectedIndex = videoGalleryArrayList.indexOf(videoGallery)
            val playerVideoGallery = exoPlayerRv.currentVideoGallery
            var currentPlayerPosition = 0L
            if (playerVideoGallery != null && videoGallery.id.equals(playerVideoGallery.id)) {
                currentPlayerPosition = exoPlayerRv.currentVideoPosition
            }
            /*AppNavigatorHelper.getInstance().launchExoVideoGalleryForSelectedCategory(
                mBaseActivity, videoGalleryArrayList, selectedIndex,
                videoGallery.catId, currentPlayerPosition
            )*/
        } catch (e: Exception) {
            Log.e(TAG, e.message + "")
        }
    }

    private fun onVideoListFetched(videoList: List<VideoGallery>?) {
        if (null != videoList && videoList.isNotEmpty()) {
            videoGalleryArrayList.addAll(videoList)
            for (i in videoList.indices) {
                val videoGallery = videoList[i]

                val exoPlayerRecyclerViewItem =
                    ExoPlayerRecyclerViewItem(videoApp, videoGallery, deviceWidth, this)
                recyclerAdapter?.add(exoPlayerRecyclerViewItem)
            }
            playExoVideoAtFirst()
        } else {
            Toast.makeText(videoApp, "No Data Available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        exoPlayerRv?.onPausePlayer()
    }

    override fun onDestroyView() {
        exoPlayerRv?.let {
            it.onStopPlayer()
            it.adapter = null
        }
        recyclerAdapter = null

        super.onDestroyView()
    }

    override fun onDestroy() {
        exoPlayerRv?.releasePlayer()
        super.onDestroy()
    }

    override fun onVideoItemSelected(videoGallery: VideoGallery) {
        Toast.makeText(videoApp, videoGallery.title + "", Toast.LENGTH_SHORT).show()
    }
}