package com.codeboy.mediafacer.mediaFragments

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.R
import com.codeboy.mediafacer.adapters.VideoContentAdapter
import com.codeboy.mediafacer.databinding.FragmentVideoSelectBinding
import com.codeboy.mediafacer.models.VideoContent
import com.codeboy.mediafacer.models.VideoFolderContent
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
    private lateinit var layoutManager: GridLayoutManager

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
        val numOfColumns = calculateNoOfColumns(requireActivity(), 105f)
        layoutManager = GridLayoutManager(requireActivity(),numOfColumns)
        bindings.videoList.layoutManager = layoutManager

        viewModel = VideosViewModel()
        videoSearch()
        loadVideoFolders()
    }

    private fun loadVideos(){
        val adapter = VideoContentAdapter(listener)
        bindings.videoList.adapter = adapter

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

    private fun loadVideoFolders(){
        val adapter = VideoContentAdapter(listener)
        var videoFolders = ArrayList<VideoFolderContent>()

        viewModel.videoFolders.observe(viewLifecycleOwner) {
            videoFolders = it
            val folderNames = ArrayList<String>()
            folderNames.add("All Videos")
            for(bucket: VideoFolderContent in videoFolders){
                folderNames.add(bucket.folderName)
            }

            val spinnerAdapter = ArrayAdapter(
                requireActivity(),
                R.layout.video_spinner_text,
                folderNames
            )
            bindings.videoFolderSpinner.adapter = spinnerAdapter
        }

        bindings.videoFolderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position == 0){
                    loadVideos()
                }else{
                    bindings.videoList.adapter = adapter
                    adapter.submitList(videoFolders[position - 1].videos)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        viewModel.loadVideoBucketItems(requireActivity())
    }

    private fun videoSearch(){
        val adapter = VideoContentAdapter(listener)
        var searchHolder = ""

        viewModel.foundVideos.observe(viewLifecycleOwner){
            val results = ArrayList<VideoContent>()
            results.addAll(it)
            adapter.submitList(results)
            paginationStart = it.size //+ 1
        }

        bindings.videoList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                viewModel.searchVideoItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate,
                    MediaFacer.videoSearchSelectionTypeDisplayName,searchHolder)
            }
        })

        bindings.videoSearch.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(newText: CharSequence?, start: Int, before: Int, count: Int) {
                bindings.videoList.layoutManager = layoutManager
                bindings.videoList.adapter = adapter

                // on every new search, clear the list in the view model and reset the pagination values to default
                viewModel.foundList.clear()
                paginationStart = 0
                paginationLimit = 100
                shouldPaginate = true

                val searchText = newText.toString().trim()
                if(!TextUtils.isEmpty(searchText)){
                    searchHolder = searchText
                    viewModel.searchVideoItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate,
                        MediaFacer.videoSearchSelectionTypeDisplayName,searchText)
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
    }
}