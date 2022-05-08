package com.codeboy.mediafacerkotlin.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.MediaFacer.Companion.externalVideoContent
import com.codeboy.mediafacer.models.VideoContent
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.FragmentVideosBinding
import com.codeboy.mediafacerkotlin.utils.EndlessScrollListener
import com.codeboy.mediafacerkotlin.utils.Utils.calculateNoOfColumns
import com.codeboy.mediafacerkotlin.viewAdapters.VideoViewAdapter

class VideosFragment : Fragment() {

    private lateinit var bindings: FragmentVideosBinding
    private var videos: MutableLiveData<ArrayList<VideoContent>> = MutableLiveData()
    private var paginationStart = 0
    private var paginationLimit = 150
    private var shouldPaginate = true
    private lateinit var videosList: ArrayList<VideoContent>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_videos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentVideosBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner
        initVideos()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initVideos(){

        bindings.videosList.hasFixedSize()
        bindings.videosList.setHasFixedSize(true)
        bindings.videosList.setItemViewCacheSize(20)
        val numOfColumns = calculateNoOfColumns(requireActivity(), 115f)
        val layoutManager = GridLayoutManager(requireActivity(),numOfColumns)
        bindings.videosList.layoutManager = layoutManager
        bindings.videosList.itemAnimator = null

        val adapter = VideoViewAdapter()
        bindings.videosList.adapter = adapter

        videos.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            //notifyDataSetChanged on adapter after submitting list to avoid scroll lagging on recyclerview
            adapter.notifyDataSetChanged()
        }

        //init videoList
        videosList = ArrayList()
        loadNewItems()

        bindings.videosList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                loadNewItems()
            }
        })
    }

    private fun loadNewItems(){
        Handler(Looper.getMainLooper())
            .post {
                videosList.addAll(
                    MediaFacer()
                        .withPagination(paginationStart, paginationLimit, shouldPaginate)
                        .getVideos(requireActivity(), externalVideoContent)
                )
                paginationStart = videosList.size + 1
                videos.value = videosList
                Toast.makeText(
                    requireActivity(),
                    "gotten new video data " + videosList.size.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

}