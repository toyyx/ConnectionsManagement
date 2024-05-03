package com.example.connectionsmanagement.Face.FaceDetect

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.connectionsmanagement.R
import com.example.connectionsmanagement.Relations.Relation
import com.example.connectionsmanagement.Tools.Camera
import com.example.connectionsmanagement.Tools.ConnectionsManagementApplication
import com.example.connectionsmanagement.Tools.ImageDownloader
import com.example.connectionsmanagement.Tools.ImageDownloader.GetFaceDetectBase64
import com.google.gson.Gson
import com.google.gson.JsonObject
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


/**
 * A simple [Fragment] subclass.
 * Use the [FaceDetect_Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FaceDetect_Fragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val thisView = inflater.inflate(R.layout.fragment_face_detect, container, false)
        // 获取关联的 Activity
        var activity = requireActivity()
        var camera=Camera(activity,thisView.findViewById<ImageView>(R.id.faceDetect_Image_ImageView))

        //获取RecyclerView
        val recyclerView = thisView.findViewById<RecyclerView>(R.id.faceDetect_RecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(ConnectionsManagementApplication.context)
        var jsonArrayResults = JSONArray()//搜索结果队列
        val adapter = FaceDetect_result_Adapter(jsonArrayResults)
        recyclerView.adapter = adapter

        val detectResult_TV=thisView.findViewById<TextView>(R.id.faceDetect_result_TextView)

        thisView.findViewById<Button>(R.id.faceDetect_start_Button).setOnClickListener {
            if(camera.imageUri!=null) {
                activity.findViewById<ConstraintLayout>(R.id.loadingImage_ConstraintLayout).visibility=View.VISIBLE
                //获取用户选择的图片文件
                val selectedImageFile = ImageDownloader.getFileFromURI(camera.imageUri!!)
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
                            activity.runOnUiThread {
                                // 将JSON字符串解析为JsonObject
                                val jsonObject = JSONObject(responseBody)
                                // 读取特定键的值
                                val result = jsonObject.get("result").toString()
                                if (result == "success") {
                                    Toast.makeText(
                                        ConnectionsManagementApplication.context, "人脸检测成功",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val detect_result_full_jsonObject=jsonObject.getJSONObject("detect_result")
                                    if(detect_result_full_jsonObject.getInt("face_num")==0){
                                        recyclerView.visibility=View.GONE
                                        detectResult_TV.visibility=View.VISIBLE
                                        detectResult_TV.text="当前图片未检测出人脸"
                                    }else{
                                        recyclerView.visibility=View.VISIBLE
                                        detectResult_TV.visibility=View.GONE
                                        // 更新数据源后通知适配器更新数据
                                        adapter.updateData(detect_result_full_jsonObject.getJSONArray("face_list"))
                                    }
//                                    detectResult_TV.text=jsonObject.get("detect_result").toString()
                                } else {
                                    Toast.makeText(
                                        ConnectionsManagementApplication.context, "人脸检测失败\nerror_msg:"+jsonObject.get("error_msg").toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    recyclerView.visibility=View.GONE
                                    detectResult_TV.visibility=View.VISIBLE
                                    detectResult_TV.text="人脸检测失败，请重新尝试"
                                }
                                activity.findViewById<ConstraintLayout>(R.id.loadingImage_ConstraintLayout).visibility=View.GONE
                            }
                        }
                    }

                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        activity.runOnUiThread {
                            // 处理请求失败情况，例如网络连接问题
                            Toast.makeText(
                                ConnectionsManagementApplication.context,
                                "网络连接失败",
                                Toast.LENGTH_SHORT
                            ).show()
                            recyclerView.visibility=View.GONE
                            detectResult_TV.visibility=View.VISIBLE
                            detectResult_TV.text="网络连接失败，请重新尝试"
                            activity.findViewById<ConstraintLayout>(R.id.loadingImage_ConstraintLayout).visibility=View.GONE
                        }
                    }
                })
            }else{
                Toast.makeText(ConnectionsManagementApplication.context, "图片不可为空", Toast.LENGTH_SHORT).show()
            }
        }


        return thisView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FaceDetect_Fragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FaceDetect_Fragment().apply {

            }
    }
}