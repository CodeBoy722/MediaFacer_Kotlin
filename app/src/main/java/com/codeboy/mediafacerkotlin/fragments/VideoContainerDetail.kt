package com.codeboy.mediafacerkotlin.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.codeboy.mediafacer.models.VideoContent
import com.codeboy.mediafacerkotlin.MainActivity
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.FragmentVideoMediaDetailBinding
import com.codeboy.mediafacerkotlin.dialogs.VideoDetails
import com.codeboy.mediafacerkotlin.listeners.VideoActionListener
import com.codeboy.mediafacerkotlin.utils.Utils
import com.codeboy.mediafacerkotlin.viewAdapters.VideoViewAdapter

class VideoContainerDetail() : Fragment() {

    private lateinit var bindings: FragmentVideoMediaDetailBinding
    private lateinit var audioMediaType: String
    private lateinit var title: String
    private lateinit var videos: ArrayList<VideoContent>

    constructor(audioMediaType: String, title: String, audios: ArrayList<VideoContent>): this() {
        this.audioMediaType = audioMediaType
        this.title = title
        this.videos = audios
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video_media_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentVideoMediaDetailBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner

        bindings.videoList.hasFixedSize()
        bindings.videoList.setHasFixedSize(true)
        bindings.videoList.setItemViewCacheSize(20)
        val numOfColumns = Utils.calculateNoOfColumns(requireActivity(), 105f)
        val layoutManager = GridLayoutManager(requireActivity(),numOfColumns)
        bindings.videoList.layoutManager = layoutManager

        initContent()
    }

    private fun initContent(){
        val headTitle = "$audioMediaType: $title"
        bindings.mediaTitle.text = headTitle

        val audiosAdapter = VideoViewAdapter(object: VideoActionListener {
            override fun onVideoItemClicked(videoItem: VideoContent) {
                TODO("Not yet implemented")
            }

            override fun onVideoItemLongClicked(videoItem: VideoContent) {
                val videoDetails = VideoDetails(videoItem)
                videoDetails.show(childFragmentManager,videoDetails.javaClass.canonicalName)
            }

        })
        audiosAdapter.submitList(videos)
        bindings.videoList.adapter = audiosAdapter
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