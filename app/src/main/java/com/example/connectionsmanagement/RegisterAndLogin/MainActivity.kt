package com.example.connectionsmanagement.RegisterAndLogin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.connectionsmanagement.R

/**
 * 此类 implements View.OnClickListener 之后，
 * 就可以把onClick事件写到onCreate()方法之外
 * 这样，onCreate()方法中的代码就不会显得很冗余
 */
class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        // 初始化控件对象
        val mBtMainLogout = findViewById<Button>(R.id.bt_main_logout)
        // 绑定点击监听器
        mBtMainLogout.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        if (view.id == R.id.bt_main_logout) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}