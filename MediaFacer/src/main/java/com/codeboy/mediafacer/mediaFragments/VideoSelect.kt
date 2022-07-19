package com.codeboy.mediafacer.mediaFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codeboy.mediafacer.R
import com.codeboy.mediafacer.adapters.VideoContentAdapter
import com.codeboy.mediafacer.databinding.FragmentVideoSelectBinding
import com.codeboy.mediafacer.tools.EndlessScrollListener
import com.codeboy.mediafacer.tools.MediaSelectionListener
import com.codeboy.mediafacer.tools.Utils.calculateNoOfColumns
import com.codeboy.mediafacer.viewModels.VideosViewModel

internal class VideoSelect() : Fragment() {

    private lateinit var bindings: FragmentVideoSelectBinding
    private lateinit var viewModel: VideosViewModel
    private lateinit var listener: MediaSelectionListener

    private var paginationStart = 0
    private var paginationLimit = 100
    private var shouldPaginate = true

    constructor(listener: MediaSelectionListener): this(){
        this.listener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_video_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentVideoSelectBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner

        bindings.videoList.hasFixedSize()
        bindings.videoList.setHasFixedSize(true)
        bindings.videoList.setItemViewCacheSize(20)

        initVideos()
    }

    private fun initVideos(){
        val numOfColumns = calculateNoOfColumns(requireActivity(), 105f)
        val layoutManager = GridLayoutManager(requireActivity(),numOfColumns)
        bindings.videoList.layoutManager = layoutManager

        val adapter = VideoContentAdapter(listener)
        bindings.videoList.adapter = adapter

        viewModel = VideosViewModel()
        viewModel.videos.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            paginationStart = it.size
        }

        bindings.videoList.addOnScrollListener(object : EndlessScrollListener(layoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
               viewModel.loadMoreVideoItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
            }
        })

        viewModel.loadMoreVideoItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)

    }

}