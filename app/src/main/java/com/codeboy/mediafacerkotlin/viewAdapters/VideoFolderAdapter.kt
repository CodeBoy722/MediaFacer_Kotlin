package com.codeboy.mediafacerkotlin.viewAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codeboy.mediafacer.models.VideoFolderContent
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.VideoFolderItemBinding
import com.codeboy.mediafacerkotlin.listeners.VideoContainerActionListener

class VideoFolderAdapter(private val listener: VideoContainerActionListener): ListAdapter<VideoFolderContent, VideoFolderAdapter.VideoFolderViewHolder>(VideoBucketDiffUtil()) {

    var lastPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoFolderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val bindingView = VideoFolderItemBinding.inflate(inflater,parent,false)
        return VideoFolderViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: VideoFolderViewHolder, position: Int) {
        if(holder.layoutPosition > lastPosition){
            val anim: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.animation_fall_down)
            holder.itemView.startAnimation(anim)
            lastPosition = holder.layoutPosition
        }
        holder.item = getItem(position)
        holder.bind()
    }

    private class VideoBucketDiffUtil : DiffUtil.ItemCallback<VideoFolderContent>() {
        override fun areItemsTheSame(oldItem: VideoFolderContent, newItem: VideoFolderContent): Boolean {
            return oldItem.folderName == newItem.folderName
        }

        override fun areContentsTheSame(oldItem: VideoFolderContent, newItem: VideoFolderContent): Boolean {
            return oldItem.folderName == newItem.folderName
        }
    }

    inner class VideoFolderViewHolder(private val bindings: VideoFolderItemBinding)
        : RecyclerView.ViewHolder(bindings.root), View.OnClickListener{
        lateinit var item: VideoFolderContent
        fun bind(){
            bindings.root.setOnClickListener(this)
            bindings.videoFolderName.text = item.folderName
            val bucketSize = item.videos.size.toString()
            val bucketSizeText = "$bucketSize Videos in this folder"
            bindings.numOfVideos.text = bucketSizeText
        }

        override fun onClick(v: View?) {
            listener.onVideoFolderClicked("Folder",item.folderName,item.videos)
        }
    }

}