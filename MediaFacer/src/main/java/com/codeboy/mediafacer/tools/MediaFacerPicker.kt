package com.codeboy.mediafacer.tools

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codeboy.mediafacer.R
import com.codeboy.mediafacer.databinding.FragmentMediaFacerPickerBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

 class MediaFacerPicker() : BottomSheetDialogFragment() {

    private lateinit var bindings: FragmentMediaFacerPickerBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private var addVideos = false
    private var addImages = false
    private var addAudios = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_media_facer_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentMediaFacerPickerBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner

        /* bottomSheetBehavior?.peekHeight = Resources.getSystem().displayMetrics.heightPixels
       bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED*/

        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View);
        //setting Peek at the 16:9 ratio key line of its parent.
        bottomSheetBehavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO;

        view.minimumHeight = (Resources.getSystem().displayMetrics.heightPixels);// / 2;
        //bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSE

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(view: View, i: Int) {
                when {
                    BottomSheetBehavior.STATE_EXPANDED == i -> {
                        /*  showView(bi.appBarLayout, getActionBarSize())
                                  hideAppBar(bi.profileLayout)*/
                        //view.minimumHeight = Resources.getSystem().displayMetrics.heightPixels
                    }
                    BottomSheetBehavior.STATE_COLLAPSED == i -> {
                        /* hideAppBar(bi.appBarLayout)
                                 showView(bi.profileLayout, getActionBarSize())*/
                    }
                    BottomSheetBehavior.STATE_HIDDEN == i -> {
                        dismiss()
                    }
                }
            }

            override fun onSlide(view: View, offset: Float) {}
        })
    }

    override fun onStart() {
        super.onStart()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED;
    }

    fun initMediaPicker(){

    }
    
    fun addVideoSelection(): MediaFacerPicker{
        addVideos = true
        return this
    }

    fun addImageSelection(): MediaFacerPicker{
        addImages = true
        return this
    }

    fun addAudioSelection(): MediaFacerPicker{
        addAudios = true
        return this
    }
    

}