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
import com.codeboy.mediafacer.R
import com.codeboy.mediafacer.adapters.ImageContentAdapter
import com.codeboy.mediafacer.databinding.FragmentImageSelectBinding
import com.codeboy.mediafacer.models.ImageFolderContent
import com.codeboy.mediafacer.tools.EndlessScrollListener
import com.codeboy.mediafacer.tools.MediaSelectionListener
import com.codeboy.mediafacer.tools.Utils.calculateNoOfColumns
import com.codeboy.mediafacer.viewModels.ImagesViewModel

internal class ImageSelect() : Fragment() {

    private var paginationStart = 0
    private var paginationLimit = 300
    private var shouldPaginate = true

    private lateinit var bindings: FragmentImageSelectBinding
    private lateinit var viewModel: ImagesViewModel
    private lateinit var listener: MediaSelectionListener
    private lateinit var layoutManager: GridLayoutManager

    constructor(listener: MediaSelectionListener):this(){
        this.listener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        return inflater.inflate(R.layout.fragment_image_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentImageSelectBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner

        bindings.imageList.hasFixedSize()
        bindings.imageList.setHasFixedSize(true)
        bindings.imageList.setItemViewCacheSize(20)
        val numOfColumns = calculateNoOfColumns(requireActivity(), 82f)
        layoutManager = GridLayoutManager(requireActivity(),numOfColumns)
        bindings.imageList.layoutManager = layoutManager

        viewModel = ImagesViewModel()
        loadImageFolders()
    }

    private fun loadImages(){
        val adapter = ImageContentAdapter(listener)
        bindings.imageList.adapter = adapter

        viewModel.images.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            paginationStart = it.size
        }

        bindings.imageList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                viewModel.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
            }
        })

        viewModel.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
    }

    private fun loadImageFolders(){
        val adapter = ImageContentAdapter(listener)
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
                R.layout.spinner_text,
                folderNames
            )
            bindings.imageFolderSpinner.adapter = spinnerAdapter
        }

        bindings.imageFolderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position == 0){
                    loadImages()
                }else{
                    bindings.imageList.adapter = adapter
                    adapter.submitList(imageFolders[position - 1].images)
                }

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        viewModel.loadFolders(requireActivity())
    }

}