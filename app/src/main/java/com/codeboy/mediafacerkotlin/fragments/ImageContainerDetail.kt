package com.codeboy.mediafacerkotlin.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.Fade
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacerkotlin.MainActivity
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.FragmentImageMediadetailBinding
import com.codeboy.mediafacerkotlin.dialogs.ImageDetails
import com.codeboy.mediafacerkotlin.listeners.ImageActionListener
import com.codeboy.mediafacerkotlin.utils.Utils
import com.codeboy.mediafacerkotlin.viewAdapters.ImageViewAdapter
import com.google.android.flexbox.*

class ImageContainerDetail() : Fragment() {

    private lateinit var bindings: FragmentImageMediadetailBinding
    private lateinit var audioMediaType: String
    private lateinit var title: String
    private lateinit var images: ArrayList<ImageContent>

    constructor(audioMediaType: String, title: String, audios: ArrayList<ImageContent>) : this() {
        this.audioMediaType = audioMediaType
        this.title = title
        this.images = audios
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_mediadetail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentImageMediadetailBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner

        bindings.imageList.hasFixedSize()
        bindings.imageList.setHasFixedSize(true)
        bindings.imageList.setItemViewCacheSize(20)
        val numOfColumns = Utils.calculateNoOfColumns(requireActivity(), 85f)
        //val layoutManager = GridLayoutManager(requireActivity(),numOfColumns)
        val layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.SPACE_EVENLY
            alignItems = AlignItems.FLEX_START
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        bindings.imageList.layoutManager = layoutManager
        bindings.imageList.addItemDecoration(
            com.codeboy.mediafacer.tools.Utils.MarginItemDecoration(8)
        )

        initContent()
    }

    private fun initContent(){
        val headTitle = "$audioMediaType: $title"
        bindings.mediaTitle.text = headTitle

        val audiosAdapter = ImageViewAdapter(object: ImageActionListener {
            override fun onImageItemClicked(imagePosition: Int, imageList: ArrayList<ImageContent>) {
                val imageBrowser = ImageDisplayFragment(0, 0,imageList,imagePosition, false)
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
        audiosAdapter.submitList(images)
        bindings.imageList.adapter = audiosAdapter
    }

    override fun onDetach() {
        super.onDetach()
        //make bottom navigation visible again
        (requireActivity() as MainActivity).showBottomMenu()
    }

    override fun onDestroyView() {
        viewModelStore.clear()
        super.onDestroyView()
    }

}