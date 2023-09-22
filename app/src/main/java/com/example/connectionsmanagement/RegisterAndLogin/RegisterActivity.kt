package com.example.connectionsmanagement.RegisterAndLogin

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.connectionsmanagement.R
import com.example.connectionsmanagement.ConnectionsMap.ResultActivity
import java.util.Locale

/**
 * 此类 implements View.OnClickListener 之后，
 * 就可以把onClick事件写到onCreate()方法之外
 * 这样，onCreate()方法中的代码就不会显得很冗余
 */
class RegisterActivity : AppCompatActivity(), View.OnClickListener {
    private var realCode: String? = null
    var mDBOpenHelper: DBOpenHelper? = null
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        initView()
        mDBOpenHelper = DBOpenHelper(this)

        //将验证码用图片的形式显示出来
        mIvRegisteractivityShowcode?.setImageBitmap(Code.instance?.createBitmap())
        realCode = Code.instance?.code?.lowercase(Locale.getDefault())

        mIvRegisteractivityBack?.setOnClickListener(this)
        mIvRegisteractivityShowcode?.setOnClickListener(this)
        mBtRegisteractivityRegister?.setOnClickListener(this)
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
                //获取用户输入的用户名、密码、验证码
                val username = mEtRegisteractivityUsername?.text.toString().trim { it <= ' ' }
                val password = mEtRegisteractivityPassword2?.text.toString().trim { it <= ' ' }
                val phoneCode =
                    mEtRegisteractivityPhonecodes?.text.toString().lowercase(Locale.getDefault())
                //注册验证
                if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(
                        phoneCode
                    )
                ) {
                    if (phoneCode == realCode) {
                        //将用户名和密码加入到数据库中
                        mDBOpenHelper?.add(username, password)
                        val intent2 = Intent(this, ResultActivity::class.java)
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
}