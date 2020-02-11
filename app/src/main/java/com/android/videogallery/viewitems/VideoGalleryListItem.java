package com.android.videogallery.viewitems;

import android.content.res.ColorStateList;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;

import com.android.videogallery.R;
import com.android.videogallery.Utils;
import com.android.videogallery.adapters.AdapterItem;
import com.android.videogallery.adapters.RecyclerAdapterNotifier;
import com.android.videogallery.adapters.RecyclerAdapterViewHolder;
import com.android.videogallery.models.VideoGallery;
import com.android.videogallery.providers.VideoGalleryUiProvider;

public final class VideoGalleryListItem extends AdapterItem<VideoGalleryListItem.Holder> {

    private VideoGallery videoGallery;
    private VideoGalleryUiProvider.OnVideoGalleryListItemClickListener onVideoGalleryListItemClickListener;
    private Utils.LayoutType layoutType;
    private static final String TAG = VideoGalleryListItem.class.getSimpleName();
    private int thumbWidth;
    private int colorCode;

    /***
     *
     * @param layoutType
     * @param thumbWidth
     * @param onVideoGalleryListItemClickListener
     */
    public VideoGalleryListItem(@NonNull int colorCode, @NonNull Utils.LayoutType layoutType, int thumbWidth,
                                @NonNull VideoGalleryUiProvider.OnVideoGalleryListItemClickListener onVideoGalleryListItemClickListener) {
        this.colorCode = colorCode;
        this.layoutType = layoutType;
        this.thumbWidth = thumbWidth;
        this.onVideoGalleryListItemClickListener = onVideoGalleryListItemClickListener;
    }

    @Override
    public int getLayoutId() {
        if (layoutType == Utils.LayoutType.VIDEO_GALLERY_RVL)
            return R.layout.item_video_gallery_list;
        else if (layoutType == Utils.LayoutType.VIDEO_GALLERY_RVG) {
            return R.layout.item_video_gallery_grid;
        } else if (layoutType == Utils.LayoutType.VIDEO_GALLERY_RVH)
            return R.layout.item_video_gallery_horizontal_fill;
        else
            return R.layout.item_video_gallery_list;
    }

    @Override
    public VideoGallery getData() {
        return videoGallery;
    }

    @Override
    public void setData(Object obj) {
        videoGallery = (VideoGallery) obj;
    }

    @Override
    protected void bindData(Holder holder, Object data, int position) {
        holder.setData(colorCode, videoGallery, layoutType, thumbWidth);
        holder.itemView.setOnClickListener(clickListener);
    }

    private final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (onVideoGalleryListItemClickListener != null)
                onVideoGalleryListItemClickListener.onVideoGalleryListItemClicked(VideoGalleryListItem.this);
        }
    };

    @Override
    protected void onViewRecycled(Holder holder) {
    }

    public static class Holder extends RecyclerAdapterViewHolder {

        ImageView thumbIv;

        TextView slugTitleTv;

        TextView videoDurationTv;

        ImageView playIconIv;

        ViewGroup playDurationParentLayout;

        public Holder(View view, RecyclerAdapterNotifier adapter) {
            super(view, adapter);
            slugTitleTv = view.findViewById(R.id.tv_video_slug_title);
            thumbIv = view.findViewById(R.id.iv_video_thumb);
            slugTitleTv = view.findViewById(R.id.tv_video_slug_title);
            videoDurationTv = view.findViewById(R.id.tv_video_duration);
            playIconIv = view.findViewById(R.id.iv_play_small_icon);
            playDurationParentLayout = view.findViewById(R.id.ll_play_duration);
        }

        public void setData(int colorCode, VideoGallery videoGallery, Utils.LayoutType layoutType, int thumbWidth) {
            try {
                if (videoGallery == null) return;
                setSlugTitle(videoGallery);
                setThumbImage(videoGallery, layoutType, thumbWidth);

                try {
                    if (layoutType == Utils.LayoutType.VIDEO_GALLERY_RVL) {
                        ImageViewCompat.setImageTintList(playIconIv, ColorStateList.valueOf(colorCode));
                    } else if (layoutType == Utils.LayoutType.VIDEO_GALLERY_RVG ||
                            layoutType == Utils.LayoutType.VIDEO_GALLERY_RVH && playDurationParentLayout != null) {
                        playDurationParentLayout.setBackgroundColor(colorCode);
                    }
                } catch (Exception e) {

                }

                String duration = videoGallery.getVideoDuration();
                if (!TextUtils.isEmpty(duration)) {
                    videoDurationTv.setVisibility(View.VISIBLE);
                    videoDurationTv.setText(duration);
                } else {
                    videoDurationTv.setVisibility(View.GONE);

                    /*CommonUtils.getMediaDuration(videoGallery.getVideoUrl(), new MediaDurationListener() {
                        @Override
                        public void onMediaDurationFound(String videoUrl, long duration) {
                            try {
                                if (duration != 0) {
                                    videoDurationTv.setVisibility(View.VISIBLE);
                                    videoGallery.setDuration(duration);
                                    videoDurationTv.setText(CommonUtils.getHoursMinutesSecondsString(duration));
                                } else {
                                    videoDurationTv.setVisibility(View.GONE);
                                }
                                Log.d(TAG, "onMediaDurationFound: " + duration);
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage() + "");
                            } catch (Error e) {
                                Log.e(TAG, e.getMessage() + "");
                            }
                        }
                    });*/
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage() + "");
            }
        }

        /***
         *
         * @param videoGallery
         */
        private void setThumbImage(VideoGallery videoGallery, Utils.LayoutType layoutType, int thumbWidth) {

            String videoUrl = videoGallery.getImageUrl();

            if (layoutType == Utils.LayoutType.VIDEO_GALLERY_RVL) {
                Utils.Companion.loadImage(videoUrl,
                        thumbIv, R.drawable.ic_placeholder_4_3);
            } else if (layoutType == Utils.LayoutType.VIDEO_GALLERY_RVG) {
                videoUrl = !TextUtils.isEmpty(videoGallery.getSquareImageUrl()) ? videoGallery.getSquareImageUrl() : videoUrl;
                Utils.Companion.loadImage(videoUrl,
                        thumbIv, R.drawable.ic_place_holder);
                thumbIv.setScaleType(ImageView.ScaleType.FIT_XY);
            } else if (layoutType == Utils.LayoutType.VIDEO_GALLERY_RVH) {
                thumbIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Utils.Companion.loadImage(videoUrl,
                        thumbIv, R.drawable.ic_placeholder_4_2,
                        thumbWidth, thumbWidth / 2);
            } else {
                Utils.Companion.loadImage(videoUrl,
                        thumbIv, R.drawable.ic_placeholder_4_3);
            }
        }

        /***
         *
         * @param videoGallery
         */
        private void setSlugTitle(VideoGallery videoGallery) {
            String heading = videoGallery.getTitle();
            slugTitleTv.setText(Html.fromHtml(heading));
        }
    }
}
