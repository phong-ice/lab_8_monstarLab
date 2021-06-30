package com.example.icemusic.helper

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.icemusic.dao.MusicFavoriteDao
import com.example.icemusic.model.IceMusic
import com.example.icemusic.model.MusicFavorite

@Database(entities = [MusicFavorite::class], version = 5, exportSchema = false)
abstract class MusicRoom : RoomDatabase() {

    abstract val favoriteDao:MusicFavoriteDao

    companion object {
        private const val MUSIC_ROOM_DATABASE = "music_room_database"
        private var INSTANCE: MusicRoom? = null
        fun getInstance(context: Context): MusicRoom {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext, MusicRoom::class.java,
                        MUSIC_ROOM_DATABASE
                    ).fallbackToDestructiveMigration().build()
                }
                return instance
            }
        }
    }
}