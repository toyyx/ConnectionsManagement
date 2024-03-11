package com.example.connectionsmanagement.ConnectionsMap
//人物信息数据库
import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import androidx.core.content.contentValuesOf
import com.example.connectionsmanagement.RegisterAndLogin.User

class ConnectionsDatabaseHelper(private val context:Context, version:Int):SQLiteOpenHelper(context,"HumanConnectionDatabase",null,version) {
    //创建表
    //①用户账号User：用户编号userId（唯一）、用户名userName、密码password、姓名name、性别gender、头像image_data、电话phone_number、邮箱email
    //②人脉关系Connections：用户编号userId、人物编号personId、关系类型（朋友、亲人、同学、其他）relationship、姓名name、性别gender、头像image_data、电话phone_number、邮箱email、备注notes

    //①用户账号User：用户编号userId（唯一）、用户名userName、密码password
    //②用户个人信息UserInformation：用户编号userId、姓名name、性别gender、头像image_data、电话phone_number、邮箱email
    //③人脉关系Connections：用户编号userId、关系类型（朋友、亲人、同学、其他）relationship、人物编号personId
    //④人物数据Person：人物编号personId（唯一）、姓名name、性别gender、头像image_data、电话phone_number、邮箱email、备注notes

    private val createUser="create table User(userId integer primary key autoincrement,userName text,password text)"
    private val createUserInformation="create table UserInformation(userId integer primary key,name text,gender text,image_data blob,phone_number text,email text)"
    private val createConnections="create table Connections(userId integer ,relationship text,personId integer ,primary key(userId,personId))"
    private val createPerson="create table Person(personId integer primary key autoincrement,name text,gender text,image_data blob,phone_number text,email text,notes text)"
    private val db: SQLiteDatabase = readableDatabase

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(createUser)
        db?.execSQL(createUserInformation)
        db?.execSQL(createConnections)
        db?.execSQL(createPerson)
        Toast.makeText(context,"数据库创建成功",Toast.LENGTH_SHORT).show()
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    //添加用户（注册时使用）
    fun addUser(userName: String, password: String):Int {
        return db.insert("User",null, contentValuesOf(
            "userName" to userName,
            "password" to password)).toInt()
    }

    //添加用户信息（注册时使用）
    fun addUserInformation(userId:Int,name:String,image_data:ByteArray){
        db.insert("UserInformation",null, contentValuesOf(
            "userId" to userId,
            "name" to name,
            "image_data" to image_data))
    }

    fun delete(name: String, password: String) {
        db.execSQL("DELETE FROM user WHERE name = AND password =$name$password")
    }

    fun updata(password: String) {
        db.execSQL("UPDATE user SET password = ?", arrayOf<Any>(password))
    }

    //登录时获取所有用户时使用
    val allData: ArrayList<User>
        @SuppressLint("Range")
        get() {
            val list = ArrayList<User>()
            val cursor = db.query("User", null, null, null, null, null, null)
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndex("userId"))
                val name = cursor.getString(cursor.getColumnIndex("userName"))
                val password = cursor.getString(cursor.getColumnIndex("password"))
                //list.add(User(id,name, password))
            }
            cursor.close()
            return list
        }

}