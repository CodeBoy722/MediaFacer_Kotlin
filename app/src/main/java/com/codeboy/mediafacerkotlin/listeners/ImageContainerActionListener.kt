package com.codeboy.mediafacerkotlin.listeners

import com.codeboy.mediafacer.models.ImageContent

interface ImageContainerActionListener {

    fun onImageFolderClicked(mediaType: String, title: String, images: ArrayList<ImageContent>)

}