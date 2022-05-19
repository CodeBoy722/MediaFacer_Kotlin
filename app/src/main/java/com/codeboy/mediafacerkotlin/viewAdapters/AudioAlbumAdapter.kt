package com.codeboy.mediafacerkotlin.viewAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codeboy.mediafacer.models.AudioAlbumContent
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.AudioAlbumItemBinding

class AudioAlbumAdapter: ListAdapter<AudioAlbumContent, AudioAlbumAdapter.AudioAlbumViewHolder>(AudioAlbumDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioAlbumViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val bindingView = AudioAlbumItemBinding.inflate(inflater,parent,false)
        return AudioAlbumViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: AudioAlbumViewHolder, position: Int) {
        holder.item = getItem(position)
        holder.bind()
    }

    private class AudioAlbumDiffUtil : DiffUtil.ItemCallback<AudioAlbumContent>() {
        override fun areItemsTheSame(oldItem: AudioAlbumContent, newItem: AudioAlbumContent): Boolean {
            return oldItem.albumName == newItem.albumName
        }

        override fun areContentsTheSame(oldItem: AudioAlbumContent, newItem: AudioAlbumContent): Boolean {
            return oldItem.albumName == newItem.albumName
        }
    }

    class AudioAlbumViewHolder(private val bindings: AudioAlbumItemBinding)
        : RecyclerView.ViewHolder(bindings.root){
        lateinit var item: AudioAlbumContent
        fun bind(){

            Glide.with(bindings.albumArt)
                .load(item.albumArtUri)
                .apply(RequestOptions().centerCrop()).circleCrop()
                .placeholder(R.drawable.music_placeholder)
                .into(bindings.albumArt)

            bindings.albumName.text = item.albumName
            val bucketSize = item.albumAudios.size.toString()
            val bucketSizeText = "$bucketSize Songs in this Album"
            bindings.numSongs.text = bucketSizeText
        }
    }

}