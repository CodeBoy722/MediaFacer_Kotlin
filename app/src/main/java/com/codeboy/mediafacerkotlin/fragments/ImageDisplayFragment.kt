package com.codeboy.mediafacerkotlin.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacerkotlin.MainActivity
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.FragmentImageDisplayBinding
import com.codeboy.mediafacerkotlin.dialogs.ImageDetails
import com.codeboy.mediafacerkotlin.listeners.ImageActionListener
import com.codeboy.mediafacerkotlin.listeners.ImageDisplayItemListener
import com.codeboy.mediafacerkotlin.utils.EndlessScrollListener
import com.codeboy.mediafacerkotlin.viewAdapters.ImageDisplayAdapter
import com.codeboy.mediafacerkotlin.viewAdapters.ImageIndicatorAdapter
import com.codeboy.mediafacerkotlin.viewAdapters.ImageViewAdapter
import com.codeboy.mediafacerkotlin.viewModels.ImageViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ImageDisplayFragment(
    var paginationStart: Int,
    var paginationLimit: Int,
    var imageList: ArrayList<ImageContent>,
    var imagePosition: Int,
    var shouldPaginate: Boolean) : Fragment() {

    private lateinit var bindings: FragmentImageDisplayBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_display, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentImageDisplayBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner
        initViews()
    }


    private fun initViews(){
        val model = ImageViewModel()
        val layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)

        val pagerAdapter = ImageDisplayAdapter(object : ImageDisplayItemListener{
            override fun onImageItemClicked() {
                if(bindings.imagesIndicator.visibility == View.VISIBLE){
                    bindings.imagesIndicator.visibility = View.GONE
                }else{
                    bindings.imagesIndicator.visibility = View.VISIBLE
                }
            }
        })

        val indicatorAdapter = ImageIndicatorAdapter(object: ImageActionListener {
            override fun onImageItemClicked(imagePosition: Int, imageList: ArrayList<ImageContent>) {
                bindings.imagesPager.setCurrentItem(imagePosition,true)
            }
            override fun onImageItemLongClicked(imageItem: ImageContent) {}
        })

        bindings.imagesIndicator.layoutManager = layoutManager
        bindings.imagesIndicator.adapter = indicatorAdapter
        val smoothScroller: RecyclerView.SmoothScroller = CenterSmoothScroller(bindings.imagesIndicator.context)

        bindings.imagesPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        bindings.imagesPager.adapter = pagerAdapter
        bindings.imagesPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                smoothScroller.targetPosition = position;
                layoutManager.startSmoothScroll(smoothScroller) // scroll to position and center
                indicatorAdapter.setSelected(position)
                //load more when it last position
                if(position == (pagerAdapter.itemCount - 1)){
                    if (shouldPaginate){
                        model.loadNewItems(requireActivity(),paginationStart,paginationLimit,true)
                    }
                }
            }
        })

        pagerAdapter.submitList(imageList)
        indicatorAdapter.submitList(imageList)
        bindings.imagesPager.setCurrentItem(imagePosition,true)

        CoroutineScope(Dispatchers.Main).launch {
            delay(100).apply {
                smoothScroller.targetPosition = imagePosition;
                layoutManager.startSmoothScroll(smoothScroller)
                indicatorAdapter.setSelected(imagePosition)
            }
        }

        model.images.observe(viewLifecycleOwner) {
            pagerAdapter.submitList(it)
            indicatorAdapter.submitList(it)
            paginationStart = it.size //+ 1
        }

        bindings.imagesIndicator.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                if (shouldPaginate){
                    model.loadNewItems(requireActivity(),paginationStart,paginationLimit,true)
                }
            }
        })

    }

    override fun onDetach() {
        super.onDetach()
        //make bottom navigation visible again
        (requireActivity() as MainActivity).showBottomMenu()
    }

    class CenterSmoothScroller(context: Context?) :
        LinearSmoothScroller(context) {
        override fun calculateDtToFit(
            viewStart: Int,
            viewEnd: Int,
            boxStart: Int,
            boxEnd: Int,
            snapPreference: Int
        ): Int {
            return boxStart + (boxEnd - boxStart) / 2 - (viewStart + (viewEnd - viewStart) / 2)
        }
    }



}