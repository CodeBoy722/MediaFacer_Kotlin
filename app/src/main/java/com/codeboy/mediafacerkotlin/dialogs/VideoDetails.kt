package com.codeboy.mediafacerkotlin.dialogs

import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codeboy.mediafacer.models.VideoContent
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.FragmentVideoDetailsBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class VideoDetails() : Fragment() {

    lateinit var bindings: FragmentVideoDetailsBinding
    lateinit var videoItem: VideoContent
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    constructor(videoItem: VideoContent):this(){
        this.videoItem = videoItem
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_video_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentVideoDetailsBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner

        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        //setting Peek at the 16:9 ratio key line of its parent.
        bottomSheetBehavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
        view.minimumHeight = (Resources.getSystem().displayMetrics.heightPixels)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

    }
}