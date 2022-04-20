package com.codeboy.mediafacer

import android.content.Context

class MediaFacer() {



    companion object {

        internal fun withVideoMedia(context: Context): VideoGet{
            return VideoGet(context)
        }

        internal fun withImageMedia(context: Context): ImageGet{
            return ImageGet(context)
        }

        internal fun withAudioMedia(context: Context): AudioGet{
            return AudioGet(context)
        }
    }

}