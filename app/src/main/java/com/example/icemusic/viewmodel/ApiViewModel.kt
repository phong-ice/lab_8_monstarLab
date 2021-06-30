package com.example.icemusic.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.icemusic.model.DataInfo
import com.example.icemusic.model.IceMusic
import com.example.icemusic.model.MusicSearch
import com.example.icemusic.repositoty.ApiRepository
import kotlinx.coroutines.launch

class ApiViewModel(private val repo: ApiRepository) : ViewModel() {

    val listTop100Music: MutableLiveData<MutableList<IceMusic>> = MutableLiveData()
    val listRelateMusic: MutableLiveData<MutableList<IceMusic>> = MutableLiveData()
    val listMusicSearched: MutableLiveData<MutableList<MusicSearch>> = MutableLiveData()
    val dataInfoLiveData: MutableLiveData<DataInfo> = MutableLiveData()

    fun getTop100Music() {
        viewModelScope.launch {
            val response = repo.getTop100Music()
            if (response.isSuccessful) {
                listTop100Music.value = response.body()?.data?.song
            }
        }
    }

    fun getRelateMusic(type: String, idMusic: String) {
        viewModelScope.launch {
            val response = repo.getRelateMusic(type, idMusic)
            if (response.isSuccessful) {
                listRelateMusic.value = response.body()?.data?.items
            }
        }
    }

    fun searchMusic(searchPattern: String) {
        viewModelScope.launch {
            val response = repo.searchMusic("artist,song,key,code", "20", searchPattern)
            if (response.isSuccessful) {
                response.body()?.data?.size?.let {
                    if (it > 0){
                        listMusicSearched.value = response.body()?.data?.get(0)?.song
                    }
                }
            }
        }
    }

     fun getInfoMusic(idMusic: String) {
        viewModelScope.launch {
            val response = repo.getMusicInfo("audio", idMusic)
            if (response.isSuccessful) {
                dataInfoLiveData.value = response.body()?.data
            }
        }
    }


    class ApiViewModelProvide(val repo: ApiRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ApiViewModel(repo) as T
        }
    }
}