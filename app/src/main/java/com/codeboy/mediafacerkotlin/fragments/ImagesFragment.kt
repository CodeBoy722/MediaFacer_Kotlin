package com.codeboy.mediafacerkotlin.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.FragmentImagesBinding
import com.codeboy.mediafacerkotlin.utils.EndlessScrollListener
import com.codeboy.mediafacerkotlin.utils.Utils.calculateNoOfColumns
import com.codeboy.mediafacerkotlin.viewAdapters.ImageViewAdapter
import com.codeboy.mediafacerkotlin.viewModels.ImageViewModel

class ImagesFragment : Fragment() {

    private lateinit var bindings: FragmentImagesBinding
    private var paginationStart = 0
    private var paginationLimit = 150
    private var shouldPaginate = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_images, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentImagesBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner
        initImages()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initImages(){
        // init and setup your recyclerview with a layout manager
        bindings.imagesList.hasFixedSize()
        bindings.imagesList.setHasFixedSize(true)
        bindings.imagesList.setItemViewCacheSize(20)
        val numOfColumns = calculateNoOfColumns(requireActivity(), 85f)
        val layoutManager = GridLayoutManager(requireActivity(),numOfColumns)
        bindings.imagesList.layoutManager = layoutManager
        bindings.imagesList.itemAnimator = null

        //init your adapter and bind it to recyclerview
        val adapter = ImageViewAdapter()
        bindings.imagesList.adapter = adapter

        //init viewModel
        val model = ImageViewModel()
        //observe the LifeData list of items and feed them to recyclerview each time there is an update
        model.images.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            //notifyDataSetChanged on adapter after submitting list to avoid scroll lagging on recyclerview
            paginationStart = it.size + 1
            adapter.notifyDataSetChanged()
        }

        //get paginated audio items using MediaFacer, remember to set paginationStart to size+1 of
        //of items gotten from MediaFacer to prepare for getting next page of items when user scroll
        model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)

        //adding EndlessScrollListener to our recyclerview to auto paginate items when user is
        //scrolling towards end of list
        bindings.imagesList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
            }
        })
    }

    /*private fun loadNewItems(){
        Handler(Looper.getMainLooper())
            .post {
                imagesList.addAll(
                    MediaFacer()
                        .withPagination(paginationStart, paginationLimit, shouldPaginate)
                        .getImages(requireActivity(), externalImagesContent)
                )
                paginationStart = imagesList.size + 1
                images.value = imagesList
                Toast.makeText(
                    requireActivity(),
                    "gotten new images data " + imagesList.size.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }
    }*/

}