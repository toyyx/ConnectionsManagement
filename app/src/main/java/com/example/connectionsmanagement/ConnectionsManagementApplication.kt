package com.example.connectionsmanagement
//为方便本项目随时可获取上下文context而设
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class ConnectionsManagementApplication:Application() {
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context:Context
    }

    override fun onCreate() {
        super.onCreate()
        context=applicationContext
    }
}