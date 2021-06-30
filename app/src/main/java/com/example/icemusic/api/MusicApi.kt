package com.example.icemusic.api

import com.example.icemusic.model.IceMusic
import com.example.icemusic.model.RequestGetInfo
import com.example.icemusic.model.RequestSearch
import com.example.icemusic.model.StateRequest
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MusicApi {

    @GET("xhr/chart-realtime?songId=0&videoId=0&albumId=0&chart=song&time=-1")
    suspend fun getTop100Music(): Response<StateRequest>

    @GET("xhr/recommend")
    suspend fun getMusicRelate(
        @Query("type") type: String,
        @Query("id") idMusic: String
    ): Response<StateRequest>

    @GET("/complete")
    suspend fun searchMusic(
        @Query("type") type: String,
        @Query("num") num: String,
        @Query("query") searchPattern: String,
    ): Response<RequestSearch>

    @GET("/xhr/media/get-info")
    suspend fun getInfoMusic(
        @Query("type") type: String,
        @Query("id") idMusic: String
    ):Response<RequestGetInfo>
}