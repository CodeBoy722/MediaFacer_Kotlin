package com.codeboy.mediafacerkotlin.viewAdapters

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacerkotlin.databinding.ImageItemBinding

class ImageViewAdapter(): ListAdapter<ImageContent, ImageViewAdapter.ImageViewHolder>(ImageDiffUtil()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        TODO("Not yet implemented")
    }


    private class ImageDiffUtil : DiffUtil.ItemCallback<ImageContent>() {
        override fun areItemsTheSame(oldItem: ImageContent, newItem: ImageContent): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: ImageContent, newItem: ImageContent): Boolean {
            return oldItem.imageId == newItem.imageId
        }
    }


    class ImageViewHolder(private val bindings:ImageItemBinding): RecyclerView.ViewHolder(bindings.root){

        fun bind(){

        }
    }


}