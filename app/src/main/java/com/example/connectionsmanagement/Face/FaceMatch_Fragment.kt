package com.example.connectionsmanagement.Face

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.connectionsmanagement.R
import com.example.connectionsmanagement.Tools.Camera
import com.example.connectionsmanagement.Tools.ConnectionsManagementApplication
import com.example.connectionsmanagement.Tools.ImageDownloader
import com.google.gson.Gson
import com.google.gson.JsonObject
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FaceMatch_Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FaceMatch_Fragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var first_create=true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(
            ConnectionsManagementApplication.context, "FaceMatch_Fragment_onCreate",
            Toast.LENGTH_SHORT
        ).show()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Toast.makeText(
            ConnectionsManagementApplication.context, "FaceMatch_Fragment_onCreateView",
            Toast.LENGTH_SHORT
        ).show()
        val thisView = inflater.inflate(R.layout.fragment_face_match, container, false)
        if(first_create) {
            first_create=false

            // 获取关联的 Activity
            val activity = requireActivity()
            val tipsResult_TV = thisView.findViewById<TextView>(R.id.faceMatch_result_TextView)
            val showResult_LL = thisView.findViewById<LinearLayout>(R.id.faceMatch_resultShow_LinearLayout)
            val face_taken_1_TV = thisView.findViewById<TextView>(R.id.faceMatch_face_token1_TextView)
            val face_taken_2_TV = thisView.findViewById<TextView>(R.id.faceMatch_face_token2_TextView)
            val score_TV = thisView.findViewById<TextView>(R.id.faceMatch_score_TextView)

            var camera = Camera(
                activity,
                thisView.findViewById<ImageView>(R.id.faceMatch_Image_1_ImageView),
                thisView.findViewById<ImageView>(R.id.faceMatch_Image_2_ImageView)
            )

            thisView.findViewById<Button>(R.id.faceMatch_start_Button).setOnClickListener {
                if (camera.imageUri != null && camera.imageUri_standby != null) {
                    activity.findViewById<ConstraintLayout>(R.id.loadingImage_ConstraintLayout).visibility=View.VISIBLE
                    //获取用户选择的图片文件
                    val selectedImageFile_1 = ImageDownloader.getFileFromURI(camera.imageUri!!)
                    val selectedImageFile_2 = ImageDownloader.getFileFromURI(camera.imageUri_standby!!)
                    // 创建OkHttpClient实例
                    val client = OkHttpClient()
                    // 构建MultipartBody，用于上传图片
                    val requestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(
                            "image_1", "avatar.jpg",
                            selectedImageFile_1!!.asRequestBody("image/*".toMediaTypeOrNull())
                        )
                        .addFormDataPart(
                            "image_2", "avatar.jpg",
                            selectedImageFile_2!!.asRequestBody("image/*".toMediaTypeOrNull())
                        )
                        .build()

                    // 创建POST请求
                    val request = Request.Builder()
                        .url("http://121.199.71.143:8080/connection_server-1.0-SNAPSHOT/FaceMatchServlet")
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
                                            ConnectionsManagementApplication.context,
                                            "人脸对比成功",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        tipsResult_TV.visibility=View.GONE
                                        showResult_LL.visibility=View.VISIBLE
                                        val match_result_full_jsonObject=jsonObject.getJSONObject("match_result")
                                        score_TV.text=match_result_full_jsonObject.get("score").toString()
                                        val face_list=match_result_full_jsonObject.getJSONArray("face_list")
                                        face_taken_1_TV.text=face_list.getJSONObject(0).get("face_token").toString()
                                        face_taken_2_TV.text=face_list.getJSONObject(1).get("face_token").toString()
                                    } else {
                                        Toast.makeText(
                                            ConnectionsManagementApplication.context,
                                            "人脸对比失败\nerror_msg:" + jsonObject.get("error_msg").toString(),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        tipsResult_TV.visibility=View.VISIBLE
                                        showResult_LL.visibility=View.GONE
                                        tipsResult_TV.text = "人脸对比失败，请重新尝试"
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
                                tipsResult_TV.visibility=View.VISIBLE
                                showResult_LL.visibility=View.GONE
                                tipsResult_TV.text = "网络连接失败，请重新尝试"
                                activity.findViewById<ConstraintLayout>(R.id.loadingImage_ConstraintLayout).visibility=View.GONE
                            }
                        }
                    })
                } else {
                    Toast.makeText(
                        ConnectionsManagementApplication.context,
                        "图片不可为空",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
         * @return A new instance of fragment FaceMatch_Fragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FaceMatch_Fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}