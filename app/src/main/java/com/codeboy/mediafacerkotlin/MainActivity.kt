package com.codeboy.mediafacerkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.databinding.DataBindingUtil
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.MediaFacer.Companion.externalVideoContent
import com.codeboy.mediafacerkotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var bindings: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindings = DataBindingUtil.setContentView(this, R.layout.activity_main)
        bindings.lifecycleOwner = this

        val mediaFacer = MediaFacer()
        val videosList = mediaFacer.withVideoPagination(0,500,true).getVideos(this,externalVideoContent)
        val allVideos = mediaFacer.getVideos(this,externalVideoContent)
    }

}