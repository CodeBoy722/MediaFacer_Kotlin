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
import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.AudioItemBinding

class AudioViewAdapter : ListAdapter<AudioContent, AudioViewAdapter.AudioViewHolder>(AudioDiffUtil()) {

    var lastPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val bindingView: AudioItemBinding = AudioItemBinding.inflate(inflater, parent, false)
        return AudioViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        if(holder.adapterPosition > lastPosition){
            val anim: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.animation_fall_down)
            holder.itemView.startAnimation(anim)
            lastPosition = holder.adapterPosition
        }
        holder.item = getItem(position)
        holder.bind()
    }

    private class AudioDiffUtil : DiffUtil.ItemCallback<AudioContent>() {
        override fun areItemsTheSame(oldItem: AudioContent, newItem: AudioContent): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: AudioContent, newItem: AudioContent): Boolean {
            return oldItem.musicId == newItem.musicId
        }
    }


    class AudioViewHolder(private val bindings: AudioItemBinding)
        : RecyclerView.ViewHolder(bindings.root), View.OnClickListener{
        lateinit var item: AudioContent
        fun bind(){
            Glide.with(bindings.art)
                .load(item.artUri)
                .apply(RequestOptions().centerCrop().circleCrop())
                .placeholder(R.drawable.music_placeholder)
                .into(bindings.art)

            bindings.artist.text = item.artist
            bindings.title.text = item.title
            bindings.play.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {

        }
    }

}