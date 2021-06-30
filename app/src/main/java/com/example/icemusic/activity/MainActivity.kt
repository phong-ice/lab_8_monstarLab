package com.example.icemusic.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.icemusic.R
import com.example.icemusic.service.MusicService

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Intent(this,MusicService::class.java).also {
            startService(it)
        }
        startActivity(Intent(this,Content::class.java))
        finish()
    }
}