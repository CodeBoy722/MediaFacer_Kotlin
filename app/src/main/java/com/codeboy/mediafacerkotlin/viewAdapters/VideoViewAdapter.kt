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
import com.codeboy.mediafacer.models.VideoContent
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.VideoItemBinding

class VideoViewAdapter : ListAdapter<VideoContent, VideoViewAdapter.VideoViewHolder>(VideoDiffUtil()){

    var lastPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val bindingView = VideoItemBinding.inflate(inflater,parent,false)
        return VideoViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        if(holder.adapterPosition > lastPosition){
            val anim: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.animation_fall_down)
            holder.itemView.startAnimation(anim)
            lastPosition = holder.adapterPosition
        }
        holder.item = getItem(position)
        holder.bind()
    }

    private class VideoDiffUtil : DiffUtil.ItemCallback<VideoContent>() {
        override fun areItemsTheSame(oldItem: VideoContent, newItem: VideoContent): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: VideoContent, newItem: VideoContent): Boolean {
            return oldItem.id == newItem.id
        }
    }

    class VideoViewHolder(private val bindings: VideoItemBinding)
        : RecyclerView.ViewHolder(bindings.root), View.OnClickListener{
        lateinit var item: VideoContent
        fun bind(){
            Glide.with(bindings.videoPreview)
                .load(item.videoUri)
                .apply(RequestOptions().centerCrop())
                .into(bindings.videoPreview)
            bindings.play.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {

        }
    }



}