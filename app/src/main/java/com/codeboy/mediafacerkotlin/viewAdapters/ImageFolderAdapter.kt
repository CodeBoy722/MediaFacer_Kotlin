package com.codeboy.mediafacerkotlin.viewAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codeboy.mediafacer.models.ImageFolderContent
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.databinding.ImageFolderItemBinding
import com.codeboy.mediafacerkotlin.listeners.ImageContainerActionListener

class ImageFolderAdapter(private val listener: ImageContainerActionListener): ListAdapter<ImageFolderContent, ImageFolderAdapter.ImageFolderViewHolder>(ImageBucketDiffUtil()) {

    var lastPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageFolderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val bindingView = ImageFolderItemBinding.inflate(inflater,parent,false)
        return ImageFolderViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: ImageFolderViewHolder, position: Int) {
        if(holder.layoutPosition > lastPosition){
            val anim: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.animation_fall_down)
            holder.itemView.startAnimation(anim)
            lastPosition = holder.layoutPosition
        }
        holder.item = getItem(position)
        holder.bind()
    }


    private class ImageBucketDiffUtil : DiffUtil.ItemCallback<ImageFolderContent>() {
        override fun areItemsTheSame(oldItem: ImageFolderContent, newItem: ImageFolderContent): Boolean {
            return oldItem.folderName == newItem.folderName
        }

        override fun areContentsTheSame(oldItem: ImageFolderContent, newItem: ImageFolderContent): Boolean {
            return oldItem.folderName == newItem.folderName
        }
    }

    inner class ImageFolderViewHolder(private val bindings: ImageFolderItemBinding)
        : RecyclerView.ViewHolder(bindings.root), View.OnClickListener{
        lateinit var item: ImageFolderContent
        fun bind(){
            bindings.root.setOnClickListener(this)
            bindings.imageFolderName.text = item.folderName
            val bucketSize = item.images.size.toString()
            val bucketSizeText = "$bucketSize Images in this folder"
            bindings.numOfImages.text = bucketSizeText
        }

        override fun onClick(v: View?) {
            listener.onImageFolderClicked("Folder",item.folderName,item.images)
        }
    }

}