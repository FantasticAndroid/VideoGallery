//package com.android.videogallery.viewitems
//
//import android.content.res.ColorStateList
//import android.text.Html
//import android.text.TextUtils
//import android.util.Log
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.core.widget.ImageViewCompat
//import com.android.videogallery.R
//import com.android.videogallery.Utils.Companion.loadImage
//import com.android.videogallery.Utils.LayoutType
//import com.android.videogallery.adapters.AdapterItem
//import com.android.videogallery.adapters.RecyclerAdapterNotifier
//import com.android.videogallery.adapters.RecyclerAdapterViewHolder
//import com.android.videogallery.models.VideoGallery
//import com.android.videogallery.providers.VideoGalleryUiProvider.OnVideoGalleryListItemClickListener
//
//class VideoGalleryListItem(
//        private val layoutType: LayoutType, private val thumbWidth: Int,
//        private val onVideoGalleryListItemClickListener: OnVideoGalleryListItemClickListener
//) : AdapterItem<VideoGalleryListItem.Companion.Holder>() {
//
//    private var videoGallery: VideoGallery? = null
//
//    override fun getLayoutId(): Int {
//        return when {
//            layoutType === LayoutType.VIDEO_GALLERY_RVL -> R.layout.item_video_gallery_list
//            layoutType === LayoutType.VIDEO_GALLERY_RVG -> {
//                R.layout.item_video_gallery_grid
//            }
//            layoutType === LayoutType.VIDEO_GALLERY_RVH -> R.layout.item_video_gallery_horizontal_fill
//            else -> R.layout.item_video_gallery_list
//        }
//    }
//
//    override fun getData(): VideoGallery {
//        return videoGallery!!
//    }
//
//    override fun setData(obj: Any) {
//        videoGallery = obj as VideoGallery
//    }
//
//    override fun bindData(holder: Holder?, data: Any?, position: Int) {
//        holder?.setData(videoGallery, layoutType, thumbWidth)
//        holder?.itemView?.setOnClickListener(clickListener)
//    }
//
//    private val clickListener =
//            View.OnClickListener {
//                onVideoGalleryListItemClickListener?.onVideoGalleryListItemClicked(
//                        this@VideoGalleryListItem
//                )
//            }
//
//    override fun onViewRecycled(holder: Holder?) {
//
//    }
//
//
//    companion object {
//        private val TAG = VideoGalleryListItem::class.java.simpleName
//
//        open class Holder(view: View, adapter: RecyclerAdapterNotifier?) :
//                RecyclerAdapterViewHolder(view, adapter) {
//
//            private val thumbIv: ImageView = view.findViewById(R.id.iv_video_thumb)
//            private val slugTitleTv: TextView = view.findViewById(R.id.tv_video_slug_title)
//            private val videoDurationTv: TextView = view.findViewById(R.id.tv_video_duration)
//            private val playIconIv: ImageView = view.findViewById(R.id.iv_play_small_icon)
//            private val playDurationParentLayout: ViewGroup? = view.findViewById(R.id.ll_play_duration)
//
//            fun setData(videoGallery: VideoGallery?, layoutType: LayoutType, thumbWidth: Int) {
//                try {
//                    if (videoGallery == null) return
//                    setSlugTitle(videoGallery)
//                    setThumbImage(videoGallery, layoutType, thumbWidth)
//                    try {
//                        if (layoutType === LayoutType.VIDEO_GALLERY_RVL) {
//                            ImageViewCompat.setImageTintList(
//                                    playIconIv,
//                                    ColorStateList.valueOf(videoGallery.catColorCode)
//                            )
//                        } else if (layoutType === LayoutType.VIDEO_GALLERY_RVG ||
//                                layoutType === LayoutType.VIDEO_GALLERY_RVH && playDurationParentLayout != null
//                        ) {
//                            playDurationParentLayout!!.setBackgroundColor(videoGallery.catColorCode)
//                        }
//                    } catch (e: Exception) {
//                    }
//                    val duration = videoGallery.videoDuration
//                    if (!TextUtils.isEmpty(duration)) {
//                        videoDurationTv.visibility = View.VISIBLE
//                        videoDurationTv.text = duration
//                    } else {
//                        videoDurationTv.visibility = View.GONE
//                        /*CommonUtils.getMediaDuration(videoGallery.getVideoUrl(), new MediaDurationListener() {
//                            @Override
//                            public void onMediaDurationFound(String videoUrl, long duration) {
//                                try {
//                                    if (duration != 0) {
//                                        videoDurationTv.setVisibility(View.VISIBLE);
//                                        videoGallery.setDuration(duration);
//                                        videoDurationTv.setText(CommonUtils.getHoursMinutesSecondsString(duration));
//                                    } else {
//                                        videoDurationTv.setVisibility(View.GONE);
//                                    }
//                                    Log.d(TAG, "onMediaDurationFound: " + duration);
//                                } catch (Exception e) {
//                                    Log.e(TAG, e.getMessage() + "");
//                                } catch (Error e) {
//                                    Log.e(TAG, e.getMessage() + "");
//                                }
//                            }
//                        });*/
//                    }
//                } catch (e: Exception) {
//                    Log.e(TAG, e.message + "")
//                }
//            }
//
//            /***
//             *
//             * @param videoGallery
//             */
//            private fun setThumbImage(videoGallery: VideoGallery, layoutType: LayoutType, thumbWidth: Int) {
//                var videoUrl = videoGallery.imageUrl
//                if (layoutType === LayoutType.VIDEO_GALLERY_RVL) {
//                    loadImage(
//                            videoUrl!!,
//                            thumbIv, R.drawable.ic_placeholder_4_3
//                    )
//                } else if (layoutType === LayoutType.VIDEO_GALLERY_RVG) {
//                    videoUrl =
//                            if (!TextUtils.isEmpty(videoGallery.squareImageUrl)) videoGallery.squareImageUrl else videoUrl
//                    loadImage(
//                            videoUrl!!,
//                            thumbIv, R.drawable.ic_place_holder
//                    )
//                    thumbIv.scaleType = ImageView.ScaleType.FIT_XY
//                } else if (layoutType === LayoutType.VIDEO_GALLERY_RVH) {
//                    thumbIv.scaleType = ImageView.ScaleType.CENTER_CROP
//                    loadImage(
//                            videoUrl!!,
//                            thumbIv, R.drawable.ic_placeholder_4_2,
//                            thumbWidth, thumbWidth / 2
//                    )
//                } else {
//                    loadImage(
//                            videoUrl!!,
//                            thumbIv, R.drawable.ic_placeholder_4_3
//                    )
//                }
//            }
//
//            /***
//             *
//             * @param videoGallery
//             */
//            private fun setSlugTitle(videoGallery: VideoGallery) {
//                val heading = videoGallery.title
//                slugTitleTv.text = Html.fromHtml(heading)
//            }
//        }
//    }
//}