package com.codeboy.mediafacer.tools

import android.content.res.Resources
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codeboy.mediafacer.MediaFacerException
import com.codeboy.mediafacer.R
import com.codeboy.mediafacer.databinding.FragmentMediaFacerPickerBinding
import com.codeboy.mediafacer.mediaFragments.AudioSelect
import com.codeboy.mediafacer.mediaFragments.ImageSelect
import com.codeboy.mediafacer.mediaFragments.VideoSelect
import com.codeboy.mediafacer.models.AudioArtistContent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MediaFacerPicker() : BottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var bindings: FragmentMediaFacerPickerBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private var addVideos = false
    private var addImages = false
    private var addAudios = false

    private var customAlbumDrawable = R.drawable.music_placeholder
    private lateinit var listener: MediaSelectionListener

    private lateinit var videoSelect: VideoSelect
    private lateinit var audioSelect: AudioSelect
    private lateinit var imageSelect: ImageSelect

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_media_facer_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentMediaFacerPickerBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner

        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO;
        view.minimumHeight = (Resources.getSystem().displayMetrics.heightPixels)

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
        //set bottom sheet to full size
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED;
        initMediaPicker()
    }

    @Throws(MediaFacerException::class)
    private fun initMediaPicker(): MediaFacerPicker{
        when {
            !addVideos && !addAudios && !addImages -> {
                throw MediaFacerException("add at least one media type for selection")
            }
            !this::listener.isInitialized -> {
                throw MediaFacerException("add a selection listener to get your selected items")
            }
            else -> {
                buildMediaPicker()
            }
        }
        return this
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

    fun addAllMediaSelection(): MediaFacerPicker{
        addAudios = true
        addImages = true
        addVideos = true
        return this
    }

    fun addMediaSelectionListener(listener: MediaSelectionListener): MediaFacerPicker{
        this.listener = listener
        return this
    }

    private fun buildMediaPicker(){
        bindings.audioOption.setOnClickListener(this)
        bindings.videoOption.setOnClickListener(this)
        bindings.imageOption.setOnClickListener(this)
        //todo setup navigation
        setUpSelectedMediaFragment()
    }

    private fun setUpSelectedMediaFragment(){
        when {
            addVideos -> {
                videoSelect = VideoSelect()
            }
            addAudios -> {
                audioSelect = AudioSelect(customAlbumDrawable)
            }
            addImages -> {
                imageSelect = ImageSelect()
            }
        }
    }

    // pass in yur custom selecting complete drawable
    fun setSelectionCompleteDrawable(drawableId: Int): MediaFacerPicker{

        return this
    }

    //pass in your custom default album image for audios without an album art
    //if you are selecting audios too
    fun setAudioDefaultAlbumArtDrawable(drawableId: Int): MediaFacerPicker{
        customAlbumDrawable = drawableId
        return this
    }

    //pass in your title strings here in any local or language to fit your use-case
    fun setSelectionMenuTitles(audiosText: String, videosText: String, ImagesText:String): MediaFacerPicker{
        bindings.audioOptionText.text = audiosText
        bindings.videoOptionText.text = videosText
        bindings.imageOptionText.text = videosText
        return this
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.audio_option -> {

            }
            R.id.video_option -> {

            }
            R.id.image_option -> {

            }
        }
    }

}