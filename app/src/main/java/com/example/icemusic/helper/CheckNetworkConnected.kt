package com.example.icemusic.helper

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object CheckNetworkConnected {
    fun isConnectedInternet(context: Context):Boolean{
        var isInternet = false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                connectivityManager?.run {
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.run {
                        isInternet = when{
                            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                            hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
                            else -> false
                        }
                    }
                }
           }else{
               connectivityManager.activeNetworkInfo?.run {
                   isInternet = when(type){
                       ConnectivityManager.TYPE_WIFI -> true
                       ConnectivityManager.TYPE_MOBILE -> true
                       ConnectivityManager.TYPE_VPN -> true
                       else -> false
                   }
               }
           }
        return isInternet
    }
}