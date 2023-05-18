package com.codeboy.mediafacerkotlin.fragments

import android.content.Intent
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
import com.codeboy.mediafacer.models.VideoContent
import com.codeboy.mediafacer.tools.Utils
import com.codeboy.mediafacerkotlin.MainActivity
import com.codeboy.mediafacerkotlin.PlayerActivity
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.FragmentVideosBinding
import com.codeboy.mediafacerkotlin.dialogs.VideoDetails
import com.codeboy.mediafacerkotlin.listeners.VideoActionListener
import com.codeboy.mediafacerkotlin.listeners.VideoContainerActionListener
import com.codeboy.mediafacerkotlin.utils.EndlessScrollListener
import com.codeboy.mediafacerkotlin.utils.Utils.calculateNoOfColumns
import com.codeboy.mediafacerkotlin.viewAdapters.VideoFolderAdapter
import com.codeboy.mediafacerkotlin.viewAdapters.VideoViewAdapter
import com.codeboy.mediafacerkotlin.viewModels.VideoFolderViewModel
import com.codeboy.mediafacerkotlin.viewModels.VideoSearchViewModel
import com.codeboy.mediafacerkotlin.viewModels.VideoViewModel
import com.google.android.flexbox.*

class VideosFragment() : Fragment() {

    private lateinit var bindings: FragmentVideosBinding
    private var paginationStart = 0
    private var paginationLimit = 50
    private var shouldPaginate = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_videos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentVideosBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner

        bindings.videoOptions.setOnClickListener {
            showMenu(it)
        }

        bindings.videosList.hasFixedSize()
        bindings.videosList.setHasFixedSize(true)
        bindings.videosList.setItemViewCacheSize(20)
        bindings.emptyView.visibility = View.GONE

