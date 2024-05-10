package com.example.connectionsmanagement.RegisterAndLogin

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import com.example.connectionsmanagement.R
import com.example.connectionsmanagement.Tools.ConnectionsManagementApplication
import com.example.connectionsmanagement.Tools.Tools.getFileFromUri
import com.example.connectionsmanagement.Tools.Tools.showUserAgreement
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.util.Locale


class RegisterActivity : AppCompatActivity(), View.OnClickListener {
    private var realCode: String? = null
    private var mBtRegisteractivityRegister: Button? = null
    private var mRlRegisteractivityTop: RelativeLayout? = null
    private var mIvRegisteractivityBack: ImageView? = null
    private var mLlRegisteractivityBody: LinearLayout? = null
    private var mEtRegisteractivityUsername: EditText? = null
    private var mEtRegisteractivityPassword1: EditText? = null
    private var mEtRegisteractivityPassword2: EditText? = null
    private var mEtRegisteractivityPhonecodes: EditText? = null
    private var mIvRegisteractivityShowcode:ImageView?=null
    private var mRlRegisteractivityBottom: LinearLayout? = null

    private var mEtRegisteractivityUserRealName: EditText? = null
    private var selectedGender:String ="男" //选中性别
    private var mEtRegisteractivityPhoneNumber: EditText? = null
    private var mEtRegisteractivityEmail: EditText? = null
    private var userAgreement_TV: TextView? = null
    private var userAgreement_CB: CheckBox? = null


    lateinit var imageUri: Uri  //图片地址
    private lateinit var getPicturesFromCameraActivity: ActivityResultLauncher<Uri>//拍照获取图片-启动器
    private lateinit var getPicturesFromAlbumActivity: ActivityResultLauncher<String> //相册获取图片-启动器

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        initView()//初始化

