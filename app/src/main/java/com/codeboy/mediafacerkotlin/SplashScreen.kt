package com.codeboy.mediafacerkotlin

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.codeboy.mediafacerkotlin.databinding.ActivitySplashScreenBinding

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {

    lateinit var bindings: ActivitySplashScreenBinding
    private val requestStorage = 1
    private var granted = false
    lateinit var handler : Handler
    private lateinit var run : Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings = DataBindingUtil.setContentView(this,R.layout.activity_splash_screen)
        bindings.lifecycleOwner = this

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            when {
                hasPermissions(
                    this,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.WAKE_LOCK
                ) -> { moveToMain() }
                else -> requestStoragePermission()
            }
        }else{
            when {
                hasPermissions(
                    this,
                    //Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WAKE_LOCK
                ) || Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> { moveToMain() }
                else -> requestStoragePermission()
            }
        }
    }

    private fun requestStoragePermission() {
        Log.w("SplashScreen", "Storage permission is not granted. Requesting permission")
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.WAKE_LOCK)
        } else {
            arrayOf(
                //Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WAKE_LOCK)
        }

        ActivityCompat.requestPermissions(this, permissions, requestStorage)
    }

    private fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
        val perm = permissions.size
        var numGranted = 0
        when {
            context != null -> {
                permissions.forEach { permission ->
                    Log.d("SplashScreen", "Checking permission : $permission")
                    when {
                        ActivityCompat.checkSelfPermission(context,permission) != PackageManager.PERMISSION_GRANTED -> {
                            Log.w("SplashScreen", "not granted : $permission")
                        }
                        else -> {
                            Log.d("SplashScreen", "granted : $permission")
                            numGranted++
                        }
                    }
                }
            }
        }
        granted = numGranted == perm
        return granted
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when {
            requestCode != requestStorage -> {
                Log.d("SplashScreen","Got unexpected permission result: $requestCode"
                )
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
                return
            }
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED -> {
                Log.d("SplashScreen", "Storage permission granted")
                granted = true
                moveToMain()
            }
            else -> {
                Toast.makeText(this, "Storage permission not granted", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun moveToMain(){
        val main = Intent(this@SplashScreen, MainActivity::class.java)
        main.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        handler = Handler(mainLooper)
        run = Runnable {
            startActivity(main)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        handler.postDelayed( run, 3000)
    }


}