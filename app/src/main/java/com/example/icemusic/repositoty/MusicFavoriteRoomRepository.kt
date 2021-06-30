package com.example.icemusic.repositoty

import android.app.Application
import com.example.icemusic.dao.MusicFavoriteDao
import com.example.icemusic.helper.MusicRoom
import com.example.icemusic.model.IceMusic
import com.example.icemusic.model.MusicFavorite

class MusicFavoriteRoomRepository(application: Application) {
    private val favoriteDao:MusicFavoriteDao

    init {
    val musicRoom = MusicRoom.getInstance(application)
        favoriteDao = musicRoom.favoriteDao
    }

    suspend fun insertFavoriteMusic(music: MusicFavorite) = favoriteDao.insertMusicFavorite(music)
    suspend fun deleteFavoriteMusic(music:MusicFavorite) = favoriteDao.deleteMusicFavorite(music)
    suspend fun getAllMusicFavorite():MutableList<MusicFavorite> = favoriteDao.getAllFavoriteMusic()
}