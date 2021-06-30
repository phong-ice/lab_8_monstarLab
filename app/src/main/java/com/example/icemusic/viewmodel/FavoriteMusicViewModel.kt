package com.example.icemusic.viewmodel

import android.app.Application
import android.content.ContentUris
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.icemusic.model.IceMusic
import com.example.icemusic.model.MusicFavorite
import com.example.icemusic.repositoty.MusicFavoriteRoomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoriteMusicViewModel(private val repository: MusicFavoriteRoomRepository) : ViewModel() {

    val listFavorite = MutableLiveData<MutableList<MusicFavorite>>()
    val listMusicOffline = MutableLiveData<MutableList<IceMusic>>()

    fun getFavoriteMusic() {
        viewModelScope.launch {
            listFavorite.value = repository.getAllMusicFavorite()
        }
    }

    fun insertMusicFavorite(music: MusicFavorite) {
        viewModelScope.launch {
            repository.insertFavoriteMusic(music)
        }
    }

    fun deleteMusicFavorite(music: MusicFavorite) {
        viewModelScope.launch {
            repository.deleteFavoriteMusic(music)
        }
    }

    fun getAllMusicOffline(application: Application) {
        val listMusic: MutableList<IceMusic> = mutableListOf()
        viewModelScope.launch {
            val collection = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
                else -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION
            )
            application.applicationContext.contentResolver.query(
                collection,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(0)
                    val name = cursor.getString(1)
                    val artists = cursor.getString(2)
                    val duration = cursor.getInt(3)
                    val uri =
                        ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val iceMusic = IceMusic(id.toString(),name,artists,"",duration,uri)
                    listMusic.add(iceMusic)
                }
                withContext(Dispatchers.Main){
                    listMusicOffline.value = listMusic
                }
            }
        }


    }

    class ViewModelProvide(private val repository: MusicFavoriteRoomRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return FavoriteMusicViewModel(repository) as T
        }

    }
}