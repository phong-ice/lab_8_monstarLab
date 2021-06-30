package com.example.icemusic.model

import com.google.gson.annotations.SerializedName

data class StateRequest (
    var err:Int,
    var msg:String,
    var data:IceData
    )