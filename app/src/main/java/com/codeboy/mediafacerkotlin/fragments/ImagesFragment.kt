package com.codeboy.mediafacerkotlin.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import androidx.transition.Slide
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacer.tools.Utils
import com.codeboy.mediafacerkotlin.MainActivity
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.FragmentImagesBinding
import com.codeboy.mediafacerkotlin.dialogs.ImageDetails
import com.codeboy.mediafacerkotlin.listeners.ImageActionListener
import com.codeboy.mediafacerkotlin.listeners.ImageContainerActionListener
import com.codeboy.mediafacerkotlin.utils.EndlessScrollListener
import com.codeboy.mediafacerkotlin.utils.Utils.calculateNoOfColumns
import com.codeboy.mediafacerkotlin.viewAdapters.ImageFolderAdapter
import com.codeboy.mediafacerkotlin.viewAdapters.ImageViewAdapter
import com.codeboy.mediafacerkotlin.viewModels.ImageFolderViewModel
import com.codeboy.mediafacerkotlin.viewModels.ImageViewModel
import com.google.android.flexbox.*

class ImagesFragment() : Fragment() {

    private lateinit var bindings: FragmentImagesBinding
    private var paginationStart = 0
    private var paginationLimit = 300
    private var shouldPaginate = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_images, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentImagesBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner

        bindings.imageOptions.setOnClickListener {
            showMenu(it)
        }

        bindings.imagesList.hasFixedSize()
        bindings.imagesList.setHasFixedSize(true)
        bindings.imagesList.setItemViewCacheSize(20)
        bindings.emptyView.visibility = View.GONE

        initImages()
    }

    private fun initImages(){
        // init and setup your recyclerview with a layout manager
        //val numOfColumns = calculateNoOfColumns(requireActivity(), 85f)
        //val layoutManager = GridLayoutManager(requireActivity(),numOfColumns)
        val layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.SPACE_EVENLY
            alignItems = AlignItems.CENTER
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        bindings.imagesList.layoutManager = layoutManager
        paginationStart = 0
        paginationLimit = 300
        shouldPaginate = false

        //init your adapter and bind it to recyclerview
        val adapter = ImageViewAdapter(object: ImageActionListener{
            override fun onImageItemClicked(imagePosition: Int, imageList: ArrayList<ImageContent>) {
                val imageBrowser = ImageDisplayFragment(paginationStart, paginationLimit,imageList,imagePosition, false)
                val fade = Fade()
                imageBrowser.enterTransition = fade
                imageBrowser.exitTransition = fade
                val anim: Animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.animation_fall_down)
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.activity_parent, imageBrowser, imageBrowser.javaClass.canonicalName)
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit()
                imageBrowser.view?.startAnimation(anim)
                (requireActivity() as MainActivity).hideBottomMenu()
            }

            override fun onImageItemLongClicked(imageItem: ImageContent) {
                val imageDetails = ImageDetails(imageItem)
                imageDetails.show(childFragmentManager,imageDetails.javaClass.canonicalName)
            }
        })
        bindings.imagesList.adapter = adapter

        //init viewModel
        val model = ImageViewModel()
        //observe the LifeData list of items and feed them to recyclerview each time there is an update
        model.images.observe(viewLifecycleOwner) {
            //notifyDataSetChanged on adapter after submitting list to avoid scroll lagging on recyclerview
            adapter.submitList(it)
            if(it.size == 0) bindings.emptyView.visibility = View.VISIBLE
            paginationStart = it.size //+ 1
            bindings.loader.visibility = View.GONE
        }

        //get paginated audio items using MediaFacer, remember to set paginationStart to size+1 of
        //of items gotten from MediaFacer to prepare for getting next page of items when user scroll
        model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
        bindings.loader.visibility = View.VISIBLE

        //adding EndlessScrollListener to our recyclerview to auto paginate items when user is
        //scrolling towards end of list
        /*bindings.imagesList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
                bindings.loader.visibility = View.VISIBLE
            }
        })*/
    }

    private fun initImageFolders(){
        val layoutManager = LinearLayoutManager(requireActivity(),LinearLayoutManager.VERTICAL,false)
        bindings.imagesList.layoutManager = layoutManager
        //bindings.imagesList.itemAnimator = null

        paginationStart = 0
        paginationLimit = 300
        shouldPaginate = true

        val adapter = ImageFolderAdapter(object: ImageContainerActionListener{
            override fun onImageFolderClicked(mediaType: String, title: String, images: ArrayList<ImageContent>) {
                navigateToMediaDetails(mediaType,title,images)
            }

        })
        bindings.imagesList.adapter = adapter

        val model = ImageFolderViewModel()
        model.imageFolders.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            paginationStart = it.size //+ 1
            bindings.loader.visibility = View.GONE
        }

        model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
        bindings.loader.visibility = View.VISIBLE

        bindings.imagesList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
                bindings.loader.visibility = View.VISIBLE
            }
        })

    }

    private fun navigateToMediaDetails(mediaType: String, title: String, images: ArrayList<ImageContent>){
        //hide the bottom navigation in main activity
        (requireActivity() as MainActivity).hideBottomMenu()

        val mediaDetail = ImageContainerDetail(mediaType,title,images)
        val slideOutFromTop = Slide(Gravity.TOP)
        val slideInFromBottom = Slide(Gravity.BOTTOM)
        mediaDetail.enterTransition = slideInFromBottom
        mediaDetail.exitTransition = slideOutFromTop
        val anim: Animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.animation_fall_down)
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.activity_parent, mediaDetail, mediaDetail.javaClass.canonicalName)
            .setReorderingAllowed(true)
            .addToBackStack(null)
            .commit()
        mediaDetail.view?.startAnimation(anim)//animates the view of the fragment can be done alone or with transitions above
    }

    private fun showMenu(view: View){
        val popup = PopupMenu(requireActivity(), view)
        try {
            val fields = popup.javaClass.declaredFields
            for (field in fields) {
                if ("mPopup" == field.name) {
                    field.isAccessible = true
                    val menuPopupHelper = field[popup]
                    val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                    val setForceIcons = classPopupHelper.getMethod(
                        "setForceShowIcon",
                        Boolean::class.javaPrimitiveType
                    )
                    setForceIcons.invoke(menuPopupHelper, true)
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.image_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.images -> {
                    initImages()
                }
                R.id.image_folders -> {
                    initImageFolders()
                }
            }
            false
        }
        popup.show()
    }

    override fun onDestroyView() {
        viewModelStore.clear()
        super.onDestroyView()
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