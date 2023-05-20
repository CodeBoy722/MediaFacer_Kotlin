package com.codeboy.mediafacer.adapters

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
import com.codeboy.mediafacer.R
import com.codeboy.mediafacer.databinding.AudioSelectItemBinding
import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacer.tools.MediaSelectionListener

internal class AudioContentAdapter(private val defaultArt: Int,private val listener: MediaSelectionListener)
    : ListAdapter<AudioContent, AudioContentAdapter.AudioSelectViewHolder>(AudioDiffUtil()) {

    var lastPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioSelectViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val bindingView = AudioSelectItemBinding.inflate(inflater, parent, false)
        return AudioSelectViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: AudioSelectViewHolder, position: Int) {
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

    inner class AudioSelectViewHolder(private val bindings: AudioSelectItemBinding)
        : RecyclerView.ViewHolder(bindings.root), View.OnClickListener{
        lateinit var item: AudioContent
        fun bind(){
            bindings.root.setOnClickListener(this)
            Glide.with(bindings.art)
                .load(item.artUri)
                .apply(RequestOptions().centerCrop().circleCrop())
                .placeholder(R.drawable.music_placeholder)
                .into(bindings.art)

            //bindings.selector.visibility = View.GONE
            bindings.artist.text = item.artist
            bindings.title.text = item.title
        }

        override fun onClick(v: View?) {
            bindings.selector.isChecked = !bindings.selector.isChecked
            if(bindings.selector.isChecked){
                //bindings.selector.visibility = View.VISIBLE
                //todo add to view model select list
            }else{
                //bindings.selector.visibility = View.GONE
                //todo remove from view model select list
            }
        }
    }

}