        setupVideoSearch()
        initVideos()
    }

    private fun initVideos(){
        // init and setup your recyclerview with a layout manager
        //val numOfColumns = calculateNoOfColumns(requireActivity(), 115f)
        //val layoutManager = GridLayoutManager(requireActivity(),numOfColumns)
        val layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.SPACE_EVENLY
            alignItems = AlignItems.CENTER
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        bindings.videosList.layoutManager = layoutManager

        paginationStart = 0
        paginationLimit = 50
        shouldPaginate = true

        //init your adapter and bind it to recyclerview
        val adapter = VideoViewAdapter(object: VideoActionListener{
            override fun onVideoItemClicked(playPosition: Int, mediaItemList: ArrayList<VideoContent>) {
                navigateToPlayer(playPosition, mediaItemList)
            }

            override fun onVideoItemLongClicked(videoItem: VideoContent) {
                val videoDetails = VideoDetails(videoItem)
                videoDetails.show(childFragmentManager,videoDetails.javaClass.canonicalName)
            }

        })
        bindings.videosList.adapter = adapter

        //init viewModel
        val model = VideoViewModel()
        //observe the LifeData list of items and feed them to recyclerview each time there is an update
        model.videos.observe(viewLifecycleOwner) {
            //notifyDataSetChanged on adapter after submitting list to avoid scroll lagging on recyclerview
            if(it.size == 0) bindings.emptyView.visibility = View.VISIBLE
            adapter.submitList(it)
            paginationStart = it.size //+ 1
            bindings.loader.visibility = View.GONE
        }

        //get paginated audio items using MediaFacer, remember to set paginationStart to size+1 of
        //of items gotten from MediaFacer to prepare for getting next page of items when user scroll
        model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
        bindings.loader.visibility = View.VISIBLE

        //adding EndlessScrollListener to our recyclerview to auto paginate items when user is
        //scrolling towards end of list
        bindings.videosList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
                bindings.loader.visibility = View.VISIBLE
            }
        })
    }

    private fun initVideoFolders(){
        val layoutManager = LinearLayoutManager(requireActivity(),LinearLayoutManager.VERTICAL,false)
        bindings.videosList.layoutManager = layoutManager
        //bindings.videosList.itemAnimator = null

        paginationStart = 0
        paginationLimit = 50
        shouldPaginate = true

        val adapter = VideoFolderAdapter(object: VideoContainerActionListener{
            override fun onVideoFolderClicked(mediaType: String, title: String, videos: ArrayList<VideoContent>) {
                navigateToMediaDetails(mediaType,title,videos)
            }
        })
        bindings.videosList.adapter = adapter

        val model = VideoFolderViewModel()
        model.videoFolders.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            paginationStart = it.size //+ 1
            bindings.loader.visibility = View.GONE
        }

        model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
        bindings.loader.visibility = View.VISIBLE

        bindings.videosList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
                bindings.loader.visibility = View.VISIBLE
            }
        })

    }

    private fun setupVideoSearch(){
        val numOfColumns = calculateNoOfColumns(requireActivity(), 115f)
        //val layoutManager = GridLayoutManager(requireActivity(),numOfColumns)
        val layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.SPACE_EVENLY
            alignItems = AlignItems.CENTER
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        val adapter = VideoViewAdapter(object: VideoActionListener{
            override fun onVideoItemClicked(playPosition: Int, mediaItemList: ArrayList<VideoContent>) {
                navigateToPlayer(playPosition, mediaItemList)
            }

            override fun onVideoItemLongClicked(videoItem: VideoContent) {
                val videoDetails = VideoDetails(videoItem)
                videoDetails.show(childFragmentManager,videoDetails.javaClass.canonicalName)
            }

        })
        var searchHolder = ""

        val videoSearch = VideoSearchViewModel()
        videoSearch.videos.observe(viewLifecycleOwner){
            val results = ArrayList<VideoContent>()
            results.addAll(it)
            adapter.submitList(results)
            paginationStart = it.size //+ 1
            bindings.loader.visibility = View.GONE
        }

        bindings.videosList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                videoSearch.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate,
                MediaFacer.videoSearchSelectionTypeDisplayName,searchHolder)
                bindings.loader.visibility = View.VISIBLE
            }
        })

        bindings.videoSearch.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(newText: CharSequence?, start: Int, before: Int, count: Int) {
                bindings.videosList.layoutManager = layoutManager
                bindings.videosList.adapter = adapter

                // on every new search, clear the list in the view model and reset the pagination values to default
                videoSearch.videosList.clear()
                paginationStart = 0
                paginationLimit = 50
                shouldPaginate = true

                val searchText = newText.toString().trim()
                if(!TextUtils.isEmpty(searchText)){
                    searchHolder = searchText
                    videoSearch.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate,
                        MediaFacer.videoSearchSelectionTypeDisplayName,searchText)
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun navigateToMediaDetails(mediaType: String, title: String, videos: ArrayList<VideoContent>){
//hide the bottom navigation in main activity
        (requireActivity() as MainActivity).hideBottomMenu()

        val mediaDetail = VideoContainerDetail(mediaType,title,videos)
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
        inflater.inflate(R.menu.video_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.videos -> {
                    initVideos()
                }
                R.id.video_folders -> {
                    initVideoFolders()
                }
            }
            false
        }
        popup.show()
    }

    private fun navigateToPlayer(playPosition: Int, mediaItemList: ArrayList<VideoContent>){
        val player = Intent(requireActivity(), PlayerActivity::class.java)
        player.action = "videos"
        player.putExtra("play_position", playPosition)
        player.putParcelableArrayListExtra("videos", mediaItemList)

        player.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(player)
        requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onDestroyView() {
        viewModelStore.clear()
        super.onDestroyView()
    }

    /*private fun loadNewItems(){
        Handler(Looper.getMainLooper())
            .post {
                videosList.addAll(
                    MediaFacer()
                        .withPagination(paginationStart, paginationLimit, shouldPaginate)
                        .getVideos(requireActivity(), externalVideoContent)
                )
                paginationStart = videosList.size + 1
                videos.value = videosList
                *//*Toast.makeText(
                    requireActivity(),
                    "gotten new video data " + videosList.size.toString(),
                    Toast.LENGTH_LONG
                ).show()*//*
            }
    }*/

}