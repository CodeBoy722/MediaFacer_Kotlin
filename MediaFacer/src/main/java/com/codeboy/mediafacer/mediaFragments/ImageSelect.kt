package com.codeboy.mediafacer.mediaFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codeboy.mediafacer.MediaSelectionViewModel
import com.codeboy.mediafacer.R
import com.codeboy.mediafacer.adapters.ImageContentAdapter
import com.codeboy.mediafacer.databinding.FragmentImageSelectBinding
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacer.models.ImageFolderContent
import com.codeboy.mediafacer.tools.EndlessScrollListener
import com.codeboy.mediafacer.tools.MediaSelectionListener
import com.codeboy.mediafacer.tools.Utils
import com.codeboy.mediafacer.tools.Utils.calculateNoOfColumns
import com.codeboy.mediafacer.viewModels.ImagesViewModel
import com.google.android.flexbox.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlin.math.roundToInt

internal class ImageSelect() : Fragment() {

    private var paginationStart = 0
    private var paginationLimit = 300
    private var shouldPaginate = true

    private lateinit var bindings: FragmentImageSelectBinding
    private lateinit var viewModel: ImagesViewModel
    private lateinit var listener: MediaSelectionViewModel

    private lateinit var layoutManager: FlexboxLayoutManager
    private lateinit var scrollListener: EndlessScrollListener
    private lateinit var adapter: ImageContentAdapter

    constructor(listener: MediaSelectionViewModel):this(){
        this.listener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        return inflater.inflate(R.layout.fragment_image_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentImageSelectBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner
        // init all views with defaults
        initViews()
    }

    private fun initViews(){
        bindings.imageList.hasFixedSize()
        bindings.imageList.setHasFixedSize(true)
        bindings.imageList.setItemViewCacheSize(20)
        bindings.emptyView.visibility = View.GONE

        //config grid layout manager
        val numOfColumns = calculateNoOfColumns(requireActivity(), 85f)
        //layoutManager = GridLayoutManager(requireActivity(),numOfColumns)
        layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.SPACE_EVENLY
            alignItems = AlignItems.CENTER
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        bindings.imageList.layoutManager = layoutManager

        //add adapter
        adapter = ImageContentAdapter(listener)
        bindings.imageList.adapter = adapter

        //init images view model
        viewModel = ImagesViewModel()

        //observe image results from view model
        viewModel.images.observe(viewLifecycleOwner) {
            //note to use "it" directly the variable  imagesList in the view model must be private
            //because of this, the adapter can't compute a change because it thinks is still has the same list
            //"it" must be a final and immutable list to be used directly and have desired effect
            if(it.size == 0) bindings.emptyView.visibility = View.VISIBLE
            val results = ArrayList<ImageContent>()
            results.addAll(it)
            adapter.submitList(results)
            paginationStart = it.size
        }

        //setup image folder selection
        loadImageFolders()
    }

    private fun loadImages(){
        scrollListener = object: EndlessScrollListener(layoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                viewModel.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
            }
        }
        bindings.imageList.addOnScrollListener(scrollListener)
        viewModel.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
    }

    private fun loadImageFolders(){
        var imageFolders = ArrayList<ImageFolderContent>()

        viewModel.imageFolders.observe(viewLifecycleOwner) {
            imageFolders = it
            val folderNames = ArrayList<String>()
            folderNames.add("All Images")
            for (bucket: ImageFolderContent in imageFolders) {
                folderNames.add(bucket.folderName)
            }

            val spinnerAdapter = ArrayAdapter(
                requireActivity(),
                R.layout.image_spinner_text,
                folderNames
            )
            bindings.imageFolderSpinner.adapter = spinnerAdapter
        }

        bindings.imageFolderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //clear scroll listener to avoid unwanted behavior in adapter
                bindings.imageList.clearOnScrollListeners()
                if(position == 0){
                    viewModel.imagesList.clear()
                    paginationStart = 0
                    paginationLimit = 100
                    shouldPaginate = true
                    loadImages()
                }else{
                    adapter.submitList(imageFolders[position - 1].images)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        CoroutineScope(Dispatchers.Main).async {
            viewModel.loadFolders(requireActivity())
        }.invokeOnCompletion {
            loadImages()
        }
    }

}