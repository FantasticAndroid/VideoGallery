package com.android.videogallery.viewitems

import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.android.VideoApp
import com.android.videogallery.R
import com.android.videogallery.Utils
import com.android.videogallery.adapters.AdapterItem
import com.android.videogallery.adapters.RecyclerAdapterNotifier
import com.android.videogallery.adapters.RecyclerAdapterViewHolder
import com.android.videogallery.callbacks.OnVideoItemSelectionListener
import com.android.videogallery.models.VideoGallery

class ExoPlayerRecyclerViewItem(val videoApp: VideoApp,private val videoGallery: VideoGallery,
                                private val deviceWidth: Int,
                                private val onVideoItemSelectionListener:OnVideoItemSelectionListener):
        AdapterItem<ExoPlayerRecyclerViewItem.ExoPlayerRvHolder>() {

    override fun getLayoutId(): Int {
        return R.layout.view_item_video_listing_exo_player
    }

    override fun getData(): Any? {
        return null
    }

    override fun setData(obj: Any?) {
    }

    override fun bindData(holder: ExoPlayerRvHolder, data: Any?, position: Int) {
        holder.mediaContainer.layoutParams.width = deviceWidth

        val height = (deviceWidth * 3) / 4

        holder.mediaContainer.layoutParams.height = height - height / 6

        holder.itemView.tag = holder
        holder.title.text = Html.fromHtml(videoGallery.title)

        if (!TextUtils.isEmpty(videoGallery.imageUrl)) {
            Utils.loadImage(videoGallery.imageUrl!!, holder.mediaCoverImage, R.mipmap.ic_launcher)
        }

        holder.title.setOnClickListener(View.OnClickListener {
            onVideoItemSelectionListener.onVideoItemSelected(videoGallery)
            /*RxEventUtils.sendEventWithDataAndFilter(holder.rxBus,
                    NavigationEvent.EVENT_VIDEO_ITEM_CLICKED, videoGallery, videoGallery.reponseFilter)*/
        })
    }

    override fun onViewRecycled(holder: ExoPlayerRvHolder?) {

    }

    class ExoPlayerRvHolder(itemView: View, adapter: RecyclerAdapterNotifier) : RecyclerAdapterViewHolder(itemView, adapter) {

        lateinit var mediaContainer: FrameLayout
        lateinit var title: TextView
        lateinit var videoProgressBar: ProgressBar
        lateinit var mediaCoverImage: ImageView

        init {
            mediaContainer = itemView.findViewById(R.id.mediaContainer)
            mediaCoverImage = itemView.findViewById(R.id.ivMediaCoverImage)
            videoProgressBar = itemView.findViewById(R.id.progressBar)
            title = itemView.findViewById(R.id.tvTitle)
        }
    }
}