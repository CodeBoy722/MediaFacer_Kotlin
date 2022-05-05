package com.codeboy.mediafacerkotlin.viewAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacerkotlin.databinding.ImageItemBinding

class ImageViewAdapter : ListAdapter<ImageContent, ImageViewAdapter.ImageViewHolder>(ImageDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val bindingView = ImageItemBinding.inflate(inflater,parent,false)
        return ImageViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.item = getItem(position)
        holder.bind()
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
        lateinit var item: ImageContent
        fun bind(){
            Glide.with(bindings.image)
                .load(item.imageUri)
                .apply(RequestOptions().centerCrop())
                .into(bindings.image)
        }
    }


}