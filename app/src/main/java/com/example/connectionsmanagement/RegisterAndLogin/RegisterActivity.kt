package com.example.connectionsmanagement.RegisterAndLogin

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import com.example.connectionsmanagement.ConnectionsMap.ConnectionsDatabaseHelper
import com.example.connectionsmanagement.ConnectionsMap.ConnectionsManagementApplication
import com.example.connectionsmanagement.R
import com.example.connectionsmanagement.ConnectionsMap.ResultActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDateTime
import java.util.Locale

/**
 * 此类 implements View.OnClickListener 之后，
 * 就可以把onClick事件写到onCreate()方法之外
 * 这样，onCreate()方法中的代码就不会显得很冗余
 */
class RegisterActivity : AppCompatActivity(), View.OnClickListener {
    private var realCode: String? = null
    var mDBOpenHelper: ConnectionsDatabaseHelper? = null
    private var mBtRegisteractivityRegister: Button? = null
    private var mRlRegisteractivityTop: RelativeLayout? = null
    private var mIvRegisteractivityBack: ImageView? = null
    private var mLlRegisteractivityBody: LinearLayout? = null
    private var mEtRegisteractivityUsername: EditText? = null
    private var mEtRegisteractivityPassword1: EditText? = null
    private var mEtRegisteractivityPassword2: EditText? = null
    private var mEtRegisteractivityPhonecodes: EditText? = null
    private var mIvRegisteractivityShowcode:ImageView?=null
    private var mRlRegisteractivityBottom: RelativeLayout? = null

    //
    lateinit var imageUri: Uri  //图片地址
    private lateinit var getPicturesFromCameraActivity: ActivityResultLauncher<Uri>//拍照获取图片-启动器
    private lateinit var getPicturesFromAlbumActivity: ActivityResultLauncher<String> //相册获取图片-启动器
    //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        initView()
        mDBOpenHelper = ConnectionsDatabaseHelper(this,1)

        //将验证码用图片的形式显示出来
        mIvRegisteractivityShowcode?.setImageBitmap(Code.instance?.createBitmap())
        realCode = Code.instance?.code?.lowercase(Locale.getDefault())

        mIvRegisteractivityBack?.setOnClickListener(this)
        mIvRegisteractivityShowcode?.setOnClickListener(this)
        mBtRegisteractivityRegister?.setOnClickListener(this)

        //
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
        //
    }

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
         mRlRegisteractivityBottom = findViewById<RelativeLayout>(R.id.rl_registeractivity_bottom)
        /**
         * 注册页面能点击的就三个地方
         * top处返回箭头、刷新验证码图片、注册按钮
         */

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_registeractivity_back -> {
                val intent1 = Intent(this, LoginActivity::class.java)
                startActivity(intent1)
                finish()
            }

            R.id.iv_registeractivity_showCode -> {
                mIvRegisteractivityShowcode?.setImageBitmap(Code.instance?.createBitmap())
                realCode = Code.instance?.code?.lowercase(Locale.getDefault())
            }

            R.id.bt_registeractivity_register -> {
                //保存照片至本地
                val imageBitmap=getBitmapFromUri(imageUri)
                //转为将bitmap转为字节数组，便于后续数据库存储
                val stream = ByteArrayOutputStream()
                //quality设为100时，程序将因照片过大而崩溃，有待后续优化
                imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 75, stream)
                val imageByteArray = stream.toByteArray()

                //获取用户输入的用户名、密码、验证码
                val username = mEtRegisteractivityUsername?.text.toString().trim { it <= ' ' }
                val password = mEtRegisteractivityPassword2?.text.toString().trim { it <= ' ' }
                val phoneCode = mEtRegisteractivityPhonecodes?.text.toString().lowercase(Locale.getDefault())
                //注册验证
                if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(
                        phoneCode
                    )
                ) {
                    if (phoneCode == realCode) {
                        //将用户名和密码加入到数据库中,加入用户个人信息
                        ConnectionsManagementApplication.NowUserId= mDBOpenHelper?.addUser(username, password)!!
                        mDBOpenHelper?.addUserInformation(ConnectionsManagementApplication.NowUserId,username,imageByteArray)

                        val intent2 = Intent(this, LoginActivity::class.java)
                        startActivity(intent2)
                        finish()
                        Toast.makeText(this, "验证通过，注册成功", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "验证码错误,注册失败", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "未完善信息，注册失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //uri转变为bitmap
    fun getBitmapFromUri(uri:Uri?)=uri?.let {  contentResolver.openFileDescriptor(uri,"r")?.use {
        BitmapFactory.decodeFileDescriptor(it.fileDescriptor) }}
}