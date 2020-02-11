package com.android.videogallery

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.widget.ImageView
import com.android.videogallery.providers.ExoVideoPlayerProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import java.nio.charset.Charset

class Utils {
    companion object {
        val KEY_AUDIO_PLAYBACK_EVENT = "key_audio_playback_event"
        //String KEY_IS_COMING_FROM_HAMBURGER_MENU = "key_is_coming_from_hamburger_menu";
        val ACTION_BROADCAST_PLAYBACK_CONTROL =
            BuildConfig.APPLICATION_ID + ExoVideoPlayerProvider::class.java.simpleName

        val VIDEO_URL = "video_url"
        val KEY_VIDEO_CURRENT_POSITION = "key_video_current_position"
        /*String KEY_LAYOUT_TYPE = "key_layout_type";*/
        val KEY_EXO_PLAYER_POSITION = "key_exo_player_position"
        val KEY_EXO_PLAYER_WINDOW = "key_exo_player_window"
        val KEY_VIDEO_DURATION = "key_video_duration"
        val KEY_IS_SCREEN_ORIENTATION_LOCKED = "key_screen_orientation_locked"

        val KEY_GALLERY_LAYOUT_TYPE = "key_gallery_layout_type"
        val KEY_GALLERY_LAYOUT_COLOR = "key_gallery_layout_color"

        /**
         * This method will be used to read json from file
         *
         * @param context
         * @param fileName
         */
        fun readJSONFromAssetFile(context: Context, fileName: String): String? {
            var json: String? = null
            try {
                val inputStream = context.assets.open(fileName)
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                json = String(buffer, Charset.forName("UTF-8"))
                Log.d("JSON", "Read JSON from file: $json")
            } catch (e: Exception) {
                Log.e("readJSON", "readJSON: " + e.message)
            }
            return json
        }

        fun loadImage(
            url: String, imageView: ImageView, placeholder: Int
        ) {
            try {
                val requestOptions: RequestOptions =
                    getRequestOptions(placeholder, placeholder, 0, 0)
                Glide.with(imageView)
                    .load(url)
                    .apply(requestOptions)
                    .into(imageView)
            } catch (e: java.lang.Exception) {
                Log.d("ImageUtil", "loadImage")
            }
        }

        fun loadImage(
            url: String, imageView: ImageView, placeholder: Int, width: Int, height: Int
        ) {
            try {
                val requestOptions: RequestOptions =
                    getRequestOptions(placeholder, placeholder, width, height)
                Glide.with(imageView)
                    .load(url)
                    .apply(requestOptions)
                    .into(imageView)
            } catch (e: java.lang.Exception) {
                Log.d("ImageUtil", "loadImage")
            }
        }

        private fun getRequestOptions(
            placeholder: Int,
            error: Int, width: Int, height: Int
        ): RequestOptions {
            val requestOptions = RequestOptions()
            requestOptions.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            requestOptions.skipMemoryCache(false)
            requestOptions.placeholder(placeholder)
            requestOptions.error(error)
            if (width > 0 && height > 0) {
                requestOptions.override(width, height)
            }
            return requestOptions
        }

        /**
         * @param activity
         * @return an array having width in 0 index and height in 1 index
         */
        fun getDeviceWidthAndHeight(activity: Activity): IntArray {
            val wh = IntArray(2)
            try {
                val displaymetrics = DisplayMetrics()
                activity.windowManager.defaultDisplay
                    .getMetrics(displaymetrics)
                wh[0] = displaymetrics.widthPixels
                wh[1] = displaymetrics.heightPixels
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return wh
        }
    }
    enum class LayoutType {
        VIDEO_GALLERY_RVL, VIDEO_GALLERY_RVG, VIDEO_GALLERY_RVH
    }
}