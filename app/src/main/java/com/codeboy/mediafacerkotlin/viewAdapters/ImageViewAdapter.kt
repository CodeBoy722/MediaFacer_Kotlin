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
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.ImageItemBinding
import com.codeboy.mediafacerkotlin.listeners.ImageActionListener

class ImageViewAdapter(private val listener: ImageActionListener)
    : ListAdapter<ImageContent, ImageViewAdapter.ImageViewHolder>(ImageDiffUtil()) {

    var lastPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val bindingView = ImageItemBinding.inflate(inflater,parent,false)
        return ImageViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        if(holder.adapterPosition > lastPosition){
            val anim: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.animation_fall_down)
            holder.itemView.startAnimation(anim)
            lastPosition = holder.adapterPosition
        }
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


    inner class ImageViewHolder(private val bindings:ImageItemBinding):
        RecyclerView.ViewHolder(bindings.root), View.OnClickListener, View.OnLongClickListener{
        lateinit var item: ImageContent
        fun bind(){
            bindings.root.setOnLongClickListener(this)
            bindings.root.setOnClickListener(this)
            Glide.with(bindings.image)
                .load(item.imageUri)
                .apply(RequestOptions().centerCrop())
                .into(bindings.image)
        }

        override fun onClick(p0: View?) {

        }

        override fun onLongClick(v: View?): Boolean {
            listener.onImageItemLongClicked(item)
            return true
        }
    }


}