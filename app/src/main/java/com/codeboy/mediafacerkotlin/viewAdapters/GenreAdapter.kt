package com.codeboy.mediafacerkotlin.viewAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codeboy.mediafacer.models.AudioGenreContent
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.GenreItemBinding
import com.codeboy.mediafacerkotlin.listeners.AudioContainerActionListener

class GenreAdapter(private val mediaListener: AudioContainerActionListener): ListAdapter<AudioGenreContent, GenreAdapter.AudioGenreViewHolder>(AudioGenreDiffUtil()) {

    var lastPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioGenreViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val bindingView = GenreItemBinding.inflate(inflater,parent,false)
        return AudioGenreViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: AudioGenreViewHolder, position: Int) {
        if(holder.adapterPosition > lastPosition){
            val anim: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.animation_fall_down)
            holder.itemView.startAnimation(anim)
            lastPosition = holder.adapterPosition
        }
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

    inner class AudioGenreViewHolder(private val bindings: GenreItemBinding)
        : RecyclerView.ViewHolder(bindings.root), View.OnClickListener{
        lateinit var item: AudioGenreContent
        fun bind(){
            bindings.root.setOnClickListener(this)
            bindings.genreName.text = item.genreName
            val bucketSize = item.audios.size.toString()
            val bucketSizeText = "$bucketSize Songs for this Genre"
            bindings.numSongs.text = bucketSizeText
        }

        override fun onClick(v: View?) {
            mediaListener.onAudioContainerClicked("Genre", item.genreName, item.audios)
        }
    }

}