        //性别单选处理
        val radioGroup = findViewById<RadioGroup>(R.id.rg_registeractivity_gender)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            // 当选择不同的 RadioButton 时触发此回调
            val radioButton = findViewById<RadioButton>(checkedId)
            selectedGender = radioButton.text.toString()
        }

        //将验证码用图片的形式显示出来
        mIvRegisteractivityShowcode?.setImageBitmap(Code.instance?.createBitmap())
        realCode = Code.instance?.code?.lowercase(Locale.getDefault())

        //设置点击处理
        mIvRegisteractivityBack?.setOnClickListener(this)
        mIvRegisteractivityShowcode?.setOnClickListener(this)
        mBtRegisteractivityRegister?.setOnClickListener(this)
        userAgreement_TV?.setOnClickListener(this)

        //拍照获取图片处理过程
        getPicturesFromCameraActivity =registerForActivityResult(ActivityResultContracts.TakePicture()){
            if(it) {
                val imageView: ImageView? =findViewById<CircleImageView>(R.id.tv_registeractivity_userimage)
                imageView?.setImageURI(imageUri)
            }
        }
        //相册获取图片处理过程
        getPicturesFromAlbumActivity = registerForActivityResult(ActivityResultContracts.GetContent()) {
            val imageView: ImageView? =findViewById<CircleImageView>(R.id.tv_registeractivity_userimage)
            imageView?.setImageURI(it)
            imageUri=it
        }

        //设置人物图像并保存至本地
        findViewById<CircleImageView>(R.id.tv_registeractivity_userimage).setOnClickListener {
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

    }

    //初始化
    private fun initView() {
         mBtRegisteractivityRegister = findViewById<Button>(R.id.bt_registeractivity_register)
         mRlRegisteractivityTop = findViewById<RelativeLayout>(R.id.rl_registeractivity_top)
         mIvRegisteractivityBack = findViewById<ImageView>(R.id.iv_registeractivity_back)
         mLlRegisteractivityBody = findViewById<LinearLayout>(R.id.ll_registeractivity_body)
         mEtRegisteractivityUsername = findViewById<EditText>(R.id.et_registeractivity_username)
         mEtRegisteractivityPassword1 = findViewById<EditText>(R.id.et_registeractivity_password1)
         mEtRegisteractivityPassword2 = findViewById<EditText>(R.id.et_registeractivity_password2)
         mEtRegisteractivityPhonecodes = findViewById<EditText>(R.id.et_registeractivity_phoneCodes)
         mIvRegisteractivityShowcode = findViewById<ImageView>(R.id.iv_registeractivity_showCode)
         mRlRegisteractivityBottom = findViewById<LinearLayout>(R.id.ll_registeractivity_bottom)

        mEtRegisteractivityUserRealName = findViewById<EditText>(R.id.et_registeractivity_name)
        mEtRegisteractivityPhoneNumber = findViewById<EditText>(R.id.et_registeractivity_phonenumber)
        mEtRegisteractivityEmail = findViewById<EditText>(R.id.et_registeractivity_email)
        userAgreement_TV=findViewById(R.id.register_userAgreement_TextView)
        userAgreement_CB=findViewById(R.id.register_checkBoxOfuserAgreement)

    }

    //处理点击事件
    override fun onClick(view: View) {
        when (view.id) {

            //返回
            R.id.iv_registeractivity_back -> {
                finish()
            }

            //更换验证码
            R.id.iv_registeractivity_showCode -> {
                mIvRegisteractivityShowcode?.setImageBitmap(Code.instance?.createBitmap())
                realCode = Code.instance?.code?.lowercase(Locale.getDefault())
            }

            //注册
            R.id.bt_registeractivity_register -> {
                //获取用户输入的用户名、密码、验证码
                val username = mEtRegisteractivityUsername?.text.toString().trim { it <= ' ' }
                val password = mEtRegisteractivityPassword2?.text.toString().trim { it <= ' ' }
                val phoneCode = mEtRegisteractivityPhonecodes?.text.toString().lowercase(Locale.getDefault())
                val userRealName = mEtRegisteractivityUserRealName?.text.toString().trim { it <= ' ' }
                val gender = selectedGender
                val phoneNumber = mEtRegisteractivityPhoneNumber?.text.toString().trim { it <= ' ' }
                val email = mEtRegisteractivityEmail?.text.toString().trim { it <= ' ' }

                //注册验证
                if (userAgreement_CB!!.isChecked) {
                    if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(phoneCode)) {
                        if (phoneCode == realCode) {
                            //获取用户选择的图片文件
                            val selectedImageFile = getFileFromUri(imageUri)

                            // 创建OkHttpClient实例
                            val client = OkHttpClient()

                            // 构建MultipartBody，用于上传图片
                            val requestBody = MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("userName", username)
                                .addFormDataPart("password", password)
                                .addFormDataPart("name", userRealName)
                                .addFormDataPart("gender", gender)
                                .addFormDataPart("phone_number", phoneNumber)
                                .addFormDataPart("email", email)
                                .addFormDataPart(
                                    "image", "avatar.jpg",
                                    selectedImageFile!!.asRequestBody("image/*".toMediaTypeOrNull())
                                )
                                .build()

                            // 创建POST请求
                            val request = Request.Builder()
                                .url("http://121.199.71.143:8080/connection_server-1.0-SNAPSHOT/RegisterServlet")
                                .post(requestBody)
                                .build()

                            // 发送请求并处理响应
                            client.newCall(request).enqueue(object : Callback {
                                override fun onResponse(
                                    call: okhttp3.Call,
                                    response: okhttp3.Response
                                ) {
                                    // 处理服务器响应，根据需要更新UI或执行其他操作
                                    val responseBody = response.body?.string()
                                    // 处理服务器响应内容，这里的 responseBody 就是网页内容
                                    // 可以在这里对网页内容进行解析、处理等操作
                                    println("Server Response: $responseBody")
                                    // 解析 JSON 字符串为 JSON 对象
                                    val jsonObject = JSONObject(responseBody)
                                    runOnUiThread {
                                        if (jsonObject.getString("result") == "success") {
                                            Toast.makeText(
                                                ConnectionsManagementApplication.context,
                                                "注册成功\n" + jsonObject.getString("error_msg"),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val intent = Intent(
                                                ConnectionsManagementApplication.context,
                                                LoginActivity::class.java
                                            )
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            Toast.makeText(
                                                ConnectionsManagementApplication.context,
                                                "注册失败",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }

                                override fun onFailure(call: okhttp3.Call, e: IOException) {
                                    // 处理请求失败情况，例如网络连接问题
                                    Toast.makeText(
                                        ConnectionsManagementApplication.context,
                                        "网络连接失败",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                        } else {
                            Toast.makeText(this, "验证码错误,注册失败", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "未完善信息，注册失败", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(this, "请阅读并同意用户协议", Toast.LENGTH_SHORT).show()
                }
            }

            //用户协议
            R.id.register_userAgreement_TextView ->{
                showUserAgreement(this)
            }
        }
    }



}