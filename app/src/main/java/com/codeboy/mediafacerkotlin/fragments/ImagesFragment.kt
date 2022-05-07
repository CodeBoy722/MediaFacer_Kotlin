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
import com.codeboy.mediafacer.MediaFacer.Companion.externalImagesContent
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.FragmentImagesBinding
import com.codeboy.mediafacerkotlin.utils.EndlessScrollListener
import com.codeboy.mediafacerkotlin.utils.Utils.calculateNoOfColumns
import com.codeboy.mediafacerkotlin.viewAdapters.ImageViewAdapter

class ImagesFragment : Fragment() {

    private lateinit var bindings: FragmentImagesBinding
    private var images: MutableLiveData<ArrayList<ImageContent>> = MutableLiveData()
    private var paginationStart = 0
    private var paginationLimit = 150
    private var shouldPaginate = true
    private lateinit var imagesList: ArrayList<ImageContent>

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
        bindings.imagesList.hasFixedSize()
        bindings.imagesList.setHasFixedSize(true)
        bindings.imagesList.setItemViewCacheSize(20)
        val numOfColumns = calculateNoOfColumns(requireActivity(), 100f)
        val layoutManager = GridLayoutManager(requireActivity(),numOfColumns)
        bindings.imagesList.layoutManager = layoutManager
        bindings.imagesList.itemAnimator = null

        val adapter = ImageViewAdapter()
        bindings.imagesList.adapter = adapter

        images.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            //adapter.notifyItemRangeChanged(paginationStart,it.size-1)
            adapter.notifyDataSetChanged()
        }

        imagesList = ArrayList()
        loadNewItems()

        bindings.imagesList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
               loadNewItems()
            }
        })
    }

    private fun loadNewItems(){
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
    }


    //normal RecyclerView.OnScrollListener(), you can use this if you wish
    /* bindings.imagesList.addOnScrollListener(object: RecyclerView.OnScrollListener() {
         override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
             if(layoutManager.findLastVisibleItemPosition() == layoutManager.itemCount-1){
                 loadNewItems()
             }
             super.onScrolled(recyclerView, dx, dy)
         }
     })*/

}