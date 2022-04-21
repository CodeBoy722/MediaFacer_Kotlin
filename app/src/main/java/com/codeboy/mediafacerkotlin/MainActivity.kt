package com.codeboy.mediafacerkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import com.codeboy.mediafacer.MediaFacer

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MediaFacer(this).withVideoPagination(0,500,true).findAudioAlbums()
        MediaFacer(this).findAudioAlbums()

    }

}