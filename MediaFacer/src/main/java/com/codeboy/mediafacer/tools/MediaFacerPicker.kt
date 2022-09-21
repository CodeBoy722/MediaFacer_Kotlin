package com.codeboy.mediafacer.tools

import android.content.res.Resources
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.codeboy.mediafacer.MediaFacerException
import com.codeboy.mediafacer.R
import com.codeboy.mediafacer.adapters.PagerFragmentAdapter
import com.codeboy.mediafacer.databinding.FragmentMediaFacerPickerBinding
import com.codeboy.mediafacer.mediaFragments.AudioSelect
import com.codeboy.mediafacer.mediaFragments.ImageSelect
import com.codeboy.mediafacer.mediaFragments.VideoSelect
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MediaFacerPicker : BottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var bindings: FragmentMediaFacerPickerBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private var addVideos = false
    private var addImages = false
    private var addAudios = false

    private var completeDrawableId: Int? = null
    private var audiosText: String = "Music"
    private var videosText: String = "Videos"
    private var imagesText: String = "Images"

    private var customAlbumDrawable = R.drawable.music_placeholder
    private lateinit var listener: MediaSelectionListener

    private lateinit var videoSelect: VideoSelect
    private lateinit var audioSelect: AudioSelect
    private lateinit var imageSelect: ImageSelect

    private var imageFragPosition: Int? = null
    private var videoFragPosition: Int? = null
    private var audioFragPosition: Int? = null

    private var selectedFragMenuColor  = R.color.material_grey_500
    private var unSelectedFragMenuColor = R.color.white

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //remove default background
        setStyle(STYLE_NORMAL,R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_media_facer_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentMediaFacerPickerBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner

        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
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

                       /* val params = (bindings.menus.layoutParams as CoordinatorLayout.LayoutParams)
                        params.gravity = Gravity.CENTER
                        bindings.menus.layoutParams = params*/
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
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
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
        bindings.audiosBox.setOnClickListener(this)
        bindings.videosBox.setOnClickListener(this)
        bindings.imagesBox.setOnClickListener(this)

        bindings.audioOptionText.text = audiosText
        bindings.videoOptionText.text = videosText
        bindings.imageOptionText.text = imagesText

        if(completeDrawableId != null){
            bindings.completeSelection.setImageDrawable(ResourcesCompat
                .getDrawable(requireActivity().resources, completeDrawableId!!,null))
        }

        setUpSelectedMediaFragment()
    }

    private fun setUpSelectedMediaFragment(){
        val medias = ArrayList<Fragment>()
        when {
            addImages -> {
                imageSelect = ImageSelect(listener)
                medias.add(imageSelect)
                imageFragPosition = medias.size - 1
            }else -> {
            bindings.imagesBox.visibility = View.GONE
            }
        }

        when {
            addVideos -> {
                videoSelect = VideoSelect(listener)
                medias.add(videoSelect)
                videoFragPosition = medias.size - 1
            }else -> {
            bindings.videosBox.visibility = View.GONE
            }
        }

        when {
            addAudios -> {
                audioSelect = AudioSelect(customAlbumDrawable,listener)
                medias.add(audioSelect)
                audioFragPosition = medias.size - 1
            }else -> {
            bindings.audiosBox.visibility = View.GONE
            }
        }

        when (medias.size) {
            1 -> {
                bindings.menus.visibility = View.GONE
            }
        }

        val pagerAdapter = PagerFragmentAdapter(requireActivity(),medias)
        bindings.mediaPager.offscreenPageLimit = medias.size
        bindings.mediaPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        bindings.mediaPager.adapter = pagerAdapter


        bindings.mediaPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                indicateSelectedFragment(position)
            }
        })
    }

    // pass in your custom selecting complete drawable
    fun setSelectionCompleteDrawable(drawableId: Int): MediaFacerPicker{
        completeDrawableId = drawableId
        return this
    }

    //pass in your custom default album image for audios without an album art
    //if you are selecting audios too
    fun setAudioDefaultAlbumArtDrawable(drawableId: Int): MediaFacerPicker{
        customAlbumDrawable = drawableId
        return this
    }

    //pass in your title strings here in any local or language to fit your use-case
    fun setSelectionMenuTitles(audiosText: String, videosText: String, imagesText:String): MediaFacerPicker{
        this.audiosText = audiosText
        this.videosText = videosText
        this.imagesText = imagesText
        return this
    }

    //set fragment selection colors
    fun setBottomItemSelectionColors(selectedColor: Int, unselectedColor: Int): MediaFacerPicker{
        selectedFragMenuColor = selectedColor
        unSelectedFragMenuColor = unselectedColor
        return this
    }

    private fun indicateSelectedFragment(fragmentPosition: Int){
        if(audioFragPosition == fragmentPosition){
            bindings.audioOption.setColorFilter(ContextCompat.getColor(requireActivity(), selectedFragMenuColor), android.graphics.PorterDuff.Mode.SRC_IN)
            bindings.audioOptionText.setTextColor(ContextCompat.getColor(requireActivity(), selectedFragMenuColor))
        }else{
            bindings.audioOption.setColorFilter(ContextCompat.getColor(requireActivity(), unSelectedFragMenuColor), android.graphics.PorterDuff.Mode.SRC_IN)
            bindings.audioOptionText.setTextColor(ContextCompat.getColor(requireActivity(), unSelectedFragMenuColor))
        }

        if(imageFragPosition == fragmentPosition){
            bindings.imageOption.setColorFilter(ContextCompat.getColor(requireActivity(), selectedFragMenuColor), android.graphics.PorterDuff.Mode.SRC_IN)
            bindings.imageOptionText.setTextColor(ContextCompat.getColor(requireActivity(), selectedFragMenuColor))
        }else{
            bindings.imageOption.setColorFilter(ContextCompat.getColor(requireActivity(), unSelectedFragMenuColor), android.graphics.PorterDuff.Mode.SRC_IN)
            bindings.imageOptionText.setTextColor(ContextCompat.getColor(requireActivity(), unSelectedFragMenuColor))
        }

        if(videoFragPosition == fragmentPosition){
            bindings.videoOption.setColorFilter(ContextCompat.getColor(requireActivity(), selectedFragMenuColor), android.graphics.PorterDuff.Mode.SRC_IN)
            bindings.videoOptionText.setTextColor(ContextCompat.getColor(requireActivity(), selectedFragMenuColor))
        }else{
            bindings.videoOption.setColorFilter(ContextCompat.getColor(requireActivity(), unSelectedFragMenuColor), android.graphics.PorterDuff.Mode.SRC_IN)
            bindings.videoOptionText.setTextColor(ContextCompat.getColor(requireActivity(), unSelectedFragMenuColor))
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.audios_box -> {
                when {
                    audioFragPosition != null -> {
                        bindings.mediaPager.setCurrentItem(audioFragPosition!!, true)
                        indicateSelectedFragment(audioFragPosition!!)
                    }
                }
            }
            R.id.videos_box -> {
                when {
                    videoFragPosition != null -> {
                        bindings.mediaPager.setCurrentItem(videoFragPosition!!, true)
                        indicateSelectedFragment(videoFragPosition!!)
                    }
                }
            }
            R.id.images_box -> {
                when {
                    imageFragPosition != null -> {
                        bindings.mediaPager.setCurrentItem(imageFragPosition!!, true)
                        indicateSelectedFragment(imageFragPosition!!)
                    }
                }
            }
        }
    }


}