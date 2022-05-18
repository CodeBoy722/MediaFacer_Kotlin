package com.codeboy.mediafacerkotlin.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.FragmentVideosBinding
import com.codeboy.mediafacerkotlin.utils.EndlessScrollListener
import com.codeboy.mediafacerkotlin.utils.Utils.calculateNoOfColumns
import com.codeboy.mediafacerkotlin.viewAdapters.VideoFolderAdapter
import com.codeboy.mediafacerkotlin.viewAdapters.VideoViewAdapter
import com.codeboy.mediafacerkotlin.viewModels.VideoFolderViewModel
import com.codeboy.mediafacerkotlin.viewModels.VideoViewModel

class VideosFragment : Fragment() {

    private lateinit var bindings: FragmentVideosBinding
    private var paginationStart = 0
    private var paginationLimit = 10
    private var shouldPaginate = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_videos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentVideosBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner
        //initVideos()
        initVideoFolders()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initVideos(){
        // init and setup your recyclerview with a layout manager
        bindings.videosList.hasFixedSize()
        bindings.videosList.setHasFixedSize(true)
        bindings.videosList.setItemViewCacheSize(20)
        val numOfColumns = calculateNoOfColumns(requireActivity(), 115f)
        val layoutManager = GridLayoutManager(requireActivity(),numOfColumns)
        bindings.videosList.layoutManager = layoutManager
        bindings.videosList.itemAnimator = null

        //init your adapter and bind it to recyclerview
        val adapter = VideoViewAdapter()
        bindings.videosList.adapter = adapter

        //init viewModel
        val model = VideoViewModel()
        //observe the LifeData list of items and feed them to recyclerview each time there is an update
        model.videos.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            //notifyDataSetChanged on adapter after submitting list to avoid scroll lagging on recyclerview
            paginationStart = it.size //+ 1
            adapter.notifyDataSetChanged()
        }

        //get paginated audio items using MediaFacer, remember to set paginationStart to size+1 of
        //of items gotten from MediaFacer to prepare for getting next page of items when user scroll
        model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)

        //adding EndlessScrollListener to our recyclerview to auto paginate items when user is
        //scrolling towards end of list
        bindings.videosList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
            }
        })
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun initVideoFolders(){
        bindings.videosList.hasFixedSize()
        bindings.videosList.setHasFixedSize(true)
        bindings.videosList.setItemViewCacheSize(20)
        val layoutManager = LinearLayoutManager(requireActivity(),LinearLayoutManager.VERTICAL,false)
        bindings.videosList.layoutManager = layoutManager
        bindings.videosList.itemAnimator = null

        val adapter = VideoFolderAdapter()
        bindings.videosList.adapter = adapter

        val model = VideoFolderViewModel()
        model.videoFolders.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            paginationStart = it.size //+ 1
            /*Toast.makeText(
                requireActivity(),
                "video folders " + it.size.toString(),
                Toast.LENGTH_LONG
            ).show()*/
            adapter.notifyDataSetChanged()
        }

        model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)

        bindings.videosList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
            }
        })

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