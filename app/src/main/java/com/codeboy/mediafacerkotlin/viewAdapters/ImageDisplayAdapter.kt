package com.codeboy.mediafacerkotlin.viewAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacerkotlin.databinding.ImageDisplayBinding
import com.codeboy.mediafacerkotlin.listeners.ImageDisplayItemListener
import com.codeboy.mediafacerkotlin.utils.Utils

class ImageDisplayAdapter(val listener: ImageDisplayItemListener)
    : ListAdapter<ImageContent, ImageDisplayAdapter.ImageDisplayViewHolder>(Utils.ImageDiffUtil()){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageDisplayViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val bindingView = ImageDisplayBinding.inflate(inflater,parent,false)
        return ImageDisplayViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: ImageDisplayViewHolder, position: Int) {
        holder.item = getItem(position)
        holder.bind()
    }

    inner class ImageDisplayViewHolder(private val bindings: ImageDisplayBinding):
        RecyclerView.ViewHolder(bindings.root), View.OnClickListener{
        lateinit var item: ImageContent
        fun bind(){
            bindings.image.setOnClickListener(this)
            Glide.with(bindings.image)
                .load(item.imageUri)
                .apply(RequestOptions().centerInside())
                .into(bindings.image)
        }

        override fun onClick(p0: View?) {
            listener.onImageItemClicked()
        }
    }


}