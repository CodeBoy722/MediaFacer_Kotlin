package com.codeboy.mediafacerkotlin.viewAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codeboy.mediafacer.models.AudioAlbumContent
import com.codeboy.mediafacer.models.AudioArtistContent
import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.ArtistItemBinding
import com.codeboy.mediafacerkotlin.listeners.AudioContainerActionListener

class ArtistAdapter(private val mediaListener: AudioContainerActionListener): ListAdapter<AudioArtistContent, ArtistAdapter.ArtistViewHolder>(ArtistDiffUtil()) {

    var lastPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val bindingView = ArtistItemBinding.inflate(inflater,parent,false)
        return ArtistViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        if(holder.adapterPosition > lastPosition){
            val anim: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.animation_fall_down)
            holder.itemView.startAnimation(anim)
            lastPosition = holder.adapterPosition
        }
        holder.item = getItem(position)
        holder.bind()
    }

    private class ArtistDiffUtil : DiffUtil.ItemCallback<AudioArtistContent>() {
        override fun areItemsTheSame(oldItem: AudioArtistContent, newItem: AudioArtistContent): Boolean {
            return oldItem.artistName == newItem.artistName
        }

        override fun areContentsTheSame(oldItem: AudioArtistContent, newItem: AudioArtistContent): Boolean {
            return oldItem.artistName == newItem.artistName
        }
    }

    inner class ArtistViewHolder(private val bindings: ArtistItemBinding)
        : RecyclerView.ViewHolder(bindings.root), View.OnClickListener{
        lateinit var item: AudioArtistContent
        fun bind(){
            bindings.root.setOnClickListener(this)
            Glide.with(bindings.artistArt)
                .load(item.albums[0].albumArtUri)
                .apply(RequestOptions().centerCrop()).circleCrop()
                .placeholder(R.drawable.music_placeholder)
                .into(bindings.artistArt)

            bindings.artistName.text = item.artistName
            val albumsSize = item.albums.size.toString()
            var numSongs = 0
            for(album:AudioAlbumContent  in item.albums){
                numSongs += album.albumAudios.size
            }
            val bucketSizeText = "$albumsSize Albums and $numSongs Songs"
            bindings.albumsAnSongs.text = bucketSizeText
        }

        override fun onClick(v: View?) {
            val audios: ArrayList<AudioContent> = ArrayList()
            item.albums.forEach { album: AudioAlbumContent ->
                audios.addAll(album.albumAudios)
            }
            mediaListener.onAudioContainerClicked("Artist", item.artistName, audios)
        }
    }

}