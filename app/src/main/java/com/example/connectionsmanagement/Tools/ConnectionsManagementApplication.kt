package com.example.connectionsmanagement.Tools
//为方便本项目随时可获取上下文context而设
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.example.connectionsmanagement.Communications.Communication
import com.example.connectionsmanagement.Relations.Relation
import com.example.connectionsmanagement.RegisterAndLogin.User

class ConnectionsManagementApplication:Application() {
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context:Context//全局通用上下文
        lateinit var NowUser:User//现在登录的用户ID
        lateinit var NowRelations: ArrayList<Relation>//当前人脉数据
        var IsRelationsChanged_forList:Boolean=false//人脉改变标识-列表
        var IsRelationsChanged_forDrawer:Boolean=false//人脉改变标识-图谱
        var Communications=ArrayList<Communication>()//当前交际数据
        var IsCommunicationsChanged:Boolean=false//交际改变标识
        var IsUserChanged:Boolean=false//用户信息改变标识
    }

    override fun onCreate() {
        super.onCreate()
        context =applicationContext
    }


}

