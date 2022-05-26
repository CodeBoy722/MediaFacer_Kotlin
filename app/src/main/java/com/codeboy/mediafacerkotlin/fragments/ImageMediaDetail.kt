package com.codeboy.mediafacerkotlin.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacerkotlin.MainActivity
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.FragmentImageMediadetailBinding
import com.codeboy.mediafacerkotlin.utils.Utils
import com.codeboy.mediafacerkotlin.viewAdapters.ImageViewAdapter

class ImageMediaDetail() : Fragment() {

    private lateinit var bindings: FragmentImageMediadetailBinding
    private lateinit var audioMediaType: String
    private lateinit var title: String
    private lateinit var Images: ArrayList<ImageContent>

    constructor(audioMediaType: String, title: String, audios: ArrayList<ImageContent>) : this() {
        this.audioMediaType = audioMediaType
        this.title = title
        this.Images = audios
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
        val numOfColumns = Utils.calculateNoOfColumns(requireActivity(), 82f)
        val layoutManager = GridLayoutManager(requireActivity(),numOfColumns)
        bindings.imageList.layoutManager = layoutManager

        initContent()
    }

    private fun initContent(){
        val headTitle = "$audioMediaType: $title"
        bindings.mediaTitle.text = headTitle

        val audiosAdapter = ImageViewAdapter()
        audiosAdapter.submitList(Images)
        bindings.imageList.adapter = audiosAdapter
    }

    override fun onDetach() {
        super.onDetach()
        //make bottom navigation visible again
        (requireActivity() as MainActivity).showBottomMenu()
    }



}