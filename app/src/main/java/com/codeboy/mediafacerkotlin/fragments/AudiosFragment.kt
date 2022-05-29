package com.codeboy.mediafacerkotlin.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
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
import com.codeboy.mediafacerkotlin.utils.EndlessScrollListener
import com.codeboy.mediafacerkotlin.utils.Utils
import com.codeboy.mediafacerkotlin.viewAdapters.*
import com.codeboy.mediafacerkotlin.viewModels.*

class AudiosFragment() : Fragment() {

    private lateinit var bindings: FragmentAudiosBinding
    private var paginationStart = 0
    private var paginationLimit = 100
    private var shouldPaginate = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_audios, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentAudiosBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner

        bindings.audioOptions.setOnClickListener {
            showMenu(it)
        }

        bindings.audiosList.hasFixedSize()
        bindings.audiosList.setHasFixedSize(true)
        bindings.audiosList.setItemViewCacheSize(20)

        setupAudioSearch()
        initAudios()
    }

    @SuppressLint("NotifyDataSetChanged")
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
            audiosAdapter.notifyDataSetChanged()
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

    @SuppressLint("NotifyDataSetChanged")
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
            /*Toast.makeText(
                requireActivity(),
                "audio buckets " + it.size.toString(),
                Toast.LENGTH_LONG
            ).show()*/
            audiosBucketAdapter.notifyDataSetChanged()
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

    @SuppressLint("NotifyDataSetChanged")
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
           /* Toast.makeText(
                requireActivity(),
                "artists " + it.size.toString(),
                Toast.LENGTH_LONG
            ).show()*/
            audiosBucketAdapter.notifyDataSetChanged()
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

    @SuppressLint("NotifyDataSetChanged")
    private fun initAlbums(){
        val numOfColumns = Utils.calculateNoOfColumns(requireActivity(), 130f)
        val layoutManager = GridLayoutManager(requireActivity(),numOfColumns)
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
          /*  Toast.makeText(
                requireActivity(),
                "audio albums " + it.size.toString(),
                Toast.LENGTH_LONG
            ).show()*/
            audiosAlbumAdapter.notifyDataSetChanged()
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

    @SuppressLint("NotifyDataSetChanged")
    private fun initGenres(){
        val numOfColumns = Utils.calculateNoOfColumns(requireActivity(), 130f)
        val layoutManager = GridLayoutManager(requireActivity(),numOfColumns)
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
            /*Toast.makeText(
                requireActivity(),
                "genres " + it.size.toString(),
                Toast.LENGTH_LONG
            ).show()*/
            audiosBucketAdapter.notifyDataSetChanged()
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

    @SuppressLint("NotifyDataSetChanged")
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
            audiosAdapter.submitList(it)
            paginationStart = it.size //+ 1
            audiosAdapter.notifyDataSetChanged()
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
                paginationLimit = 50
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

}