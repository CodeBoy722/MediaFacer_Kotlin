package com.codeboy.mediafacerkotlin.viewAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codeboy.mediafacer.models.AudioAlbumContent
import com.codeboy.mediafacer.models.AudioArtistContent
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.ArtistItemBinding

class ArtistAdapter: ListAdapter<AudioArtistContent, ArtistAdapter.ArtistViewHolder>(ArtistDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val bindingView = ArtistItemBinding.inflate(inflater,parent,false)
        return ArtistViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
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

    class ArtistViewHolder(private val bindings: ArtistItemBinding)
        : RecyclerView.ViewHolder(bindings.root){
        lateinit var item: AudioArtistContent
        fun bind(){
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
    }

}