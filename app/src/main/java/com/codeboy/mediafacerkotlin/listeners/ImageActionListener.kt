package com.codeboy.mediafacerkotlin.listeners

import com.codeboy.mediafacer.models.ImageContent

interface ImageActionListener {

    fun onImageItemClicked(imageItem: ImageContent)

    fun  onImageItemLongClicked(imageItem: ImageContent)

}