package com.example.connectionsmanagement.Communications

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.connectionsmanagement.Communications.Show.ShowCommunication
import com.example.connectionsmanagement.Tools.ConnectionsManagementApplication
import com.example.connectionsmanagement.Relations.SearchParticipants.SearchParticipantsActivity
import com.example.connectionsmanagement.Relations.SearchParticipants.SelectedParticipant
import com.example.connectionsmanagement.R
import com.example.connectionsmanagement.Tools.Tools
import com.google.gson.Gson
import okhttp3.Callback
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//展示交际详情activity
class ShowCommunicationDetailActivity : AppCompatActivity() {
    lateinit var Title_EditText:EditText
    lateinit var Address_EditText:EditText
    lateinit var StartTime_TextView: TextView
    lateinit var StartTime_TimePicker: TimePicker
    lateinit var FinishTime_TextView:TextView
    lateinit var FinishTime_TimePicker:TimePicker
    lateinit var Participants_EditText:EditText
    lateinit var Detail_EditText:EditText
    lateinit var back_Button: ImageButton
    lateinit var edit_Button: ImageButton
    lateinit var sureEdit_Button: Button
    lateinit var SelectParticipants_Button: Button
    lateinit var thisCommunication: ShowCommunication//当前展示的交际

    lateinit var startTimeString:String
    lateinit var finishTimeString:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_communication_detail)
        //初始化
        initView()
    }

    //初始化控件
    fun initView(){
        Title_EditText=findViewById(R.id.DetailCommunicationTitle_EditText)
        Address_EditText=findViewById(R.id.DetailCommunicationAddress_EditText)
        StartTime_TextView=findViewById(R.id.DetailCommunicationStartTime_TextView)
        StartTime_TimePicker=findViewById(R.id.DetailCommunicationStartTime_TimePicker)
        FinishTime_TextView=findViewById(R.id.DetailCommunicationFinishTime_TextView)
        FinishTime_TimePicker=findViewById(R.id.DetailCommunicationFinishTime_TimePicker)
        Participants_EditText=findViewById(R.id.DetailCommunicationParticipants_EditText)
        Detail_EditText=findViewById(R.id.DetailCommunicationDetail_EditText)
        back_Button=findViewById(R.id.backCommunicationDetail_Button)
        edit_Button=findViewById(R.id.editCommunication_ImageButton)
        sureEdit_Button=findViewById(R.id.sureEditCommunication_Button)
        SelectParticipants_Button=findViewById(R.id.DetailSelectParticipants_Button)

        //获取intent传递的交际数据
        thisCommunication=Gson().fromJson(intent.getStringExtra("thisCommunication"), ShowCommunication::class.java)

        Title_EditText.setText(thisCommunication.title)
        Address_EditText.setText(thisCommunication.address)
        Detail_EditText.setText(thisCommunication.detail)

        val thisStartLocalDateTime=stringToLocalDateTime(thisCommunication.startTime)
        startTimeString=thisCommunication.startTime
        StartTime_TimePicker.setIs24HourView(true)
        StartTime_TextView.text=thisCommunication.startTime
        StartTime_TimePicker.hour=thisStartLocalDateTime.hour
        StartTime_TimePicker.minute=thisStartLocalDateTime.minute

        val thisFinishLocalDateTime=stringToLocalDateTime(thisCommunication.finishTime)
        finishTimeString=thisCommunication.finishTime
        FinishTime_TimePicker.setIs24HourView(true)
        FinishTime_TextView.text=thisCommunication.finishTime
        FinishTime_TimePicker.hour=thisFinishLocalDateTime.hour
        FinishTime_TimePicker.minute=thisFinishLocalDateTime.minute

        //参与者的界面显示
        var first=true
        var stringBuilder=StringBuilder()
        thisCommunication.participants.forEach {
            if(first) {
                stringBuilder.append(it.name)
                first = false
            } else
                stringBuilder.append("、${it.name}")
        }
        Participants_EditText.setText(stringBuilder.toString())

        //返回按钮
        back_Button.setOnClickListener {
            finish()
        }

        //开始编辑按钮
        edit_Button.setOnClickListener {
            startEdit()//开始编辑模式
        }

        //完成编辑按钮
        sureEdit_Button.setOnClickListener {
            if(Title_EditText.text.trim().isNotEmpty()){//检查主题非空
                if(StartTime_TimePicker.hour<FinishTime_TimePicker.hour||
                    (StartTime_TimePicker.hour==FinishTime_TimePicker.hour&&StartTime_TimePicker.minute<FinishTime_TimePicker.minute)){//检查时间逻辑正确
                    endEdit()//结束编辑模式
                    //获取参与者数据
                    var  participantsIdArray=ArrayList<Int>()
                    thisCommunication.participants.forEach {
                        participantsIdArray.add(it.personId)
                    }

                    // 创建OkHttpClient实例
                    val client = OkHttpClient()

                    // 构建MultipartBody，用于上传图片
                    val requestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("eventId",thisCommunication.eventId)
                        .addFormDataPart("stratTime",StartTime_TextView.text.toString())
                        .addFormDataPart("finishTime",FinishTime_TextView.text.toString())
                        .addFormDataPart("title",Title_EditText.text.toString())
                        .addFormDataPart("address",Address_EditText.text.toString())
                        .addFormDataPart("detail",Detail_EditText.text.toString())
                        .addFormDataPart("participants",participantsIdArray.toString())
                        .build()

                    // 创建POST请求
                    val request = Request.Builder()
                        .url("${Tools.baseUrl}/UpdateCommunicationServlet")
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
                                    Toast.makeText(ConnectionsManagementApplication.context, "修改交际成功", Toast.LENGTH_SHORT).show()
                                    ConnectionsManagementApplication.IsCommunicationsChanged =true
                                }else{
                                    Toast.makeText(ConnectionsManagementApplication.context, "修改交际失败", Toast.LENGTH_SHORT).show()
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

        //选择参与者按钮
        SelectParticipants_Button.setOnClickListener {
            //进入查找并选择参与者界面
            val intent = Intent(ConnectionsManagementApplication.context, SearchParticipantsActivity::class.java)
            intent.putExtra("nowParticipantsJson", Gson().toJson(thisCommunication.participants))
            intent.putExtra("sender","ShowCommunicationDetailActivity")
            startActivity(intent)
        }
    }

    //处理从选择参与者界面传递回的数据
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasExtra("selectedParticipants")) {
            // 处理接收到的数据
            thisCommunication.participants= ArrayList(Gson().fromJson(intent.getStringExtra("selectedParticipants"),Array<SelectedParticipant>::class.java).toList().toMutableList())
            var first=true
            var stringBuilder=StringBuilder()
            thisCommunication.participants.forEach {
                if(first) {
                    stringBuilder.append(it.name)
                    first = false
                } else
                    stringBuilder.append("、${it.name}")
            }
            Participants_EditText.setText(stringBuilder.toString())
        }
    }

    //开始编辑模式
    fun startEdit(){
        Title_EditText.isEnabled=true
        Address_EditText.isEnabled=true
        Detail_EditText.isEnabled=true
        StartTime_TextView.visibility= View.GONE
        StartTime_TimePicker.visibility=View.VISIBLE
        FinishTime_TextView.visibility= View.GONE
        FinishTime_TimePicker.visibility=View.VISIBLE
        edit_Button.visibility= View.GONE
        sureEdit_Button.visibility=View.VISIBLE
        SelectParticipants_Button.visibility=View.VISIBLE
    }

    //结束编辑模式
    fun endEdit(){
        Title_EditText.isEnabled=false
        Address_EditText.isEnabled=false
        Detail_EditText.isEnabled=false
        StartTime_TextView.visibility= View.VISIBLE
        StartTime_TimePicker.visibility=View.GONE
        FinishTime_TextView.visibility= View.VISIBLE
        FinishTime_TimePicker.visibility=View.GONE
        edit_Button.visibility= View.VISIBLE
        sureEdit_Button.visibility=View.GONE
        SelectParticipants_Button.visibility=View.GONE

        startTimeString=stringUpdateHour_Minute(startTimeString,StartTime_TimePicker.hour,StartTime_TimePicker.minute)
        finishTimeString=stringUpdateHour_Minute(finishTimeString,FinishTime_TimePicker.hour,FinishTime_TimePicker.minute)
        StartTime_TextView.text=startTimeString
        FinishTime_TextView.text=finishTimeString
    }

    //字符串转化为LocalDateTime类型
    fun stringToLocalDateTime(timeString: String): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return LocalDateTime.parse(timeString, formatter)
    }

    //LocalDateTime类型转化为字符串
    fun LocalDateTimeToString(datetime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return datetime.format(formatter)
    }

    //更新时间字符串中的小时、分钟
    fun stringUpdateHour_Minute(timeString: String,hour:Int,minute:Int):String{
        val thisLocalDateTime=stringToLocalDateTime(timeString)
        return LocalDateTimeToString(thisLocalDateTime.withHour(hour).withMinute(minute))
    }

}