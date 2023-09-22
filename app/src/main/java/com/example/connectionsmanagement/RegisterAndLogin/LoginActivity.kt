package com.example.connectionsmanagement.RegisterAndLogin

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.connectionsmanagement.ConnectionsMap.ConnectionsManagementApplication
import com.example.connectionsmanagement.R
import com.example.connectionsmanagement.ConnectionsMap.ResultActivity

/**
 * 此类 implements View.OnClickListener 之后，
 * 就可以把onClick事件写到onCreate()方法之外
 * 这样，onCreate()方法中的代码就不会显得很冗余
 */
class LoginActivity : AppCompatActivity(), View.OnClickListener {
    /**
     * 声明自己写的 DBOpenHelper 对象
     * DBOpenHelper(extends SQLiteOpenHelper) 主要用来
     * 创建数据表
     * 然后再进行数据表的增、删、改、查操作
     */
    private var mDBOpenHelper: DBOpenHelper? = null
    private var mTvLoginactivityRegister: TextView? = null
    private var mRlLoginactivityTop: RelativeLayout? = null
    private var mEtLoginactivityUsername: EditText? = null
    private var mEtLoginactivityPassword: EditText? = null
    private var mLlLoginactivityTwo: LinearLayout? = null
    private var mBtLoginactivityLogin: Button? = null

    /**
     * 创建 Activity 时先来重写 onCreate() 方法
     * 保存实例状态
     * super.onCreate(savedInstanceState);
     * 设置视图内容的配置文件
     * setContentView(R.layout.activity_login);
     * 上面这行代码真正实现了把视图层 View 也就是 layout 的内容放到 Activity 中进行显示
     * 初始化视图中的控件对象 initView()
     * 实例化 DBOpenHelper，待会进行登录验证的时候要用来进行数据查询
     * mDBOpenHelper = new DBOpenHelper(this);
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initView()
        mDBOpenHelper = DBOpenHelper(this)
        // 设置点击事件监听器
        mBtLoginactivityLogin?.setOnClickListener(this)
        mTvLoginactivityRegister?.setOnClickListener(this)
    }

    /**
     * onCreae()中大的布局已经摆放好了，接下来就该把layout里的东西
     * 声明、实例化对象然后有行为的赋予其行为
     * 这样就可以把视图层View也就是layout 与 控制层 Java 结合起来了
     */
    private fun initView() {
        // 初始化控件
         mBtLoginactivityLogin = findViewById<Button>(R.id.bt_loginactivity_login)
         mTvLoginactivityRegister = findViewById<TextView>(R.id.tv_loginactivity_register)
         mRlLoginactivityTop = findViewById<RelativeLayout>(R.id.rl_loginactivity_top)
         mEtLoginactivityUsername = findViewById<EditText>(R.id.et_loginactivity_username)
         mEtLoginactivityPassword = findViewById<EditText>(R.id.et_loginactivity_password)
         mLlLoginactivityTwo = findViewById<LinearLayout>(R.id.ll_loginactivity_two)


    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tv_loginactivity_register -> {
                startActivity(Intent(this, RegisterActivity::class.java))
                finish()
            }

            R.id.bt_loginactivity_login -> {
                val name = mEtLoginactivityUsername?.text.toString().trim { it <= ' ' }
                Toast.makeText(ConnectionsManagementApplication.context,"输入用户${name}",Toast.LENGTH_SHORT).show();
                val password = mEtLoginactivityPassword?.text.toString().trim { it <= ' ' }
                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(password)) {
                    val data = mDBOpenHelper!!.allData
                    var match = false
                    var i = 0
                    while (i < data.size) {
                        val user = data[i]
                        if (name == user.name && password == user.password) {
                            match = true
                            break
                        } else {
                            match = false
                        }
                        i++
                    }
                    if (match) {
                        Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, ResultActivity::class.java)
                        startActivity(intent)
                        finish() //销毁此Activity
                    } else {
                        Toast.makeText(this, "用户名或密码不正确，请重新输入", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(this, "请输入你的用户名或密码", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}