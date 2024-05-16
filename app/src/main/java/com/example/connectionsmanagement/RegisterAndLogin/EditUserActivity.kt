package com.example.connectionsmanagement.RegisterAndLogin

import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import com.example.connectionsmanagement.Tools.ConnectionsManagementApplication
import com.example.connectionsmanagement.Tools.Tools
import com.example.connectionsmanagement.Tools.Tools.getUriFromLocalPath
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

//编辑用户信息activity
class EditUserActivity : AppCompatActivity() {
    lateinit var imageUri: Uri  //图片地址
    private lateinit var getPicturesFromCameraActivity: ActivityResultLauncher<Uri>//拍照获取图片-启动器
    private lateinit var getPicturesFromAlbumActivity: ActivityResultLauncher<String> //相册获取图片-启动器

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user)

        val image_CIV=findViewById<CircleImageView>(R.id.editUserImage_CircleImageView)
        val userName_ET=findViewById<EditText>(R.id.editUserUserName_EditText)
        val name_ET=findViewById<EditText>(R.id.editUserName_EditText)
        val phoneNumber_ET=findViewById<EditText>(R.id.editUserPhone_EditText)
        val email_ET=findViewById<EditText>(R.id.editUserEmail_EditText)
        val back_IV=findViewById<ImageView>(R.id.back_editUser_ImageView)
        val sure_IV=findViewById<ImageView>(R.id.sureEdit_editUser_ImageView)

        val user= ConnectionsManagementApplication.NowUser
        imageUri=getUriFromLocalPath(user.image_path!!)
        image_CIV.setImageBitmap(Tools.getBitmapFromLocalPath(user.image_path!!))
        userName_ET.setText(user.userName)
        name_ET.setText(user.name)
        phoneNumber_ET.setText(user.phone_number)
        email_ET.setText(user.email)
        var selectedGender=user.gender

        //性别单选处理
        val radioGroup = findViewById<RadioGroup>(R.id.editUser_gender_radiogroup)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            // 当选择不同的 RadioButton 时触发此回调
            val radioButton = findViewById<RadioButton>(checkedId)
            selectedGender = radioButton.text.toString()
        }
        if(selectedGender=="男"){
            findViewById<RadioButton>(R.id.editUser_gender_man).isChecked=true
        }else{
            findViewById<RadioButton>(R.id.editUser_gender_woman).isChecked=true
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

        //确定完成修改按钮
        sure_IV.setOnClickListener {
            val userName=userName_ET.text.toString().trim { it <= ' ' }
            val name=name_ET.text.toString().trim { it <= ' ' }
            val phone_number=phoneNumber_ET.text.toString().trim { it <= ' ' }
            val email=email_ET.text.toString().trim { it <= ' ' }
            if(!TextUtils.isEmpty(userName)&&!TextUtils.isEmpty(name)&&!TextUtils.isEmpty(phone_number)&&!TextUtils.isEmpty(email)){
                //获取用户选择的图片文件
                val selectedImageFile = Tools.getFileFromUri(imageUri)
                // 创建OkHttpClient实例
                val client = OkHttpClient()
                // 构建MultipartBody，用于上传图片
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("userId", ConnectionsManagementApplication.NowUser.userId.toString())
                    .addFormDataPart("userName",userName)
                    .addFormDataPart("name",name)
                    .addFormDataPart("gender",selectedGender!!)
                    .addFormDataPart("phone_number",phone_number)
                    .addFormDataPart("email",email)
                    .addFormDataPart("image_path", ConnectionsManagementApplication.NowUser.image_path!!)
                    .addFormDataPart("image", "avatar.jpg",
                        selectedImageFile!!.asRequestBody("image/*".toMediaTypeOrNull()))
                    .build()

                // 创建POST请求
                val request = Request.Builder()
                    .url("${Tools.baseUrl}/UpdateUserServlet")
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
                                    Toast.makeText(this@EditUserActivity,"更新用户信息成功", Toast.LENGTH_SHORT).show()
                                    ConnectionsManagementApplication.NowUser.userName=userName_ET.text.toString()
                                    ConnectionsManagementApplication.IsUserChanged =true
                                }else{
                                    Toast.makeText(this@EditUserActivity,"更新用户信息失败", Toast.LENGTH_SHORT).show()
                                }
                                finish()
                            }
                        }
                    }
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        runOnUiThread {
                            // 处理请求失败情况，例如网络连接问题
                            Toast.makeText(
                                this@EditUserActivity,
                                "网络连接失败",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    }
                })
            }else{
                Toast.makeText(
                    this@EditUserActivity,
                    "请完善信息",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

    }

}