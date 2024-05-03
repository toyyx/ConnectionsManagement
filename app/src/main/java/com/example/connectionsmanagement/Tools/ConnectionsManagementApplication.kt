package com.example.connectionsmanagement.Tools
//为方便本项目随时可获取上下文context而设
import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.View
import android.widget.Toast
import com.example.connectionsmanagement.Calendar.CalendarFragment
import com.example.connectionsmanagement.Communications.Communication
import com.example.connectionsmanagement.ConnectionsMap.DrawerFragment
import com.example.connectionsmanagement.ConnectionsMap.List.ListFragment
import com.example.connectionsmanagement.MysqlServer.MySQLConnection
import com.example.connectionsmanagement.Relations.Relation
import com.example.connectionsmanagement.RegisterAndLogin.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.async
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL

class ConnectionsManagementApplication:Application() {
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context:Context
        lateinit var NowUser:User//现在登录的用户ID
        lateinit var NowRelations: ArrayList<Relation>
        var IsRelationsChanged_forList:Boolean=false
        var IsRelationsChanged_forDrawer:Boolean=false
        var Communications=ArrayList<Communication>()
        var IsCommunicationsChanged:Boolean=false
        var IsUserChanged:Boolean=false

//        lateinit var List_fragment: ListFragment
//        lateinit var Drawer_fragment: DrawerFragment
//        lateinit var Calendar_fragment: CalendarFragment

        var faceDetectResult_JSONArray:JSONArray?=null
    }

    override fun onCreate() {
        super.onCreate()
        context =applicationContext
    }


}

