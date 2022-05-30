package com.codeboy.mediafacerkotlin.dialogs

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codeboy.mediafacer.MediaDataUtils
import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.FragmentAudioDetailsBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.DateFormat

class AudioDetails() : BottomSheetDialogFragment() {

    private lateinit var bindings: FragmentAudioDetailsBinding
    private lateinit var audioItem: AudioContent
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    constructor(audioItem: AudioContent): this(){
        this.audioItem = audioItem
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_audio_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentAudioDetailsBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner

        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        //setting Peek at the 16:9 ratio key line of its parent.
        bottomSheetBehavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
        view.minimumHeight = (Resources.getSystem().displayMetrics.heightPixels)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        initContent()
    }

    private fun initContent(){
        bindings.name.text = audioItem.name
        bindings.artist.text = audioItem.artist
        bindings.album.text = audioItem.album
        bindings.genre.text = audioItem.genre
        bindings.path.text = audioItem.filePath
        bindings.dateModified.text = DateFormat.getDateInstance().format(audioItem.dateModified)
        bindings.duration.text = MediaDataUtils.milliSecondsToTimer(audioItem.duration)
        bindings.size.text = MediaDataUtils.convertBytes(audioItem.musicSize)
    }

}