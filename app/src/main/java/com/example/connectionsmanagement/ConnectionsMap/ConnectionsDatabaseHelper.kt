package com.example.connectionsmanagement.ConnectionsMap
//人物信息数据库
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

class ConnectionsDatabaseHelper(private val context:Context, name:String, version:Int):SQLiteOpenHelper(context,name,null,version) {
    //目前包含数据：姓名name、备注notes、头像数据image_data
    private val createHuman="create table Human(id integer primary key autoincrement,name text,notes text,image_data blob)"

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(createHuman)
        Toast.makeText(context,"数据库创建成功",Toast.LENGTH_SHORT).show()
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }


}