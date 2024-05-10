package com.example.connectionsmanagement.Face.FaceSearch_M_N

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
import com.example.connectionsmanagement.Tools.Camera
import com.example.connectionsmanagement.Tools.ConnectionsManagementApplication
import com.example.connectionsmanagement.Tools.Tools
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

//人脸搜索M:N
class FaceSearch_M_N_Fragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val thisView = inflater.inflate(R.layout.fragment_face_search_m_n, container, false)
        // 获取关联的 Activity
        val activity = requireActivity()
        //设置图片获取
        val camera=Camera(activity,thisView.findViewById<ImageView>(R.id.faceSearch_M_N_Image_ImageView))

        //获取RecyclerView
        val showResult_RV = thisView.findViewById<RecyclerView>(R.id.faceSearch_M_N_showResult_RecyclerView)
        showResult_RV.layoutManager = LinearLayoutManager(ConnectionsManagementApplication.context)
        val adapter = FaceSearch_M_N_result_Adapter(JSONArray(),JSONArray())
        showResult_RV.adapter = adapter

        val resultTips_TV=thisView.findViewById<TextView>(R.id.faceSearch_M_N_resultTips_TextView)

        //开始搜索按钮
        thisView.findViewById<Button>(R.id.faceSearch_M_N_startSearch_Button).setOnClickListener {
            if(camera.imageUri!=null) {
                activity.findViewById<ConstraintLayout>(R.id.loadingImage_ConstraintLayout).visibility=View.VISIBLE
                //获取用户选择的图片文件
                val selectedImageFile = Tools.getFileFromUri(camera.imageUri!!)
                // 创建OkHttpClient实例
                val client = OkHttpClient()
                // 构建MultipartBody，用于上传图片
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "userId",
                        ConnectionsManagementApplication.NowUser.userId.toString()
                    )
                    .addFormDataPart(
                        "image", "avatar.jpg",
                        selectedImageFile!!.asRequestBody("image/*".toMediaTypeOrNull())
                    )
                    .build()

                // 创建POST请求
                val request = Request.Builder()
                    .url("http://121.199.71.143:8080/connection_server-1.0-SNAPSHOT/FaceSearch_M_N_Servlet")
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
                                        ConnectionsManagementApplication.context, "人群搜索成功",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Tools.GetFaceDetectBase64(selectedImageFile!!) { responseData ->
                                        // 在这里处理返回的数据
                                        // responseData 就是 onResponse 方法中的返回值
                                        println("GetFaceDetectBase64: " + responseData.toString())
                                        if(responseData!=null){
                                            activity.runOnUiThread {
                                                resultTips_TV.visibility = View.GONE
                                                showResult_RV.visibility = View.VISIBLE
                                                adapter.updateData(jsonObject.getJSONObject("search_result").getJSONArray("face_list"), responseData)
                                                activity.findViewById<ConstraintLayout>(R.id.loadingImage_ConstraintLayout).visibility = View.GONE
                                            }
                                        }else{//防止人脸检测成功，但响应结果为空
                                            activity.runOnUiThread {
                                                resultTips_TV.visibility=View.VISIBLE
                                                showResult_RV.visibility=View.GONE
                                                resultTips_TV.text="接口结果异常，请尝试其它图片"
                                                activity.findViewById<ConstraintLayout>(R.id.loadingImage_ConstraintLayout).visibility = View.GONE
                                            }
                                        }
                                    }
                                } else {
                                    Toast.makeText(
                                        ConnectionsManagementApplication.context, "人群搜索失败\nerror_msg:"+jsonObject.get("error_msg").toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    resultTips_TV.visibility=View.VISIBLE
                                    showResult_RV.visibility=View.GONE
                                    resultTips_TV.text="人群搜索失败，请重新尝试"
                                    activity.findViewById<ConstraintLayout>(R.id.loadingImage_ConstraintLayout).visibility=View.GONE
                                }

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
                            resultTips_TV.visibility=View.VISIBLE
                            showResult_RV.visibility=View.GONE
                            resultTips_TV.text="网络连接失败"
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
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FaceSearch_M_N_Fragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}