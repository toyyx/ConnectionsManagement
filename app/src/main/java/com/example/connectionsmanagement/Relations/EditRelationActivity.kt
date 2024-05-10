package com.example.connectionsmanagement.Relations

import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import com.example.connectionsmanagement.Tools.ConnectionsManagementApplication
import com.example.connectionsmanagement.Tools.Tools
import com.example.connectionsmanagement.R
import com.google.gson.Gson
import com.google.gson.JsonObject
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.time.LocalDateTime

//编辑人物信息
class EditRelationActivity : AppCompatActivity() {
    lateinit var imageUri: Uri  //图片地址
    private lateinit var getPicturesFromCameraActivity: ActivityResultLauncher<Uri>//拍照获取图片-启动器
    private lateinit var getPicturesFromAlbumActivity: ActivityResultLauncher<String> //相册获取图片-启动器
    lateinit var selectedRelation:String//选中关系
    lateinit var selectedGender:String//选中性别

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_relation)

        //获取控件
        var thisRelation= Gson().fromJson(intent.getStringExtra("thisRelation"), Relation::class.java)
        val image_CIV=findViewById<CircleImageView>(R.id.editRelationImage_CircleImageView)
        val name_ET=findViewById<EditText>(R.id.editRelationName_EditText)
        val relation_Spinner = findViewById<Spinner>(R.id.editRelation_Spinner)
        val phone_ET=findViewById<EditText>(R.id.editRelationPhone_EditText)
        val email_ET=findViewById<EditText>(R.id.editRelationEmail_EditText)
        val notes_ET=findViewById<EditText>(R.id.editRelationNotes_EditText)
        val back_IV=findViewById<ImageView>(R.id.back_editRelation_ImageView)
        val sure_IV=findViewById<ImageView>(R.id.sureEdit_editRelation_ImageView)


        //设置初始值
        imageUri= Tools.getUriFromLocalPath(thisRelation.image_path)
        image_CIV.setImageBitmap(Tools.getBitmapFromLocalPath(thisRelation.image_path))
        name_ET.setText(thisRelation.name)
        phone_ET.setText(thisRelation.phone_number)
        email_ET.setText(thisRelation.email)
        notes_ET.setText(thisRelation.notes)
        selectedRelation=thisRelation.relationship
        selectedGender=thisRelation.gender

        //关系下拉框配置
        val items = resources.getStringArray(R.array.relation_items)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        relation_Spinner.adapter = adapter
        relation_Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // 在选择不同选项时触发的操作
                val selectedItem = parent?.getItemAtPosition(position).toString()
                // 在这里处理选中的值（selectedItem）
                selectedRelation=selectedItem
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 当没有选项被选择时触发的操作
            }
        }
        // 遍历数据源，找到匹配的文本，并设置为选中项
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i) == selectedRelation) {
                relation_Spinner.setSelection(i)
                break
            }
        }

        //性别单选处理
        val radioGroup = findViewById<RadioGroup>(R.id.editRelation_gender_radiogroup)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            // 当选择不同的 RadioButton 时触发此回调
            val radioButton = findViewById<RadioButton>(checkedId)
            selectedGender = radioButton.text.toString()
        }
        if(selectedGender=="男"){
            findViewById<RadioButton>(R.id.editRelation_gender_man).isChecked=true
        }else{
            findViewById<RadioButton>(R.id.editRelation_gender_woman).isChecked=true
        }


        //拍照获取图片处理过程
        getPicturesFromCameraActivity =registerForActivityResult(ActivityResultContracts.TakePicture()){
            if(it) {
                image_CIV?.setImageURI(imageUri)
            }
        }

        //相册获取图片处理过程
        getPicturesFromAlbumActivity = registerForActivityResult(ActivityResultContracts.GetContent()) {
            image_CIV?.setImageURI(it)
            imageUri=it
        }

        //设置人物图像并保存至本地
        image_CIV.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.menuInflater.inflate(R.menu.camera_or_album,popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    //拍照获取图片
                    R.id.menu_camera -> {
                        //设置图片存储位置
                        val humanImage= File(externalCacheDir,"human_image${LocalDateTime.now()}.jpg")
                        if(!humanImage.exists()){
                            humanImage.createNewFile()
                        }
                        //获取uri
                        imageUri=if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.N){
                            FileProvider.getUriForFile(this,"com.example.connectionsmanegement.fileprovider",humanImage)
                        }else{
                            Uri.fromFile(humanImage)
                        }
                        getPicturesFromCameraActivity.launch(imageUri)
                        true
                    }
                    //从相册获取图片
                    R.id.menu_album -> {
                        getPicturesFromAlbumActivity.launch("image/*")
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        //返回按钮
        back_IV.setOnClickListener {
            finish()
        }

        //确定修改完成按钮
        sure_IV.setOnClickListener {
            //获取用户选择的图片文件
            val selectedImageFile = Tools.getFileFromUri(imageUri)
            // 创建OkHttpClient实例
            val client = OkHttpClient()
            // 构建MultipartBody，用于上传图片
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("personId",thisRelation.personId.toString())
                .addFormDataPart("relationship",selectedRelation)
                .addFormDataPart("name",name_ET.text.toString())
                .addFormDataPart("gender",selectedGender)
                .addFormDataPart("phone_number",phone_ET.text.toString())
                .addFormDataPart("email",email_ET.text.toString())
                .addFormDataPart("notes",notes_ET.text.toString())
                .addFormDataPart("image_path",thisRelation.image_path)
                .addFormDataPart("image", "avatar.jpg",
                    selectedImageFile!!.asRequestBody("image/*".toMediaTypeOrNull()))
                .build()

            // 创建POST请求
            val request = Request.Builder()
                .url("http://121.199.71.143:8080/connection_server-1.0-SNAPSHOT/UpdateRelationServlet")
                .post(requestBody)
                .build()

            // 发送请求并处理响应
            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    // 处理服务器响应，根据需要更新UI或执行其他操作
                    val responseBody =  response.body?.string()//JsonString
                    if (responseBody != null) {
                        // 处理服务器响应内容，这里的 responseBody 就是网页内容
                        // 可以在这里对网页内容进行解析、处理等操作
                        println("Server Response: $responseBody")
                        runOnUiThread {
                            // 将JSON字符串解析为JsonObject
                            val jsonObject = Gson().fromJson(responseBody, JsonObject::class.java)
                            // 读取特定键的值
                            if(jsonObject["result"].asString=="success"){
                                Toast.makeText(
                                    ConnectionsManagementApplication.context,"修改关系成功\n"+jsonObject["error_msg"].asString,
                                    Toast.LENGTH_SHORT).show()
                                ConnectionsManagementApplication.IsRelationsChanged_forList = true
                                ConnectionsManagementApplication.IsRelationsChanged_forDrawer = true
                            }else{
                                Toast.makeText(
                                    ConnectionsManagementApplication.context,"修改关系失败",
                                    Toast.LENGTH_SHORT).show()
                            }
                            finish()
                        }
                    }
                }
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    runOnUiThread {
                        // 处理请求失败情况，例如网络连接问题
                        Toast.makeText(ConnectionsManagementApplication.context, "网络连接失败", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            })
        }

    }


}