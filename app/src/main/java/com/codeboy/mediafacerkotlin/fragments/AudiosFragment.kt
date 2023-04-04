package com.codeboy.mediafacerkotlin.fragments

import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.media.metrics.PlaybackStateEvent.STATE_PLAYING
import android.media.session.PlaybackState
import android.os.Bundle
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
import androidx.lifecycle.Observer
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
import com.codeboy.mediafacerkotlin.utils.MusicDataUtil
import com.codeboy.mediafacerkotlin.utils.Utils
import com.codeboy.mediafacerkotlin.viewAdapters.*
import com.codeboy.mediafacerkotlin.viewModels.*
import com.google.android.flexbox.*

class AudiosFragment() : Fragment() {

    private lateinit var bindings: FragmentAudiosBinding
    private var paginationStart = 0
    private var paginationLimit = 100
    private var shouldPaginate = true

    private lateinit var musicServiceBrowserCompat: MediaBrowserCompat
    private lateinit var musicServiceController: MediaControllerCompat
    var mCurrentState = 0
    private lateinit var animationDrawable: AnimationDrawable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        PlaybackProtocol.setIsMusicPlaying(false)
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

        prepareMusicPlayback()
        setupAudioSearch()
        initAudios()
    }

    override fun onStart() {
        super.onStart()
        //check connection to music service and proceed
        if(::musicServiceBrowserCompat.isInitialized){
            if (!musicServiceBrowserCompat.isConnected) {
                musicServiceBrowserCompat.connect()
            }
        }

    }

    private fun initAudios(){
        // init and setup your recyclerview with a layout manager
        val layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        bindings.audiosList.layoutManager = layoutManager
        //bindings.audiosList.itemAnimator = anim

        paginationStart = 0
        paginationLimit = 100
        shouldPaginate = true

        //init your adapter and bind it to recyclerview
        val audiosAdapter = AudioViewAdapter(object : AudioActionListener{
            override fun onAudioItemClicked(audio: AudioContent) {}

            override fun onAudioItemLongClicked(audio: AudioContent) {
               val audioDetails = AudioDetails(audio)
                audioDetails.show(childFragmentManager,audioDetails.javaClass.canonicalName)
            }
        })
        bindings.audiosList.adapter = audiosAdapter

        //init viewModel
        val model = AudioViewModel()
        //observe the LifeData list of items and feed them to recyclerview each time there is an update
        model.audios.observe(viewLifecycleOwner) {
            audiosAdapter.submitList(it)
            //notifyDataSetChanged on adapter after submitting list to avoid scroll lagging on recyclerview
            paginationStart = it.size //+ 1
            /*Toast.makeText(
                requireActivity(),
                "gotten new music data " + it.size.toString(),
                Toast.LENGTH_LONG
            ).show()*/
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

        model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
        bindings.loader.visibility = View.VISIBLE

        bindings.audiosList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override  fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
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

        model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
        bindings.loader.visibility = View.VISIBLE

        bindings.audiosList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override  fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
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

        model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
        bindings.loader.visibility = View.VISIBLE

        bindings.audiosList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override  fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
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

        model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
        bindings.loader.visibility = View.VISIBLE

        bindings.audiosList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override  fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
                bindings.loader.visibility = View.VISIBLE
            }
        })
    }

    private fun setupAudioSearch(){
        val layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        val audiosAdapter = AudioViewAdapter(object : AudioActionListener{
            override fun onAudioItemClicked(audio: AudioContent) {}

            override fun onAudioItemLongClicked(audio: AudioContent) {
                val audioDetails = AudioDetails(audio)
                audioDetails.show(childFragmentManager,audioDetails.javaClass.canonicalName)
            }
        })
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
                audioSearch.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate,
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
                    audioSearch.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate,
                        MediaFacer.audioSearchSelectionTypeTitle,searchText)
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun navigateToMediaDetails(mediaType: String, title: String, audios: ArrayList<AudioContent>){
        //hide the bottom navigation in main activity
        (requireActivity() as MainActivity).hideBottomMenu()

        val mediaDetail = AudioContainerDetails(mediaType,title,audios)
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

    private fun prepareMusicPlayback(){
        //load the music list first
        var musicList = ArrayList<AudioContent>()
        musicList = MusicDataUtil(requireActivity()).getLastPlaylist()

        if(musicList.isEmpty()){
            //load new musicList from MediaFacer
            val model = AudioViewModel()
            model.audios.observe(viewLifecycleOwner, Observer {
                musicList = it
                PlaybackProtocol.setMusicList(musicList)
                PlaybackProtocol.setCurrentMusic(musicList[0])
                startAndBindMusicService()
                //startAndBindMediaLibrary()
            })
            model.loadNewItems(requireActivity(),0,150,false)
        }else{
            PlaybackProtocol.setMusicList(musicList)
            PlaybackProtocol.setCurrentMusic(musicList[0])
            startAndBindMusicService()
            //startAndBindMediaLibrary()
            //setupUpMusicList(musicList)
        }
    }

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
        val intent = Intent(requireActivity(), MediaLibrary::class.java)
        requireActivity().startService(intent)

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
                        mCurrentState = PlaybackState.STATE_PLAYING
                        bindings.playPause.setImageDrawable(getDrawable(requireActivity(), R.drawable.ic_pause))
                    }
                    PlaybackStateCompat.STATE_PAUSED -> {
                        mCurrentState = PlaybackState.STATE_PAUSED
                        bindings.playPause.setImageDrawable(getDrawable(requireActivity(), R.drawable.ic_play))
                    }
                    PlaybackStateCompat.STATE_STOPPED -> mCurrentState = PlaybackState.STATE_STOPPED
                    PlaybackStateCompat.STATE_SKIPPING_TO_NEXT -> mCurrentState = PlaybackState.STATE_SKIPPING_TO_NEXT
                    PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS -> mCurrentState = PlaybackState.STATE_SKIPPING_TO_PREVIOUS
                    PlaybackStateCompat.STATE_REWINDING -> mCurrentState = PlaybackState.STATE_REWINDING
                    PlaybackStateCompat.STATE_FAST_FORWARDING -> mCurrentState = PlaybackState.STATE_FAST_FORWARDING
                    PlaybackStateCompat.STATE_BUFFERING -> {}
                    PlaybackStateCompat.STATE_CONNECTING -> mCurrentState = PlaybackState.STATE_CONNECTING
                    PlaybackStateCompat.STATE_ERROR -> mCurrentState = PlaybackState.STATE_ERROR
                    PlaybackStateCompat.STATE_NONE -> mCurrentState = PlaybackState.STATE_NONE
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