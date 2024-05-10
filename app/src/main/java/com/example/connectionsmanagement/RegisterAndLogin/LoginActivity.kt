package com.example.connectionsmanagement.RegisterAndLogin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.connectionsmanagement.Tools.ConnectionsManagementApplication
import com.example.connectionsmanagement.Tools.Tools.downloadImage
import com.example.connectionsmanagement.ConnectionsMap.MainActivity
import com.example.connectionsmanagement.MysqlServer.MySQLConnection
import com.example.connectionsmanagement.R
import com.example.connectionsmanagement.Tools.Tools.showUserAgreement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private var mTvLoginactivityRegister: TextView? = null
    private var mRlLoginactivityTop: RelativeLayout? = null
    private var mEtLoginactivityUsername: EditText? = null
    private var mEtLoginactivityPassword: EditText? = null
    private var mLlLoginactivityTwo: LinearLayout? = null
    private var mBtLoginactivityLogin: Button? = null
    private var forgetPassword_TV: TextView? = null
    private var userAgreement_TV: TextView? = null
    private var userAgreement_CB: CheckBox? = null
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initView()
        // 设置点击事件监听器
        mBtLoginactivityLogin?.setOnClickListener(this)
        mTvLoginactivityRegister?.setOnClickListener(this)
        forgetPassword_TV?.setOnClickListener(this)
        userAgreement_TV?.setOnClickListener(this)
    }

    // 初始化控件
    private fun initView() {
        mBtLoginactivityLogin = findViewById<Button>(R.id.bt_loginactivity_login)
        mTvLoginactivityRegister = findViewById<TextView>(R.id.tv_loginactivity_register)
        mRlLoginactivityTop = findViewById<RelativeLayout>(R.id.rl_loginactivity_top)
        mEtLoginactivityUsername = findViewById<EditText>(R.id.et_loginactivity_username)
        mEtLoginactivityPassword = findViewById<EditText>(R.id.et_loginactivity_password)
        mLlLoginactivityTwo = findViewById<LinearLayout>(R.id.ll_loginactivity_two)
        forgetPassword_TV=findViewById(R.id.tv_loginactivity_forget)
        userAgreement_TV=findViewById(R.id.login_userAgreement_TextView)
        userAgreement_CB=findViewById(R.id.login_checkBoxOfuserAgreement)
    }

    //点击事件处理
    override fun onClick(view: View) {
        when (view.id) {

            //用户协议
            R.id.login_userAgreement_TextView ->{
                showUserAgreement(this)
            }

            //忘记密码
            R.id.tv_loginactivity_forget ->{
                // 创建一个对话框，并将滚动视图设置为其内容
                val builder = AlertDialog.Builder(this)
                builder.setTitle("忘记密码")
                builder.setMessage("请联系管理员21013132@mail.ecust.edu.cn")
                builder.setPositiveButton("确定") { dialog, which ->
                    dialog.dismiss()
                }
                // 显示对话框
                val dialog = builder.create()
                dialog.show()
            }

            //注册
            R.id.tv_loginactivity_register -> {
                startActivity(Intent(this, RegisterActivity::class.java))
            }

            //登录
            R.id.bt_loginactivity_login -> {
                val name = mEtLoginactivityUsername?.text.toString().trim { it <= ' ' }
//                Toast.makeText(ConnectionsManagementApplication.context,"输入用户${name}",Toast.LENGTH_SHORT).show();//调试使用
                val password = mEtLoginactivityPassword?.text.toString().trim { it <= ' ' }
                if(userAgreement_CB!!.isChecked) {
                    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(password)) {
                        GlobalScope.launch {
                            val job1 = async {
                                MySQLConnection.fetchWebpageContent(
                                    "Login",
                                    name,
                                    password
                                )
                            }
                            // 等待所有协程执行完毕，并获取结果
                            val jsonString: String = job1.await()
                            println("Server Response: $jsonString")
                            // 解析 JSON 字符串为 JSON 对象
                            val jsonObject = JSONObject(jsonString)
                            if (jsonObject.getString("result") == "success") {
                                ConnectionsManagementApplication.NowUser = User(
                                    jsonObject.getString("userId").toInt(),
                                    jsonObject.getString("userName"),
                                    jsonObject.getString("password"),
                                    jsonObject.getString("name"),
                                    jsonObject.getString("gender"),
                                    jsonObject.getString("image_path"),
                                    jsonObject.getString("phone_number"),
                                    jsonObject.getString("email")
                                )
                                val job = async {
                                    downloadImage(
                                        ConnectionsManagementApplication.context,
                                        ConnectionsManagementApplication.NowUser.image_path
                                    )
                                }
                                // 等待所有协程执行完毕，并获取结果
                                job.await()
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        ConnectionsManagementApplication.context,
                                        "登录成功",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    //进入主页面
                                    val intent = Intent(
                                        ConnectionsManagementApplication.context,
                                        MainActivity::class.java
                                    )
                                    startActivity(intent)
                                    finish() //销毁此Activity
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        ConnectionsManagementApplication.context,
                                        "登录失败",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "用户名和密码不可为空", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(this, "请阅读并同意用户协议", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}

