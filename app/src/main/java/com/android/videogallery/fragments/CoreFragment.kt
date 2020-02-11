package com.android.videogallery.fragments

import android.content.Context
import androidx.fragment.app.Fragment
import com.android.VideoApp
import com.android.videogallery.activities.CoreActivity

abstract class CoreFragment : Fragment() {

    protected lateinit var videoApp:VideoApp
    protected  lateinit var coreActivity: CoreActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        videoApp = context.applicationContext as VideoApp
        coreActivity = context as CoreActivity
    }
}