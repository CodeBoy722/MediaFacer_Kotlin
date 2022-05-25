package com.codeboy.mediafacerkotlin.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.FragmentAudioMediaDetailsBinding
import com.codeboy.mediafacerkotlin.viewAdapters.AudioViewAdapter

class AudioMediaDetails() : Fragment() {

    private lateinit var bindings: FragmentAudioMediaDetailsBinding
    private lateinit var audioMediaType: String
    private lateinit var title: String
    private lateinit var audios: ArrayList<AudioContent>

    constructor(
        audioMediaType: String,
        title: String,
        audios: ArrayList<AudioContent>,
    ): this(){
        this.audioMediaType = audioMediaType
        this.title  = title
        this.audios = audios
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {
        return inflater.inflate(R.layout.fragment_audio_media_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentAudioMediaDetailsBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner

        bindings.audioList.hasFixedSize()
        bindings.audioList.setHasFixedSize(true)
        bindings.audioList.setItemViewCacheSize(20)
        val layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        bindings.audioList.layoutManager = layoutManager

        initContent()
    }

    private fun initContent(){
        val headTitle = "$audioMediaType: $title"
        bindings.mediaTitle.text = headTitle

        val audiosAdapter = AudioViewAdapter()
        audiosAdapter.submitList(audios)
        bindings.audioList.adapter = audiosAdapter
    }

}