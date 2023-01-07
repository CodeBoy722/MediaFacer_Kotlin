package com.codeboy.mediafacerkotlin.listeners

import com.codeboy.mediafacer.models.ImageContent

interface ImageActionListener {

    fun onImageItemClicked(imagePosition: Int, imageList: ArrayList<ImageContent>)

    fun  onImageItemLongClicked(imageItem: ImageContent)

}