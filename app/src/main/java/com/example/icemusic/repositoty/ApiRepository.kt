package com.example.icemusic.repositoty

import com.example.icemusic.api.RetrofitInstance
import com.example.icemusic.model.IceMusic
import com.example.icemusic.model.RequestGetInfo
import com.example.icemusic.model.RequestSearch
import com.example.icemusic.model.StateRequest
import retrofit2.Response

class ApiRepository {
    suspend fun getTop100Music(): Response<StateRequest> = RetrofitInstance.api.getTop100Music()
    suspend fun getRelateMusic(type: String, idMusic: String): Response<StateRequest> =
        RetrofitInstance.api.getMusicRelate(type, idMusic)

    suspend fun searchMusic(type: String, num: String, query: String): Response<RequestSearch> =
        RetrofitInstance.apiSearch.searchMusic(type, num, query)

    suspend fun getMusicInfo(type: String, idMusic: String): Response<RequestGetInfo> =
        RetrofitInstance.api.getInfoMusic(type, idMusic)
}