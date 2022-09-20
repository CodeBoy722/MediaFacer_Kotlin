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
import com.codeboy.mediafacer.tools.Utils
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
    private lateinit var adapter: VideoContentAdapter
    private lateinit var scrollListener: EndlessScrollListener
    private lateinit var searchScrollListener: EndlessScrollListener

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
        // init all views with defaults
        initViews()
    }

    private fun initViews(){
        bindings.videoList.hasFixedSize()
        bindings.videoList.setHasFixedSize(true)
        bindings.videoList.setItemViewCacheSize(20)

        //config grid layout manager
        val numOfColumns = calculateNoOfColumns(requireActivity(), 105f)
        layoutManager = GridLayoutManager(requireActivity(),numOfColumns)
        bindings.videoList.layoutManager = layoutManager

        bindings.videoList.addItemDecoration(
            Utils.MarginItemDecoration(8)
        )

        //add adapter
        adapter = VideoContentAdapter(listener)
        bindings.videoList.adapter = adapter

        //init view model
        viewModel = VideosViewModel()

        //observe video results for view model
        viewModel.videos.observe(viewLifecycleOwner) {
            val results = ArrayList<VideoContent>()
            results.addAll(it)
            adapter.submitList(results)
            paginationStart = it.size
        }

        // observe video search results from view Model
        viewModel.foundVideos.observe(viewLifecycleOwner){
            val results = ArrayList<VideoContent>()
            results.addAll(it)
            adapter.submitList(results)
            paginationStart = it.size //+ 1
        }

        //setup video search
        videoSearch()
        //setup video folder selection
        loadVideoFolders()
    }

    private fun loadVideos(){
        scrollListener = object: EndlessScrollListener(layoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                viewModel.loadMoreVideoItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
            }
        }
        bindings.videoList.addOnScrollListener(scrollListener)

        viewModel.loadMoreVideoItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
    }

    private fun loadVideoFolders(){
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
                    //todo fix bug here
                    bindings.videoList.clearOnScrollListeners()
                    viewModel.videoList.clear()
                    paginationStart = 0
                    paginationLimit = 100
                    shouldPaginate = true
                    loadVideos()
                }else{
                    adapter.submitList(videoFolders[position - 1].videos)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        viewModel.loadVideoBucketItems(requireActivity())
    }

    private fun videoSearch(){
        var searchHolder = ""

        searchScrollListener = object: EndlessScrollListener(layoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                viewModel.searchVideoItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate,
                    MediaFacer.videoSearchSelectionTypeDisplayName,searchHolder)
            }
        }

        bindings.videoSearch.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(newText: CharSequence?, start: Int, before: Int, count: Int) {
                bindings.videoList.clearOnScrollListeners()
                bindings.videoList.addOnScrollListener(searchScrollListener)

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