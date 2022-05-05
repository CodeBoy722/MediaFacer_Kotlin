package com.codeboy.mediafacerkotlin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.MediaFacer.Companion.externalAudioContent
import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.FragmentAudiosBinding
import com.codeboy.mediafacerkotlin.utils.EndlessScrollListener
import com.codeboy.mediafacerkotlin.viewAdapters.AudioViewAdapter

class AudiosFragment : Fragment() {

    private lateinit var bindings: FragmentAudiosBinding
    var audios: MutableLiveData<ArrayList<AudioContent>> = MutableLiveData()
    var paginationStart = 0
    var paginationLimit = 500
    var shouldPaginate = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_audios, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentAudiosBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner
        initAudios()
    }

    private fun initAudios(){

        // init and setup your recyclerview with a layout manager
        bindings.audiosList.hasFixedSize()
        bindings.audiosList.setHasFixedSize(true)
        bindings.audiosList.setItemViewCacheSize(20)
        val layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        bindings.audiosList.layoutManager = layoutManager
        bindings.audiosList.itemAnimator = null

        //init your adapter and bind it to recyclerview
        val audiosAdapter = AudioViewAdapter()
        bindings.audiosList.adapter = audiosAdapter

        //observe the LifeData list of items and feed them to recyclerview each time there is an update
        audios.observe(viewLifecycleOwner) {
            audiosAdapter.submitList(it)
        }

        //get paginated audio items using MediaFacer, remember to set paginationStart to size+1 of
        //of items gotten from MediaFacer to prepare for getting next page of items when user scroll
        val videosList = ArrayList<AudioContent>()
        videosList.addAll(
            MediaFacer()
                .withPagination(paginationStart,paginationLimit,shouldPaginate)
                .getAudios(requireActivity(),externalAudioContent)
        )
        paginationStart = videosList.size+1
        audios.value = videosList
        Toast.makeText(requireActivity(), "gotten new audio data "+videosList.size.toString(), Toast.LENGTH_LONG).show()

        //adding EndlessScrollListener to our recyclerview to auto paginate items when user is
        //scrolling towards end of list
        bindings.audiosList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                videosList.addAll(
                    MediaFacer()
                        .withPagination(paginationStart,paginationLimit,shouldPaginate)
                        .getAudios(requireActivity(),externalAudioContent)
                )
                paginationStart = videosList.size+1
                audios.value = videosList
                Toast.makeText(requireActivity(), "gotten new audio data "+videosList.size.toString(), Toast.LENGTH_LONG).show()
            }
        })

        //normal RecyclerView.OnScrollListener(), you can use this if you wish
       /* bindings.audiosList.addOnScrollListener(object: RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(layoutManager.findLastVisibleItemPosition() == layoutManager.itemCount-1){

                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })*/
    }

}