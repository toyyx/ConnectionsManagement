package com.example.connectionsmanagement.Communications

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.connectionsmanagement.Tools.ConnectionsManagementApplication
import com.example.connectionsmanagement.Relations.SearchParticipants.SearchParticipantsActivity
import com.example.connectionsmanagement.Relations.SearchParticipants.SelectedParticipant
import com.example.connectionsmanagement.R
import com.google.gson.Gson
import okhttp3.Callback
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import kotlin.collections.ArrayList

//添加交际anticity
class AddCommunicationActivity : AppCompatActivity() {

    var receivedParticipants=ArrayList<SelectedParticipant>()//选中的参与者
    lateinit var communicationTitle_ET:EditText
    lateinit var communicationAddress_ET:EditText
    lateinit var communicationDetail_ET:EditText
    lateinit var startTimePicker:TimePicker // 假设你的 TimePicker 的 id 是 "timePicker"
    lateinit var finishTimePicker:TimePicker
    lateinit var nowDate:String //当前时间

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_communication)

        communicationTitle_ET=findViewById<EditText>(R.id.AddCommunicationTitle_EditText)
        communicationAddress_ET=findViewById<EditText>(R.id.AddCommunicationAddress_EditText)
        communicationDetail_ET=findViewById<EditText>(R.id.AddCommunicationDetail_EditText)
        startTimePicker= findViewById<TimePicker>(R.id.AddCommunicationStartTime_TimePicker)
        finishTimePicker= findViewById<TimePicker>(R.id.AddCommunicationFinishTime_TimePicker)
        nowDate=intent.getStringExtra("date")!!
        //时间选择器设置为24h制
        startTimePicker.setIs24HourView(true)
        finishTimePicker.setIs24HourView(true)
        findViewById<Button>(R.id.cancelCommunication_Button).setOnClickListener {
            finish()
        }

        //确定添加交际按钮
        findViewById<Button>(R.id.sureAddCommunication_Button).setOnClickListener {
            if(communicationTitle_ET.text.trim().isNotEmpty()){//主题非空
                if(startTimePicker.hour<finishTimePicker.hour||
                    (startTimePicker.hour==finishTimePicker.hour&&startTimePicker.minute<finishTimePicker.minute)){//时间逻辑正确
                        //参与者数据
                        var  participantsIdArray=ArrayList<Int>()
                        receivedParticipants.forEach {
                            participantsIdArray.add(it.personId)
                        }

                        // 创建OkHttpClient实例
                        val client = OkHttpClient()

                        // 构建MultipartBody，用于上传图片
                        val requestBody = MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("userId", ConnectionsManagementApplication.NowUser.userId.toString())
                            .addFormDataPart("stratTime","$nowDate ${startTimePicker.hour}:${startTimePicker.minute}:00")
                            .addFormDataPart("finishTime","$nowDate ${finishTimePicker.hour}:${finishTimePicker.minute}:00")
                            .addFormDataPart("title",communicationTitle_ET.text.toString())
                            .addFormDataPart("address",communicationAddress_ET.text.toString())
                            .addFormDataPart("detail",communicationDetail_ET.text.toString())
                            .addFormDataPart("participants",participantsIdArray.toString())
                            .build()

                        // 创建POST请求
                        val request = Request.Builder()
                            .url("http://121.199.71.143:8080/connection_server-1.0-SNAPSHOT/AddCommunicationServlet")
                            .post(requestBody)
                            .build()

                        // 发送请求并处理响应
                        client.newCall(request).enqueue(object : Callback {
                            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                                // 处理服务器响应，根据需要更新UI或执行其他操作
                                val responseBody =  response.body?.string()
                                // 处理服务器响应内容，这里的 responseBody 就是网页内容
                                // 可以在这里对网页内容进行解析、处理等操作
                                println("Server Response: $responseBody")
                                // 解析 JSON 字符串为 JSON 对象
                                val jsonObject = JSONObject(responseBody)
                                runOnUiThread {
                                    if(jsonObject.getString("result")=="success"){
                                        Toast.makeText(ConnectionsManagementApplication.context, "增加交际成功", Toast.LENGTH_SHORT).show()
                                        ConnectionsManagementApplication.IsCommunicationsChanged =true
                                        finish()
                                    }else{
                                        Toast.makeText(ConnectionsManagementApplication.context, "增加交际失败", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            override fun onFailure(call: okhttp3.Call, e: IOException) {
                                // 处理请求失败情况，例如网络连接问题
                                Toast.makeText(ConnectionsManagementApplication.context, "网络连接失败", Toast.LENGTH_SHORT).show()
                            }
                        })
                }else{
                    Toast.makeText(this, "开始时间不可早于结束时间", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "交际主题不可为空", Toast.LENGTH_SHORT).show()
            }
        }

        //选择参与者
        findViewById<Button>(R.id.selectParticipants_Button).setOnClickListener {
            //进入查询并选择参与者页面
            val intent = Intent(this, SearchParticipantsActivity::class.java)
            intent.putExtra("nowParticipantsJson", Gson().toJson(receivedParticipants))
            intent.putExtra("sender","AddCommunicationActivity")
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        //更新显示选中的参与者
        val stringBuilder = StringBuilder()
        var first=true
        receivedParticipants.forEach {
            if(first){
                stringBuilder.append(it.name)
                first=false
            }
            else
                stringBuilder.append("、${it.name}")
        }
        findViewById<EditText>(R.id.AddCommunicationParticipants_EditText).setText(stringBuilder.toString())
    }

    //从选择参与者界面传回的数据
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasExtra("selectedParticipants")) {
            // 处理接收到的数据
            receivedParticipants= ArrayList(Gson().fromJson(intent.getStringExtra("selectedParticipants"),Array<SelectedParticipant>::class.java).toList().toMutableList())
        }
    }
}