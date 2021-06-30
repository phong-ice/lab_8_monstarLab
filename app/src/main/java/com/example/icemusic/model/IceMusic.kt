package com.example.icemusic.model

import android.net.Uri
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.jetbrains.annotations.NotNull

data class IceMusic(
     var id:String,
     var name:String,
     var artists_names:String,
     var thumbnail:String,
     var duration:Int,
     @NotNull
     var uri:Uri?
)
