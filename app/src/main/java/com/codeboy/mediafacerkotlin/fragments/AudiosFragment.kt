package com.codeboy.mediafacerkotlin.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.FragmentAudiosBinding
import com.codeboy.mediafacerkotlin.utils.EndlessScrollListener
import com.codeboy.mediafacerkotlin.viewAdapters.AudioViewAdapter
import com.codeboy.mediafacerkotlin.viewModels.AudioViewModel

class AudiosFragment : Fragment() {

    private lateinit var bindings: FragmentAudiosBinding
    private var paginationStart = 0
    private var paginationLimit = 50
    private var shouldPaginate = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_audios, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindings = FragmentAudiosBinding.bind(view)
        bindings.lifecycleOwner = viewLifecycleOwner
        initAudios()
    }

    @SuppressLint("NotifyDataSetChanged")
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

        val model = AudioViewModel()
        //observe the LifeData list of items and feed them to recyclerview each time there is an update
        model.audios.observe(viewLifecycleOwner) {
            audiosAdapter.submitList(it)
            //notifyDataSetChanged on adapter after submitting list to avoid scroll lagging on recyclerview
            paginationStart = it.size + 1
            audiosAdapter.notifyDataSetChanged()
        }

        //get paginated audio items using MediaFacer, remember to set paginationStart to size+1 of
        //of items gotten from MediaFacer to prepare for getting next page of items when user scroll
        //audiosList = ArrayList()
        model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)

        //adding EndlessScrollListener to our recyclerview to auto paginate items when user is
        //scrolling towards end of list
        bindings.audiosList.addOnScrollListener(object: EndlessScrollListener(layoutManager){
            override  fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                model.loadNewItems(requireActivity(),paginationStart,paginationLimit,shouldPaginate)
            }
        })
    }

}