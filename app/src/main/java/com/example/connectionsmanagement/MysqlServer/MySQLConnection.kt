package com.example.connectionsmanagement.MysqlServer

import android.widget.Switch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL
import java.net.UnknownHostException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

//①用户账号User：用户编号userId（唯一）、用户名userName、密码password、姓名name、性别gender、头像image_path、电话phone_number、邮箱email
//②人脉关系PersonalRelations：用户编号userId、人物编号personId、关系类型（朋友、亲人、同学、其他）relationship、姓名name、性别gender、头像image_path、电话phone_number、邮箱email、备注notes

 class MySQLConnection {

     companion object {
         val urlBasic="http://121.199.71.143:8080/connection_server-1.0-SNAPSHOT/"

         // 定义挂起函数来获取网页内容
         suspend fun fetchWebpageContent(mode:String,param1:String,param2:String): String {
             return try {
                 // 尝试执行可能会抛出异常的代码
                 withContext(Dispatchers.IO) {
                     when(mode){
                         "Login"->{URL(urlBasic+"LoginServlet?userName=${param1}&password=${param2}").readText()}
                         "SearchRelations"->{URL(urlBasic+"SearchRelationsServlet?userId=${param1}").readText()}
                         "DeleteRelation"->{URL(urlBasic+"DeleteRelationServlet?personId=${param1}").readText()}
                         "SearchCommunications"->{URL(urlBasic+"SearchCommunicationsServlet?userId=${param1}").readText()}
                         "DeleteCommunication"->{URL(urlBasic+"DeleteCommunicationServlet?eventId=${param1}").readText()}
                         else -> {""}
                     }
                 }
             } catch (e: UnknownHostException) {
                 // 捕获 UnknownHostException 异常，处理无网络连接的情况
                 "无网络连接，请检查网络设置"
             } catch (e: IOException) {
                 // 捕获 IOException 异常，处理其他网络错误的情况
                 "网络错误：${e.message}"
             }
         }
     }
}