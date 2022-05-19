package com.codeboy.mediafacerkotlin.viewAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codeboy.mediafacer.models.AudioGenreContent
import com.codeboy.mediafacerkotlin.databinding.GenreItemBinding

class GenreAdapter: ListAdapter<AudioGenreContent, GenreAdapter.AudioGenreViewHolder>(AudioGenreDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioGenreViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val bindingView = GenreItemBinding.inflate(inflater,parent,false)
        return AudioGenreViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: AudioGenreViewHolder, position: Int) {
        holder.item = getItem(position)
        holder.bind()
    }

    private class AudioGenreDiffUtil : DiffUtil.ItemCallback<AudioGenreContent>() {
        override fun areItemsTheSame(oldItem: AudioGenreContent, newItem: AudioGenreContent): Boolean {
            return oldItem.genreName == newItem.genreName
        }

        override fun areContentsTheSame(oldItem: AudioGenreContent, newItem: AudioGenreContent): Boolean {
            return oldItem.genreName == newItem.genreName
        }
    }

    class AudioGenreViewHolder(private val bindings: GenreItemBinding)
        : RecyclerView.ViewHolder(bindings.root){
        lateinit var item: AudioGenreContent
        fun bind(){
            bindings.genreName.text = item.genreName
            val bucketSize = item.audios.size.toString()
            val bucketSizeText = "$bucketSize Songs for this Genre"
            bindings.numSongs.text = bucketSizeText
        }
    }

}