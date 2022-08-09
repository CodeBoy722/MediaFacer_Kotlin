package com.codeboy.mediafacerkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.codeboy.mediafacerkotlin.databinding.ActivityPlayerBinding

//exo player activity
class PlayerActivity : AppCompatActivity() {

    private lateinit var bindings: ActivityPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings = DataBindingUtil.setContentView(this,R.layout.activity_player)
        bindings.lifecycleOwner = this
    }


}