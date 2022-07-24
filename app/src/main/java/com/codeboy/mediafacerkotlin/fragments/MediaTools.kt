package com.codeboy.mediafacerkotlin.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacer.models.VideoContent
import com.codeboy.mediafacer.tools.MediaFacerPicker
import com.codeboy.mediafacer.tools.MediaSelectionListener
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.FragmentMediaToolsBinding

class MediaTools() : Fragment() {

    lateinit var bindings: FragmentMediaToolsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_media_tools, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentMediaToolsBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner

        bindings.openPicker.setOnClickListener {

            MediaFacerPicker()
                .addAllMediaSelection()
                .setSelectionCompleteDrawable(com.codeboy.mediafacer.R.drawable.ic_media_check)
                .setAudioDefaultAlbumArtDrawable(R.drawable.music_placeholder)
                .setSelectionMenuTitles("Music","Videos","Photos")
                .addMediaSelectionListener(object : MediaSelectionListener {
                    override fun onMediaItemsSelected(
                        audios: ArrayList<AudioContent>, videos: ArrayList<VideoContent>, Images: ArrayList<ImageContent>, ) {
                        // todo handle your selected media items here
                    }
                }).show(childFragmentManager,MediaFacerPicker().javaClass.canonicalName)
        }

    }

}