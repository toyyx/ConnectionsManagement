package com.example.connectionsmanagement.ConnectionsMap
//为方便本项目随时可获取上下文context而设
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class ConnectionsManagementApplication:Application() {
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context:Context
        var NowUserId:Int=-1//现在登录的用户ID
    }

    override fun onCreate() {
        super.onCreate()
        context =applicationContext
    }

    fun setUserId(id:Int){
        NowUserId=id
    }

    fun getUserId():Int{
        return NowUserId
    }
}