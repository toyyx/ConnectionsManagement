package com.example.connectionsmanagement.Tools

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.example.connectionsmanagement.Communications.Communication
import com.example.connectionsmanagement.MysqlServer.MySQLConnection
import com.example.connectionsmanagement.RegisterAndLogin.User
import com.example.connectionsmanagement.Relations.Relation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

//通用工具类
object Tools {

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

    //从本地路径获取Bitmap
    fun getBitmapFromLocalPath(localPath :String): Bitmap {
        println("本地地址："+ ConnectionsManagementApplication.context.externalCacheDir.toString()+ getSpecialFromString(localPath, "data_image/"))
        return BitmapFactory.decodeFile(
            ConnectionsManagementApplication.context.externalCacheDir.toString() + getSpecialFromString(
                localPath,
                "data_image/"
            )
        )
    }

    //从本地路径获取Uri
    fun getUriFromLocalPath(localPath :String): Uri {
        val imagePath = ConnectionsManagementApplication.context.externalCacheDir.toString()+ getSpecialFromString(localPath, "data_image/")
        return Uri.fromFile(File(imagePath))
    }

    //从字符串中获取特定字符串的后面部分
    fun getSpecialFromString(string: String,character: String):String{
        return string.substring(string.indexOf(character) + character.length)
    }

