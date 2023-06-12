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
import com.google.android.material.snackbar.Snackbar

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
                .setPickerColor(R.color.cartesian_orange)
                .setSelectionCompleteDrawable(R.drawable.ic_send)
                .setMediaBottomMenuIcons(R.drawable.ic_audio, R.drawable.ic_video, R.drawable.ic_image)
                .setAudioDefaultAlbumArtDrawable(R.drawable.music_placeholder)
                .setSelectionMenuTitles("Music","Videos","Images")
                .setBottomItemSelectionColors(R.color.white, R.color.material_grey_600)
                .addMediaSelectionListener(object : MediaSelectionListener {

                    override fun onMediaItemsSelected(
                        audios: ArrayList<AudioContent>, videos: ArrayList<VideoContent>, images: ArrayList<ImageContent>, ) {
                        val numImages = images.size
                        val numVideos = videos.size
                        val numAudios = audios.size
                        Snackbar.make(requireView(),
                            "$numImages images selected $numAudios audios selected $numVideos videos selected",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }).show(childFragmentManager,MediaFacerPicker().javaClass.canonicalName)
        }


        bindings.customPickerOne.setOnClickListener {
            MediaFacerPicker()
                .addAudioSelection()
                .addImageSelection()
                .setSelectionCompleteDrawable(R.drawable.ic_send)
                .setBottomItemSelectionColors(R.color.white, R.color.material_grey_400)
                .setPickerColor(R.color.crimson_red)
                .addMediaSelectionListener(object : MediaSelectionListener{

                    override fun onMediaItemsSelected(audios: ArrayList<AudioContent>, videos: ArrayList<VideoContent>, images: ArrayList<ImageContent>) {
                        val numImages = images.size
                        val numVideos = videos.size
                        val numAudios = audios.size
                        Snackbar.make(requireView(),
                            "$numImages images selected $numAudios audios selected $numVideos videos selected",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }).show(childFragmentManager, MediaFacerPicker().javaClass.canonicalName)

        }

        bindings.customPickerTwo.setOnClickListener {
            MediaFacerPicker()
                .addAudioSelection()
                .addImageSelection()
                .setSelectionCompleteDrawable(R.drawable.ic_send)
                .setBottomItemSelectionColors(R.color.white, R.color.material_grey_400)
                .setPickerColor(R.color.cartesian_green)
                .addMediaSelectionListener(object : MediaSelectionListener{

                    override fun onMediaItemsSelected(audios: ArrayList<AudioContent>, videos: ArrayList<VideoContent>, images: ArrayList<ImageContent>) {
                        val numImages = images.size
                        val numVideos = videos.size
                        val numAudios = audios.size
                        Snackbar.make(requireView(),
                            "$numImages images selected $numAudios audios selected $numVideos videos selected",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }).show(childFragmentManager, MediaFacerPicker().javaClass.canonicalName)
        }

        bindings.customPickerThree.setOnClickListener {
            MediaFacerPicker()
                .addAudioSelection()
                .setSelectionCompleteDrawable(R.drawable.ic_send)
                .setBottomItemSelectionColors(R.color.white, R.color.material_grey_600)
                .setPickerColor(R.color.material_grey_900)
                .addMediaSelectionListener(object : MediaSelectionListener{

                    override fun onMediaItemsSelected(audios: ArrayList<AudioContent>, videos: ArrayList<VideoContent>, images: ArrayList<ImageContent>) {
                        val numImages = images.size
                        val numVideos = videos.size
                        val numAudios = audios.size
                        Snackbar.make(requireView(),
                            "$numImages images selected $numAudios audios selected $numVideos videos selected",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }).show(childFragmentManager, MediaFacerPicker().javaClass.canonicalName)
        }

    }

}