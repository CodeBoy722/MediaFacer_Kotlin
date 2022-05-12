package com.codeboy.mediafacer.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codeboy.mediafacer.databinding.FragmentMediaFacerMediaPickerBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MediaFacerMediaPicker() : BottomSheetDialogFragment() {

    private lateinit var bindings: FragmentMediaFacerMediaPickerBinding
    private var addVideos = false
    private var addImages = false
    private var addAudios = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentMediaFacerMediaPickerBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner
    }

    fun initMediaPicker(){

    }
    
    fun addVideoSelection(): MediaFacerMediaPicker{
        addVideos = true
        return this
    }

    fun addImageSelection(): MediaFacerMediaPicker{
        addImages = true
        return this
    }

    fun addAudioSelection(): MediaFacerMediaPicker{
        addAudios = true
        return this
    }
    

}