    //从uri中获取File
    fun getFileFromUri(uri: Uri): File? {
//        Toast.makeText(ConnectionsManagementApplication.context, "开始转换uri", Toast.LENGTH_SHORT).show()//调试使用
        val contentResolver: ContentResolver = ConnectionsManagementApplication.context.contentResolver
        var inputStream = contentResolver.openInputStream(uri)//输入流

        var outputFile: File?=null//输出流
        try {
            val cacheDir = ConnectionsManagementApplication.context.cacheDir
            outputFile = File(cacheDir, "temp_file_" + System.currentTimeMillis())
            val outputStream = FileOutputStream(outputFile)
            inputStream.use { input ->
                outputStream.use { output ->
                    val bytesCopied =input?.copyTo(output)//复制数据量
//                    //调试使用
//                    if(bytesCopied==null){
//                        Toast.makeText(ConnectionsManagementApplication.context, "返回的字节数为 null", Toast.LENGTH_SHORT).show()
//                    }else{
//                        Toast.makeText(ConnectionsManagementApplication.context, "返回的字节数为 $bytesCopied", Toast.LENGTH_SHORT).show()
//                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return outputFile
    }

    //更新本地人脉数据
    suspend fun RefreshRelations(){
        //将 JSON 字符串 jsonString 解析为 ArrayList<Relation>
        val jsonString = MySQLConnection.fetchWebpageContent(
            "SearchRelations",
            ConnectionsManagementApplication.NowUser.userId.toString(), ""
        )
        println("Server Response_RefreshRelations: $jsonString")
        val listType = object : TypeToken<ArrayList<Relation>>() {}.type
        ConnectionsManagementApplication.NowRelations = Gson().fromJson(jsonString, listType)
        ConnectionsManagementApplication.NowRelations.forEach{
            downloadImage(ConnectionsManagementApplication.context, it.image_path)
        }
    }

    //更新本地交际数据
    suspend fun RefreshCommunications(){
        //将 JSON 字符串 jsonString 解析为 ArrayList<Relation>
        val jsonString = MySQLConnection.fetchWebpageContent(
            "SearchCommunications",
            ConnectionsManagementApplication.NowUser.userId.toString(), ""
        )
        println("Server Response: $jsonString")
        val listType = object : TypeToken<ArrayList<Communication>>() {}.type
        ConnectionsManagementApplication.Communications = Gson().fromJson(jsonString, listType)
    }

    //更新本地用户数据
    suspend fun RefreshUser(){
        //将 JSON 字符串 jsonString 解析为 User
        val jsonString = MySQLConnection.fetchWebpageContent(
            "Login",
            ConnectionsManagementApplication.NowUser.userName!!,
            ConnectionsManagementApplication.NowUser.password!!
        )
        println("Server Response: $jsonString")
        val jsonObject = JSONObject(jsonString)
        ConnectionsManagementApplication.NowUser = User(
            jsonObject.getString("userId").toInt(),
            jsonObject.getString("userName"),
            jsonObject.getString("password"),
            jsonObject.getString("name"),
            jsonObject.getString("gender"),
            jsonObject.getString("image_path"),
            jsonObject.getString("phone_number"),
            jsonObject.getString("email")
        )
        downloadImage(
            ConnectionsManagementApplication.context,
            ConnectionsManagementApplication.NowUser.image_path)
    }

    //将Base64转化为Bitmap
    fun getBitmapFromBase64(image_base64: String): Bitmap {
        //将 base64 编码的字符串解码为字节数组
        val decodedBytes = android.util.Base64.decode(image_base64, android.util.Base64.DEFAULT)
        //将字节数组转换为 Bitmap 对象
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    //获取人脸检测的Base64结果(主要为了Base64，顺带传递人脸检测的其它数据)
    fun GetFaceDetectBase64(selectedImageFile: File, callback: (responseData: JSONArray?) -> Unit){
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
                        if(!detect_result_full_jsonObject.has("face_num")){
                            callback(null)
                        }
                        else if(detect_result_full_jsonObject.getInt("face_num")!=0){
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

    fun dpToPx(dp: Int): Float {
        return dp * ConnectionsManagementApplication.context.resources.displayMetrics.density
    }

    fun showUserAgreement(context:Context){
        val instructions = """
                            欢迎使用《人脉管理小助手》！在使用之前，请仔细阅读并同意本用户协议及隐私政策。
                    
                    1.使用规则
                            用户应遵守国家相关法律法规，不得利用本应用程序进行违法活动。
                            用户不得利用本应用程序从事侵犯他人权益的行为，包括但不限于侵犯知识产权、侵犯个人隐私等。
                   
                    2.隐私政策
                            我们尊重并保护用户的个人隐私。在用户使用《人脉管理小助手》时，我们可能会收集、存储和使用用户提供的个人信息，包括但不限于姓名、电话号码、电子邮件地址等。
                            我们收集用户个人信息的目的是为了向用户提供更好的服务体验，如记录人脉信息、进行人脸识别等。
                            我们承诺对用户的个人信息进行严格保密，并采取合理的安全措施保护用户的个人信息安全。
                            用户可以随时查看、修改、删除自己的个人信息，并有权选择是否同意提供个人信息。但是，拒绝提供个人信息可能导致无法使用部分功能或服务。
                            
                    3.知识产权保护
                            应用程序中的所有内容（包括但不限于文字、图片、音频、视频等）的知识产权归本应用程序所有。
                            用户不得未经授权复制、传播、修改或者利用应用程序中的任何内容。
                            
                    4.免责声明
                            用户在使用本应用程序时应自行承担风险，我们不对因使用本应用程序而导致的任何损失负责。
                            本应用程序可能因系统维护、升级等原因而中断服务，对此我们不承担任何责任。
                            
                    5.服务终止
                            用户违反本用户协议的规定，我们有权立即终止对用户的服务。
                            
                    6.争议解决
                            本用户协议及隐私政策适用于中华人民共和国的法律。因本用户协议及隐私政策引起的任何争议，双方应协商解决。协商不成的，应提交至有管辖权的法院解决。
                            
                            用户在使用本应用程序时即视为已阅读、理解并同意本用户协议及隐私政策的所有内容。如果用户不同意本用户协议及隐私政策的任何内容，应立即停止使用本应用程序。


        """.trimIndent()

        // 创建一个对话框，并将滚动视图设置为其内容
        val builder = AlertDialog.Builder(context)
        builder.setTitle("用户协议及隐私政策")
        builder.setMessage(instructions)
        builder.setPositiveButton("已阅读") { dialog, which ->
            dialog.dismiss()
        }
        // 显示对话框
        val dialog = builder.create()
        dialog.show()
    }

}