package com.codeboy.mediafacerkotlin.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.MediaFacer.Companion.externalImagesContent
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.FragmentImagesBinding
import com.codeboy.mediafacerkotlin.utils.EndlessScrollListener
import com.codeboy.mediafacerkotlin.viewAdapters.ImageViewAdapter

class ImagesFragment : Fragment() {

    lateinit var bindings: FragmentImagesBinding
    var images: MutableLiveData<ArrayList<ImageContent>> = MutableLiveData()
    var paginationStart = 0
    var paginationLimit = 50
    var shouldPaginate = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_images, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentImagesBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner
        initImages()
    }

    private fun initImages(){

        bindings.imagesList.hasFixedSize()
        bindings.imagesList.setHasFixedSize(true)
        bindings.imagesList.setItemViewCacheSize(20)
        val numOfColumns = calculateNoOfColumns(requireActivity(), 90f)
        val layoutManager = GridLayoutManager(requireActivity(),numOfColumns)
        bindings.imagesList.layoutManager = layoutManager
        bindings.imagesList.itemAnimator = null

        val adapter = ImageViewAdapter()
        bindings.imagesList.adapter = adapter

        images.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        val imagesList = ArrayList<ImageContent>()
        imagesList.addAll(
            MediaFacer()
                .withPagination(paginationStart,paginationLimit,shouldPaginate)
                .getImages(requireActivity(),externalImagesContent)
        )
        paginationStart = imagesList.size+1
        images.value = imagesList


        bindings.imagesList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                imagesList.addAll(
                    MediaFacer()
                        .withPagination(paginationStart,paginationLimit,shouldPaginate)
                        .getImages(requireActivity(),externalImagesContent)
                )
                paginationStart = imagesList.size+1
                images.value = imagesList
            }
        })

    }

    fun calculateNoOfColumns(context: Context, columnWidthDp: Float): Int { // For example columnWidthdp=180
        val displayMetrics = context.resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        return (screenWidthDp / columnWidthDp + 0.5).toInt()
    }


}