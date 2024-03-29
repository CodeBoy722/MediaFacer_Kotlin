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
import com.codeboy.mediafacerkotlin.utils.Utils

class ImageViewAdapter(private val listener: ImageActionListener)
    : ListAdapter<ImageContent, ImageViewAdapter.ImageViewHolder>(Utils.ImageDiffUtil()) {

    private var lastPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val bindingView = ImageItemBinding.inflate(inflater,parent,false)
        return ImageViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        if(holder.layoutPosition > lastPosition){
            val anim: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.animation_fall_down)
            holder.itemView.startAnimation(anim)
            lastPosition = holder.layoutPosition
        }
        holder.item = getItem(position)
        holder.bind()
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
            val imageList = ArrayList<ImageContent>()
            imageList.addAll(currentList)
            listener.onImageItemClicked(layoutPosition, imageList)
        }

        override fun onLongClick(v: View?): Boolean {
            listener.onImageItemLongClicked(item)
            return true
        }
    }


}