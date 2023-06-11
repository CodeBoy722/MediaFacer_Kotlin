package com.codeboy.mediafacer.tools

import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacer.models.ImageContent
import com.codeboy.mediafacer.models.VideoContent

interface MediaSelectionListener {

 fun onMediaItemsSelected(audios: ArrayList<AudioContent>, videos: ArrayList<VideoContent>, images: ArrayList<ImageContent>)

}