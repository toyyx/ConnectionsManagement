package com.example.connectionsmanagement.Relations


import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import com.example.connectionsmanagement.Tools.ConnectionsManagementApplication
import com.example.connectionsmanagement.Tools.Tools.getFileFromUri
import com.example.connectionsmanagement.R
import com.example.connectionsmanagement.Tools.Tools
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

//增加人物的弹窗
class PopAddHumanActivity : AppCompatActivity() {
    private var imageUri: Uri?=null  //图片地址
    private lateinit var getPicturesFromCameraActivity: ActivityResultLauncher<Uri>//拍照获取图片-启动器
    private lateinit var getPicturesFromAlbumActivity: ActivityResultLauncher<String> //相册获取图片-启动器
    private var selectedGender:String="男"//选中性别
    private var selectedRelation:String="朋友"//选中关系
    lateinit var loadingLL: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pop_add_human)

        //加载中的动画
        loadingLL=findViewById(R.id.addHuman_loadingImage_LinearLayout)
        loadingLL.setOnClickListener {
            // 执行点击事件的操作
            true // 表示点击事件已被消费
        }

        //关系下拉框配置
        val spinner = findViewById<Spinner>(R.id.addRelationSpinner)
        val items = resources.getStringArray(R.array.relation_items)
        val adapter = CustomArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

        //性别单选处理
        val radioGroup = findViewById<RadioGroup>(R.id.gender_radiogroup)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            // 当选择不同的 RadioButton 时触发此回调
            val radioButton = findViewById<RadioButton>(checkedId)
            selectedGender = radioButton.text.toString()
        }

        //拍照获取图片处理过程
        getPicturesFromCameraActivity =registerForActivityResult(ActivityResultContracts.TakePicture()){
            if(it) {
                val imageView: ImageView? =findViewById<CircleImageView>(R.id.addImage)
                imageView?.setImageURI(imageUri)
            }
        }

        //相册获取图片处理过程
        getPicturesFromAlbumActivity = registerForActivityResult(ActivityResultContracts.GetContent()) {
            val imageView: ImageView? =findViewById<CircleImageView>(R.id.addImage)
            imageView?.setImageURI(it)
            imageUri=it
        }

        //设置取消按钮关闭弹窗
        findViewById<Button>(R.id.popAddCancelButton).setOnClickListener {
            finish()
        }

        //设置人物图像并保存至本地
        findViewById<CircleImageView>(R.id.addImage).setOnClickListener {
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

        //确定按钮的功能：新增人物
        findViewById<Button>(R.id.popAddSureButton).setOnClickListener {
            var name=findViewById<EditText>(R.id.addNameText).text.toString().trim { it <= ' ' }
            var phone_number=findViewById<EditText>(R.id.addPhoneText).text.toString().trim { it <= ' ' }
            var email=findViewById<EditText>(R.id.addEmailText).text.toString().trim { it <= ' ' }
            var notes=findViewById<EditText>(R.id.addNotesText).text.toString().trim { it <= ' ' }
            if(imageUri!=null && !TextUtils.isEmpty(name)&& !TextUtils.isEmpty(phone_number)&& !TextUtils.isEmpty(email)){
                loadingLL.visibility=View.VISIBLE
                //获取用户选择的图片文件
                val selectedImageFile = getFileFromUri(imageUri!!)
                // 创建OkHttpClient实例
                val client = OkHttpClient()
                // 构建MultipartBody，用于上传图片
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("userId", ConnectionsManagementApplication.NowUser.userId.toString())
                    .addFormDataPart("relationship",selectedRelation)
                    .addFormDataPart("name",name)
                    .addFormDataPart("gender",selectedGender)
                    .addFormDataPart("phone_number",phone_number)
                    .addFormDataPart("email",email)
                    .addFormDataPart("notes",notes)
                    .addFormDataPart("image", "avatar.jpg",
                        selectedImageFile!!.asRequestBody("image/*".toMediaTypeOrNull()))
                    .build()

                // 创建POST请求
                val request = Request.Builder()
                    .url("${Tools.baseUrl}/AddRelationServlet")
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
                                val result = jsonObject["result"].asString
                                if(result=="success"){
                                    Toast.makeText(ConnectionsManagementApplication.context,"新增关系成功\n"+jsonObject["error_msg"].asString,Toast.LENGTH_SHORT).show()
                                    ConnectionsManagementApplication.IsRelationsChanged_forList = true
                                    ConnectionsManagementApplication.IsRelationsChanged_forDrawer=true
                                }else{
                                    Toast.makeText(ConnectionsManagementApplication.context,"新增关系失败,请确保照片中包含人脸",Toast.LENGTH_SHORT).show()
                                }
                                finish()
                            }
                        }
                        runOnUiThread { loadingLL.visibility=View.GONE}
                    }
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        runOnUiThread {
                            // 处理请求失败情况，例如网络连接问题
                            Toast.makeText(
                                ConnectionsManagementApplication.context,
                                "网络连接失败",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadingLL.visibility=View.GONE
                        }
                    }
                })
            }else{
                Toast.makeText(
                    ConnectionsManagementApplication.context,
                    "请完善人物信息",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

}