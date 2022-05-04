package com.codeboy.mediafacerkotlin.viewAdapters

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacerkotlin.databinding.AudioItemBinding

class AudioViewAdapter() : ListAdapter<AudioContent, AudioViewAdapter.AudioViewHolder>(AudioDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    private class AudioDiffUtil : DiffUtil.ItemCallback<AudioContent>() {
        override fun areItemsTheSame(oldItem: AudioContent, newItem: AudioContent): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: AudioContent, newItem: AudioContent): Boolean {
            return oldItem.musicId == newItem.musicId
        }
    }


    class AudioViewHolder(private val binding: AudioItemBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(){

        }

    }

}