package com.example.icemusic.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.icemusic.model.IceMusic
import com.example.icemusic.model.MusicFavorite

@Dao
interface MusicFavoriteDao {
    @Insert
    suspend fun insertMusicFavorite(music:MusicFavorite)

    @Delete
    suspend fun deleteMusicFavorite(music:MusicFavorite)

    @Query("SELECT * FROM music_favorite")
    suspend fun getAllFavoriteMusic():MutableList<MusicFavorite>
}