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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.MediaSelectionViewModel
import com.codeboy.mediafacer.R
import com.codeboy.mediafacer.adapters.AudioContentAdapter
import com.codeboy.mediafacer.databinding.FragmentAudioSelectBinding
import com.codeboy.mediafacer.models.AudioBucketContent
import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacer.tools.EndlessScrollListener
import com.codeboy.mediafacer.tools.MediaSelectionListener
import com.codeboy.mediafacer.viewModels.AudiosViewModel

internal class AudioSelect() : Fragment() {

    private var defaultAlbumArt = 0
    private lateinit var bindings: FragmentAudioSelectBinding
    private lateinit var viewModel: AudiosViewModel
    private lateinit var listener: MediaSelectionViewModel

    private var paginationStart = 0
    private var paginationLimit = 100
    private var shouldPaginate = true

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: AudioContentAdapter
    private lateinit var scrollListener: EndlessScrollListener
    private lateinit var searchScrollListener: EndlessScrollListener

    constructor(defaultAlbumArt: Int, listener: MediaSelectionViewModel): this(){
        this.defaultAlbumArt = defaultAlbumArt
        this.listener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        return inflater.inflate(R.layout.fragment_audio_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentAudioSelectBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner
        // init all views with defaults
        initViews()
    }

    private fun initViews(){
        bindings.audioList.hasFixedSize()
        bindings.audioList.setHasFixedSize(true)
        bindings.audioList.setItemViewCacheSize(20)

        //config grid layout manager
        layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        bindings.audioList.layoutManager = layoutManager

        //add adapter
        adapter = AudioContentAdapter(defaultAlbumArt, listener)
        bindings.audioList.adapter = adapter

        //init view model
        viewModel = AudiosViewModel()

        //observe audio results from view model
        viewModel.audios.observe(viewLifecycleOwner) {
            val results = ArrayList<AudioContent>()
            results.addAll(it)
            adapter.submitList(results)
            paginationStart = it.size
        }

        // observe audio search results from view Model
        viewModel.foundAudios.observe(viewLifecycleOwner){
            val results = ArrayList<AudioContent>()
            results.addAll(it)
            adapter.submitList(results)
            paginationStart = it.size //+ 1
        }

        //setup audio search
        audioSearch()
        //setup audio folder selection
        loadAudioFolders()
    }

    private fun loadAudios(){
        scrollListener = object : EndlessScrollListener(layoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                viewModel.loadMoreAudioItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
            }
        }
        bindings.audioList.addOnScrollListener(scrollListener)

        viewModel.loadMoreAudioItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
    }

    private fun loadAudioFolders(){
        var audioBuckets = ArrayList<AudioBucketContent>()
        viewModel.audioBuckets.observe(viewLifecycleOwner) {
            audioBuckets = it
            val folderNames = ArrayList<String>()
            folderNames.add("All Audios")
            for(bucket: AudioBucketContent in audioBuckets){
                folderNames.add(bucket.bucketName)
            }

            val spinnerAdapter = ArrayAdapter(
                requireActivity(),
                R.layout.audio_spinner_text,
                folderNames
            )
            bindings.audioFolderSpinner.adapter = spinnerAdapter
        }

        bindings.audioFolderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //clear scroll listener to avoid unwanted behavior in adapter
                bindings.audioList.clearOnScrollListeners()
                if(position == 0){
                    viewModel.audiosList.clear()
                    paginationStart = 0
                    paginationLimit = 100
                    shouldPaginate = true
                    loadAudios()
                }else{
                    adapter.submitList(audioBuckets[position - 1].audios)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        viewModel.loadAudioBuckets(requireActivity())
    }

    private fun audioSearch(){
        var searchHolder = ""

        searchScrollListener = object: EndlessScrollListener(layoutManager){
            override  fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                viewModel.searchAudioItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate,
                    MediaFacer.audioSearchSelectionTypeTitle,searchHolder)
            }
        }

        bindings.audioSearch.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(newText: CharSequence?, start: Int, before: Int, count: Int) {
                bindings.audioList.clearOnScrollListeners()
                bindings.audioList.addOnScrollListener(searchScrollListener)
                // on every new search, clear the list in the view model and reset the pagination values to default
                viewModel.foundList.clear()
                paginationStart = 0
                paginationLimit = 100
                shouldPaginate = true

                val searchText = newText.toString().trim()
                if(!TextUtils.isEmpty(searchText)){
                    searchHolder = searchText
                    viewModel.searchAudioItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate,
                        MediaFacer.audioSearchSelectionTypeTitle,searchText)
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
    }
}