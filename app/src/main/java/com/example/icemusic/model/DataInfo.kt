package com.example.icemusic.model

data class DataInfo(
    val artists:MutableList<ArtistsInfo>,
    val genres:MutableList<GenresMusic>,
    val info:MusicInfo
)