object ImageDownloader {
    // 下载图片（根据图片在服务器的存储路径，并保存至本地缓存）
    fun downloadImage(context: Context, serverImagePath: String?) {
        try {
            //获取图片名
            val imageFileName= serverImagePath?.let { getSpecialFromString(it,"data_image/") }
            val downloadUrl= "http://121.199.71.143:8080/connection_server-1.0-SNAPSHOT/data_image/$imageFileName"
            val localImagePath:String = context.externalCacheDir.toString()+imageFileName

            // 创建URL对象
            val imageUrl = URL(downloadUrl)

            // 打开连接
            val inputStream: InputStream = imageUrl.openStream()
            val outputStream = FileOutputStream(localImagePath)

            // 从输入流读取数据并写入输出流
            val buffer = ByteArray(2048)
            var length: Int
            while (inputStream.read(buffer).also { length = it } != -1) {
                outputStream.write(buffer, 0, length)
            }
            // 关闭流
            inputStream.close()
            outputStream.close()
            println("图片下载成功！")
            println(context.externalCacheDir.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            println("图片下载失败！")
        }
    }

    fun getBitmapFromLocalPath(localPath :String):Bitmap{
        println("本地地址："+ ConnectionsManagementApplication.context.externalCacheDir.toString()+ getSpecialFromString(localPath, "data_image/"))
        return BitmapFactory.decodeFile(ConnectionsManagementApplication.context.externalCacheDir.toString()+ getSpecialFromString(localPath, "data_image/"))
    }

    fun getUriFromLocalPath(localPath :String):Uri{
        val imagePath = ConnectionsManagementApplication.context.externalCacheDir.toString()+ getSpecialFromString(localPath, "data_image/")
        return Uri.fromFile(File(imagePath))
    }

    //从字符串中获取特定字符串的后面部分
    fun getSpecialFromString(string: String,character: String):String{
        return string.substring(string.indexOf(character) + character.length)
    }

    fun getFileFromURI(uri: Uri): File? {
        Toast.makeText(ConnectionsManagementApplication.context, "开始转换uri", Toast.LENGTH_SHORT).show()
        val contentResolver: ContentResolver = ConnectionsManagementApplication.context.contentResolver
        var inputStream = contentResolver.openInputStream(uri)

        var outputFile: File?=null
        try {
            val cacheDir = ConnectionsManagementApplication.context.cacheDir
            outputFile = File(cacheDir, "temp_file_" + System.currentTimeMillis())
            val outputStream = FileOutputStream(outputFile)
            inputStream.use { input ->
                outputStream.use { output ->
                    val bytesCopied =input?.copyTo(output)
                    if(bytesCopied==null){
                        Toast.makeText(ConnectionsManagementApplication.context, "返回的字节数为 null", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(ConnectionsManagementApplication.context, "返回的字节数为 $bytesCopied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return outputFile
    }

    fun releaseBitmap(bitmap: Bitmap?) {
        bitmap?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
        }
    }

    suspend fun RefreshRelations(){
        //将 JSON 字符串 jsonString 解析为 ArrayList<Relation>
        val jsonString = MySQLConnection.fetchWebpageContent("SearchRelations",
            ConnectionsManagementApplication.NowUser.userId.toString(),"")
        println("Server Response_RefreshRelations: $jsonString")
        val listType = object : TypeToken<ArrayList<Relation>>() {}.type
        ConnectionsManagementApplication.NowRelations = Gson().fromJson(jsonString, listType)
        ConnectionsManagementApplication.NowRelations.forEach{
            downloadImage(ConnectionsManagementApplication.context, it.image_path)
        }
    }

    suspend fun RefreshCommunications(){
        //将 JSON 字符串 jsonString 解析为 ArrayList<Relation>
        val jsonString = MySQLConnection.fetchWebpageContent("SearchCommunications",
            ConnectionsManagementApplication.NowUser.userId.toString(),"")
        println("Server Response: $jsonString")
        val listType = object : TypeToken<ArrayList<Communication>>() {}.type
        ConnectionsManagementApplication.Communications = Gson().fromJson(jsonString, listType)
    }

    suspend fun RefreshUser(){
        //将 JSON 字符串 jsonString 解析为 User
        println("开始刷新用户")
        val jsonString = MySQLConnection.fetchWebpageContent("Login",
            ConnectionsManagementApplication.NowUser.userName!!,
            ConnectionsManagementApplication.NowUser.password!!)
        println("Server Response: $jsonString")
        val jsonObject = JSONObject(jsonString)
        ConnectionsManagementApplication.NowUser =User(jsonObject.getString("userId").toInt(),
            jsonObject.getString("userName"),
            jsonObject.getString("password"),
            jsonObject.getString("name"),
            jsonObject.getString("gender"),
            jsonObject.getString("image_path"),
            jsonObject.getString("phone_number"),
            jsonObject.getString("email"))
        downloadImage(
            ConnectionsManagementApplication.context,
            ConnectionsManagementApplication.NowUser.image_path)
    }

    // 字符串转换为整数数组
    fun stringToIntArray(str: String): IntArray {
        val strArray = str.split(", ") // 假设字符串中的整数用空格分隔
        val intArray = IntArray(strArray.size)
        for (i in strArray.indices) {
            intArray[i] = strArray[i].toInt()
        }
        return intArray
    }

    fun getBitmapFromBase64(image_base64: String): Bitmap {
        //将 base64 编码的字符串解码为字节数组
        val decodedBytes = android.util.Base64.decode(image_base64, android.util.Base64.DEFAULT)
        //将字节数组转换为 Bitmap 对象
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    fun GetFaceDetectBase64(selectedImageFile:File,callback: (responseData: JSONArray?) -> Unit){
//        ConnectionsManagementApplication.faceDetectResult_JSONArray =null
        // 创建OkHttpClient实例
        val client = OkHttpClient()
        // 构建MultipartBody，用于上传图片
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image", "avatar.jpg",
                selectedImageFile!!.asRequestBody("image/*".toMediaTypeOrNull())
            )
            .build()

        // 创建POST请求
        val request = Request.Builder()
            .url("http://121.199.71.143:8080/connection_server-1.0-SNAPSHOT/FaceDetectServlet")
            .post(requestBody)
            .build()

        // 发送请求并处理响应
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                // 处理服务器响应，根据需要更新UI或执行其他操作
                val responseBody = response.body?.string()//JsonString
                if (responseBody != null) {
                    // 处理服务器响应内容，这里的 responseBody 就是网页内容
                    // 可以在这里对网页内容进行解析、处理等操作
                    println("Server Response: $responseBody")

                    // 将JSON字符串解析为JsonObject
                    val jsonObject = JSONObject(responseBody)
                    // 读取特定键的值
                    val result = jsonObject.get("result").toString()
                    if (result == "success") {
                        val detect_result_full_jsonObject=jsonObject.getJSONObject("detect_result")
                        if(detect_result_full_jsonObject.getInt("face_num")!=0){
                            callback(detect_result_full_jsonObject.getJSONArray("face_list"))
                        }else{
                            callback(null)
                        }
                    }else{
                        callback(null)
                    }
                }
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                callback(null)
            }
        })
    }

}

