package com.example.icemusic.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://mp3.zing.vn/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val retrofitSearch by lazy {
        Retrofit.Builder()
            .baseUrl("http://ac.mp3.zing.vn/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api:MusicApi by lazy {
        retrofit.create(MusicApi::class.java)
    }

    val apiSearch:MusicApi by lazy {
        retrofitSearch.create(MusicApi::class.java)
    }
}