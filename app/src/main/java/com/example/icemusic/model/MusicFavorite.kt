package com.example.icemusic.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "music_favorite")
data class MusicFavorite (
    @PrimaryKey(autoGenerate = true)
    var idFavorite:Int,
    var id: String,
    var name: String,
    var artists_names: String,
    var thumbnail: String,
    var duration: Int,
    var uri:String?
){
}