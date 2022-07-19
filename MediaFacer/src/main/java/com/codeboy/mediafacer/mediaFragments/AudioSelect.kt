package com.codeboy.mediafacer.mediaFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codeboy.mediafacer.R
import com.codeboy.mediafacer.adapters.AudioContentAdapter
import com.codeboy.mediafacer.databinding.FragmentAudioSelectBinding
import com.codeboy.mediafacer.tools.EndlessScrollListener
import com.codeboy.mediafacer.tools.MediaSelectionListener
import com.codeboy.mediafacer.viewModels.AudiosViewModel

internal class AudioSelect() : Fragment() {

    private var defaultAlbumArt = 0
    private lateinit var bindings: FragmentAudioSelectBinding
    private lateinit var viewModel: AudiosViewModel
    private lateinit var listener: MediaSelectionListener

    private var paginationStart = 0
    private var paginationLimit = 100
    private var shouldPaginate = true

    constructor(defaultAlbumArt: Int, listener: MediaSelectionListener): this(){
        this.defaultAlbumArt = defaultAlbumArt
        this.listener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {
        return inflater.inflate(R.layout.fragment_audio_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentAudioSelectBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner

        bindings.audioList.hasFixedSize()
        bindings.audioList.setHasFixedSize(true)
        bindings.audioList.setItemViewCacheSize(20)

        initAudios()
    }

    private fun initAudios(){
        val layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        bindings.audioList.layoutManager = layoutManager

        paginationStart = 0
        paginationLimit = 100
        shouldPaginate = true

        val adapter = AudioContentAdapter(defaultAlbumArt, listener)
        bindings.audioList.adapter = adapter

        viewModel = AudiosViewModel()
        viewModel.audios.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            paginationStart = it.size
        }

        bindings.audioList.addOnScrollListener(object : EndlessScrollListener(layoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                viewModel.loadMoreAudioItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
            }
        })

        viewModel.loadMoreAudioItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
    }



    private fun searchAudios(){

    }






}