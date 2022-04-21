package com.codeboy.mediafacerkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.codeboy.mediafacer.MediaFacer
import com.codeboy.mediafacer.VideoGet

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        MediaFacer.initialize()

    }

}