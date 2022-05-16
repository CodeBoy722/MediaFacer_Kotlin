package com.codeboy.mediafacerkotlin.viewAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codeboy.mediafacer.models.VideoFolderContent
import com.codeboy.mediafacerkotlin.databinding.VideoItemBinding

class VideoFolderViewAdapter: ListAdapter<VideoFolderContent, VideoFolderViewAdapter.VideoFolderViewHolder>(
    VideoFolderViewAdapter.VideoFolderDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoFolderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val bindingView = VideoItemBinding.inflate(inflater,parent,false)
        return VideoFolderViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: VideoFolderViewHolder, position: Int) {
        holder.item = getItem(position)
        holder.bind()
    }

    private class VideoFolderDiffUtil : DiffUtil.ItemCallback<VideoFolderContent>() {
        override fun areItemsTheSame(oldItem: VideoFolderContent, newItem: VideoFolderContent): Boolean {
            return oldItem.folderName == newItem.folderName
        }

        override fun areContentsTheSame(oldItem: VideoFolderContent, newItem: VideoFolderContent): Boolean {
            return oldItem.folderName == newItem.folderName
        }
    }


    class VideoFolderViewHolder(private val bindings: VideoItemBinding)
        : RecyclerView.ViewHolder(bindings.root){
        lateinit var item: VideoFolderContent
            fun bind(){

            }
    }

}