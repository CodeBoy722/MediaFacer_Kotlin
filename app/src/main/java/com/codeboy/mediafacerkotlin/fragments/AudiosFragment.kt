package com.codeboy.mediafacerkotlin.fragments

import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.media.metrics.PlaybackStateEvent.STATE_PLAYING
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacerkotlin.MainActivity
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.FragmentAudiosBinding
import com.codeboy.mediafacerkotlin.dialogs.AudioDetails
import com.codeboy.mediafacerkotlin.listeners.AudioActionListener
import com.codeboy.mediafacerkotlin.listeners.AudioContainerActionListener
import com.codeboy.mediafacerkotlin.musicSession.MediaLibrary
import com.codeboy.mediafacerkotlin.musicSession.MusicService
import com.codeboy.mediafacerkotlin.musicSession.PlaybackProtocol
import com.codeboy.mediafacerkotlin.utils.EndlessScrollListener
import com.codeboy.mediafacerkotlin.utils.Utils
import com.codeboy.mediafacerkotlin.viewAdapters.*
import com.codeboy.mediafacerkotlin.viewModels.*
import com.google.android.flexbox.*
import com.google.gson.Gson
import kotlinx.coroutines.Delay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AudiosFragment() : Fragment() {

    private lateinit var bindings: FragmentAudiosBinding
    private var paginationStart = 0
    private var paginationLimit = 100
    private var shouldPaginate = true

    private lateinit var musicServiceBrowserCompat: MediaBrowserCompat
    private lateinit var musicServiceController: MediaControllerCompat
    var mCurrentState = 0
    private lateinit var animationDrawable: AnimationDrawable
    private lateinit var audiosAdapter: AudioViewAdapter
    private var firstLoad = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_audios, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentAudiosBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner
        bindings.playbackProtocol = PlaybackProtocol

        bindings.audioOptions.setOnClickListener {
            showMenu(it)
        }

       /* animationDrawable = bindings.player.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(5000)
        animationDrawable.setExitFadeDuration(2000)
        animationDrawable.start()*/

        bindings.audiosList.hasFixedSize()
        bindings.audiosList.setHasFixedSize(true)
        bindings.audiosList.setItemViewCacheSize(20)
        bindings.emptyView.visibility = View.GONE

    }

    override fun onStart() {
        super.onStart()
        //check connection to music service and proceed
        /*if (!musicServiceBrowserCompat.isConnected ) {
            musicServiceBrowserCompat.connect()
        }*/
        setupAudioSearch()
        initAudios()
    }

    private fun initAudios(){
        //mediaList for the service playback
        var audioContentList = ArrayList<AudioContent>()
        // init and setup your recyclerview with a layout manager
        val layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        bindings.audiosList.layoutManager = layoutManager
        //bindings.audiosList.itemAnimator = anim

        paginationStart = 0
        paginationLimit = 100
        shouldPaginate = true

        //init your adapter and bind it to recyclerview
        audiosAdapter = AudioViewAdapter(object : AudioActionListener{
            override fun onAudioItemClicked(audio: AudioContent, position: Int) {
                bundleNewPlaylist(audioContentList, position)
            }

            override fun onAudioItemLongClicked(audio: AudioContent, position: Int) {
               val audioDetails = AudioDetails(audio)
                audioDetails.show(childFragmentManager,audioDetails.javaClass.canonicalName)
            }
        }, viewLifecycleOwner)
        bindings.audiosList.adapter = audiosAdapter

        //init viewModel
        val model = AudioViewModel()
        //observe the LifeData list of items and feed them to recyclerview each time there is an update
        model.audios.observe(viewLifecycleOwner) {
            if(it.size == 0) bindings.emptyView.visibility = View.VISIBLE
            //check that items are not empty before starting or connecting service
            if(firstLoad == 0 && it.size > 0){
                lifecycleScope.launch{
                    startAndBindMediaLibrary()
                    // delay to give time to musicServiceController to get initialized
                    delay(1500L)
                }
                firstLoad = 1
            }
            audiosAdapter.submitList(it)
            paginationStart = it.size //+ 1
            audioContentList = it
            bindings.loader.visibility = View.GONE
        }

        //get paginated audio items using MediaFacer, remember to set paginationStart to size+1 of
        //of items gotten from MediaFacer to prepare for getting next page of items when user scroll
        //audiosList = ArrayList()
        model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
        bindings.loader.visibility = View.VISIBLE

        //adding EndlessScrollListener to our recyclerview to auto paginate items when user is
        //scrolling towards end of list
        bindings.audiosList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override  fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
                bindings.loader.visibility = View.VISIBLE
            }
        })
    }

    private fun bundleNewPlaylist(mediaList: ArrayList<AudioContent>, position: Int){
        val gson = Gson()
        val playlistJson: String = gson.toJson(mediaList)

        val bundlePlaylist = Bundle()
        bundlePlaylist.putInt("track_position_to_play", position)
        bundlePlaylist.putString("track_list", playlistJson)
        musicServiceController.transportControls.sendCustomAction("mediafacer.action.newPlaylist",bundlePlaylist)
    }

    private fun initAudioBuckets(){
        val layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        bindings.audiosList.layoutManager = layoutManager
        //bindings.audiosList.itemAnimator = null

        paginationStart = 0
        paginationLimit = 100
        shouldPaginate = true

        val audiosBucketAdapter = AudioBucketViewAdapter(object:AudioContainerActionListener{
            override fun onAudioContainerClicked(mediaType: String, title: String, audios: ArrayList<AudioContent>) {
                navigateToMediaDetails(mediaType,title,audios)
            }
        })
        bindings.audiosList.adapter = audiosBucketAdapter

        val model = AudioBucketViewModel()
        model.audioBuckets.observe(viewLifecycleOwner) {
            audiosBucketAdapter.submitList(it)
            paginationStart = it.size //+ 1
            bindings.loader.visibility = View.GONE
        }

        model.loadNewItems(requireActivity(),paginationStart,paginationLimit)
        bindings.loader.visibility = View.VISIBLE

        bindings.audiosList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override  fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                model.loadNewItems(requireActivity(),paginationStart,paginationLimit)
                bindings.loader.visibility = View.VISIBLE
            }
        })

    }

    private fun initArtists(){
        val layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        bindings.audiosList.layoutManager = layoutManager
        //bindings.audiosList.itemAnimator = null

        paginationStart = 0
        paginationLimit = 100
        shouldPaginate = true

        val audiosBucketAdapter = ArtistAdapter(object:AudioContainerActionListener{
            override fun onAudioContainerClicked(mediaType: String, title: String, audios: ArrayList<AudioContent>) {
                navigateToMediaDetails(mediaType,title,audios)
            }
        })
        bindings.audiosList.adapter = audiosBucketAdapter

        val model = ArtistViewModel()
        model.audioArtists.observe(viewLifecycleOwner) {
            audiosBucketAdapter.submitList(it)
            paginationStart = it.size //+ 1
            bindings.loader.visibility = View.GONE
        }

        model.loadNewItems(requireActivity(),paginationStart,paginationLimit)
        bindings.loader.visibility = View.VISIBLE

        bindings.audiosList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override  fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                model.loadNewItems(requireActivity(),paginationStart,paginationLimit)
                bindings.loader.visibility = View.VISIBLE
            }
        })
    }

    private fun initAlbums(){
        val numOfColumns = Utils.calculateNoOfColumns(requireActivity(), 125f)
        //val layoutManager = GridLayoutManager(requireActivity(),numOfColumns)
        val layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.SPACE_EVENLY
            alignItems = AlignItems.CENTER
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        bindings.audiosList.layoutManager = layoutManager
        //bindings.audiosList.itemAnimator = null

        paginationStart = 0
        paginationLimit = 100
        shouldPaginate = true

        val audiosAlbumAdapter = AudioAlbumAdapter(object:AudioContainerActionListener{
            override fun onAudioContainerClicked(mediaType: String, title: String, audios: ArrayList<AudioContent>) {
                navigateToMediaDetails(mediaType,title,audios)
            }
        })
        bindings.audiosList.adapter = audiosAlbumAdapter

        val model = AudioAlbumViewModel()
        model.audioAlbums.observe(viewLifecycleOwner) {
            audiosAlbumAdapter.submitList(it)
            paginationStart = it.size //+ 1
            bindings.loader.visibility = View.GONE
        }

        model.loadNewItems(requireActivity(),paginationStart,paginationLimit)
        bindings.loader.visibility = View.VISIBLE

        bindings.audiosList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override  fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                model.loadNewItems(requireActivity(),paginationStart,paginationLimit)
                bindings.loader.visibility = View.VISIBLE
            }
        })
    }

    private fun initGenres(){
        val numOfColumns = Utils.calculateNoOfColumns(requireActivity(), 1250f)
        //val layoutManager = GridLayoutManager(requireActivity(),numOfColumns)
        val layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.SPACE_EVENLY
            alignItems = AlignItems.CENTER
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        bindings.audiosList.layoutManager = layoutManager
        //bindings.audiosList.itemAnimator = null

        paginationStart = 0
        paginationLimit = 100
        shouldPaginate = true

        val audiosBucketAdapter = GenreAdapter(object:AudioContainerActionListener{
            override fun onAudioContainerClicked(mediaType: String, title: String, audios: ArrayList<AudioContent>) {
                navigateToMediaDetails(mediaType,title,audios)
            }
        })
        bindings.audiosList.adapter = audiosBucketAdapter

        val model = AudioGenreViewModel()
        model.audioGenres.observe(viewLifecycleOwner) {
            audiosBucketAdapter.submitList(it)
            paginationStart = it.size //+ 1
            bindings.loader.visibility = View.GONE
        }

        model.loadNewItems(requireActivity(),paginationStart,paginationLimit)
        bindings.loader.visibility = View.VISIBLE

        bindings.audiosList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override  fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                model.loadNewItems(requireActivity(),paginationStart,paginationLimit)
                bindings.loader.visibility = View.VISIBLE
            }
        })
    }

    private fun setupAudioSearch(){
        val layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        val audiosAdapter = AudioViewAdapter(object : AudioActionListener{
            override fun onAudioItemClicked(audio: AudioContent, position: Int) {}

            override fun onAudioItemLongClicked(audio: AudioContent, position: Int) {
                val audioDetails = AudioDetails(audio)
                audioDetails.show(childFragmentManager,audioDetails.javaClass.canonicalName)
            }
        },viewLifecycleOwner)
        var searchHolder = ""

        val audioSearch = AudioSearchViewModel()
        audioSearch.audios.observe(viewLifecycleOwner){
            val results = ArrayList<AudioContent>()
            results.addAll(it)
            audiosAdapter.submitList(results)
            paginationStart = it.size //+ 1
            bindings.loader.visibility = View.GONE
        }

        bindings.audiosList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override  fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                audioSearch.loadNewItems(requireActivity(),paginationStart,paginationLimit,
                    MediaFacer.audioSearchSelectionTypeTitle,searchHolder)
                bindings.loader.visibility = View.VISIBLE
            }
        })

        bindings.audioSearch.addTextChangedListener(object:TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(newText: CharSequence?, start: Int, before: Int, count: Int) {
                bindings.audiosList.layoutManager = layoutManager
                bindings.audiosList.adapter = audiosAdapter

                // on every new search, clear the list in the view model and reset the pagination values to default
                audioSearch.audiosList.clear()
                paginationStart = 0
                paginationLimit = 100
                shouldPaginate = true

                val searchText = newText.toString().trim()
                if(!TextUtils.isEmpty(searchText)){
                    searchHolder = searchText
                    audioSearch.loadNewItems(requireActivity(),paginationStart,paginationLimit,
                        MediaFacer.audioSearchSelectionTypeTitle,searchText)
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun navigateToMediaDetails(mediaType: String, title: String, audios: ArrayList<AudioContent>){
        //hide the bottom navigation in main activity
        (requireActivity() as MainActivity).hideBottomMenu()

        val mediaDetail = AudioContainerDetails(mediaType,title,audios,musicServiceController)
        val slideOutFromTop = Slide(Gravity.TOP)
        val slideInFromBottom = Slide(Gravity.BOTTOM)
        mediaDetail.enterTransition = slideInFromBottom
        mediaDetail.exitTransition = slideOutFromTop
        val anim: Animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.animation_fall_down)
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.activity_parent, mediaDetail, mediaDetail.javaClass.canonicalName)
            .setReorderingAllowed(true)
            .addToBackStack(null)
            .commit()
        mediaDetail.view?.startAnimation(anim)//animates the view of the fragment can be done alone or with transitions above
    }

    private fun showMenu(view: View){
        val popup = PopupMenu(requireActivity(), view)
        try {
            val fields = popup.javaClass.declaredFields
            for (field in fields) {
                if ("mPopup" == field.name) {
                    field.isAccessible = true
                    val menuPopupHelper = field[popup]
                    val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                    val setForceIcons = classPopupHelper.getMethod(
                        "setForceShowIcon",
                        Boolean::class.javaPrimitiveType
                    )
                    setForceIcons.invoke(menuPopupHelper, true)
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.audio_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.audio -> {
                    initAudios()
                }
                R.id.buckets -> {
                    initAudioBuckets()
                }
                R.id.albums -> {
                    initAlbums()
                }
                R.id.artists -> {
                    initArtists()
                }
                R.id.genres -> {
                    initGenres()
                }
            }
            false
        }
        popup.show()
    }

    override fun onDestroyView() {
        viewModelStore.clear()
        musicServiceController.unregisterCallback(mMediaControllerCallback)
        musicServiceBrowserCompat.disconnect()
        super.onDestroyView()
    }

    //MusicService Section, functions for music playback with media session and exoplayer -------------------------------------------------------------------------------------------------------------------------------------------------------------

    private fun startAndBindMusicService(){
        val intent = Intent(requireActivity(), MusicService::class.java)
        requireActivity().startService(intent)

        MediaBrowserCompat(requireActivity(),
            ComponentName(requireActivity(), MusicService::class.java), mMediaBrowserConnectionCallback, requireActivity().intent.extras).apply {
            connect()
            musicServiceBrowserCompat = this
        }
    }

    private fun startAndBindMediaLibrary(){
        if (!MediaLibrary.isStarted()){
            val intent = Intent(requireActivity(), MediaLibrary::class.java)
            requireActivity().startService(intent)
        }
        MediaBrowserCompat(requireActivity(),
            ComponentName(requireActivity(), MediaLibrary::class.java), mMediaBrowserConnectionCallback, requireActivity().intent.extras).apply {
            connect()
            musicServiceBrowserCompat = this
        }
    }

    private val mMediaControllerCallback: MediaControllerCompat.Callback =
        object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
                super.onPlaybackStateChanged(state)
                when (state.state) {
                    PlaybackStateCompat.STATE_PLAYING -> {
                        audiosAdapter.playingState = true
                        audiosAdapter.notifyDataSetChanged()
                        PlaybackProtocol.setIsMusicPlaying(true)
                        mCurrentState = PlaybackState.STATE_PLAYING
                        bindings.playPause.setImageDrawable(getDrawable(requireActivity(), R.drawable.ic_pause))
                    }
                    PlaybackStateCompat.STATE_PAUSED -> {
                        audiosAdapter.playingState = false
                        audiosAdapter.notifyDataSetChanged()
                        PlaybackProtocol.setIsMusicPlaying(false)
                        mCurrentState = PlaybackState.STATE_PAUSED
                        bindings.playPause.setImageDrawable(getDrawable(requireActivity(), R.drawable.ic_play))
                    }
                    PlaybackStateCompat.STATE_STOPPED ->  mCurrentState = PlaybackState.STATE_STOPPED
                    PlaybackStateCompat.STATE_SKIPPING_TO_NEXT -> mCurrentState = PlaybackState.STATE_SKIPPING_TO_NEXT
                    PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS -> mCurrentState = PlaybackState.STATE_SKIPPING_TO_PREVIOUS
                    PlaybackStateCompat.STATE_REWINDING -> mCurrentState = PlaybackState.STATE_REWINDING
                    PlaybackStateCompat.STATE_FAST_FORWARDING -> mCurrentState = PlaybackState.STATE_FAST_FORWARDING
                    PlaybackStateCompat.STATE_BUFFERING -> {}
                    PlaybackStateCompat.STATE_CONNECTING -> mCurrentState = PlaybackState.STATE_CONNECTING
                    PlaybackStateCompat.STATE_ERROR -> mCurrentState = PlaybackState.STATE_ERROR
                    PlaybackStateCompat.STATE_NONE -> {
                        audiosAdapter.playingState = false
                        audiosAdapter.notifyDataSetChanged()
                        PlaybackProtocol.setIsMusicPlaying(false)
                        mCurrentState = PlaybackState.STATE_NONE
                        bindings.playPause.setImageDrawable(getDrawable(requireActivity(), R.drawable.ic_play))
                    }
                    PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM -> mCurrentState = PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM
                }
            }

            override fun onSessionReady() {
                super.onSessionReady()
            }
        }

    private val mMediaBrowserConnectionCallback: MediaBrowserCompat.ConnectionCallback =
        object : MediaBrowserCompat.ConnectionCallback() {
            override fun onConnected() {
                super.onConnected()
                try {
                    musicServiceController = MediaControllerCompat(
                        requireActivity(),
                        musicServiceBrowserCompat.sessionToken
                    )
                    musicServiceController.registerCallback(mMediaControllerCallback)

                    bindings.previous.setOnClickListener(View.OnClickListener {
                        musicServiceController.transportControls.skipToPrevious()
                        //musicServiceController.transportControls.skipToQueueItem()
                    })

                    //setup playback views
                    bindings.musicTitle.isSelected = true

                    val rotateAnim : RotateAnimation = RotateAnimation(0f,360f,
                        Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f)
                    rotateAnim.interpolator = LinearInterpolator()
                    rotateAnim.duration = 1500
                    rotateAnim.repeatCount = Animation.INFINITE
                    bindings.musicAlbumArt.startAnimation(rotateAnim)

                    bindings.next.setOnClickListener(View.OnClickListener {
                        musicServiceController.transportControls.skipToNext()
                    })

                    bindings.playPause.setOnClickListener(View.OnClickListener {
                        if(mCurrentState == STATE_PLAYING){
                            musicServiceController.transportControls.pause()
                        }else {
                            musicServiceController.transportControls.play()
                        }
                    })

                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }

}

