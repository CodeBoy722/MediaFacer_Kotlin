package com.codeboy.mediafacerkotlin.viewAdapters

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codeboy.mediafacer.models.VideoContent
import com.codeboy.mediafacerkotlin.databinding.VideoItemBinding

class VideoViewAdapter(): ListAdapter<VideoContent, VideoViewAdapter.VideoViewHolder>(VideoDiffUtil()){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    private class VideoDiffUtil : DiffUtil.ItemCallback<VideoContent>() {
        override fun areItemsTheSame(oldItem: VideoContent, newItem: VideoContent): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: VideoContent, newItem: VideoContent): Boolean {
            return oldItem.id == newItem.id
        }
    }

    class VideoViewHolder(private val bindings: VideoItemBinding): RecyclerView.ViewHolder(bindings.root){

        fun bind(){

        }

    }



}