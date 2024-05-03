package com.example.connectionsmanagement.Face

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.connectionsmanagement.Face.FaceDetect.FaceDetect_Fragment
import com.example.connectionsmanagement.Face.FaceSearch_1_N.FaceSearch_1_N_Fragment
import com.example.connectionsmanagement.Face.FaceSearch_M_N.FaceSearch_M_N_Fragment
import com.example.connectionsmanagement.R
import com.example.connectionsmanagement.Tools.ConnectionsManagementApplication
import com.google.android.material.navigation.NavigationView


class FaceRecognitionActivity : AppCompatActivity() {
    lateinit var loadingCL: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_recognition)
        loadingCL=findViewById(R.id.loadingImage_ConstraintLayout)
        loadingCL.setOnClickListener {
            // 执行点击事件的操作
            true // 表示点击事件已被消费
        }
        val FaceDetect_fragment= FaceDetect_Fragment()
        val FaceMatch_fragment=FaceMatch_Fragment()
        val FaceSearch_1_N_fragment= FaceSearch_1_N_Fragment()
        val FaceSearch_M_N_fragment= FaceSearch_M_N_Fragment()
        // 创建一个 ArrayList，元素类型为 Fragment
        val fragmentList: ArrayList<Fragment> = ArrayList<Fragment>().apply{
            add(FaceDetect_fragment)
            add(FaceMatch_fragment)
            add(FaceSearch_1_N_fragment)
            add(FaceSearch_M_N_fragment)
        }
        saveFragment(fragmentList)
        showFragment(FaceDetect_fragment)

        findViewById<ImageView>(R.id.back_faceRecognition_ImageView).setOnClickListener {
            finish()
        }

        findViewById<NavigationView>(R.id.face_activity_navigationView).setNavigationItemSelectedListener  { menuItem ->
            when (menuItem.itemId) {
                R.id.face_menu_detect -> {
                    showFragment(FaceDetect_fragment)
                    true // 返回true表示事件已被处理
                }
                R.id.face_menu_match->{
                    showFragment(FaceMatch_fragment)
                    true
                }
                R.id.face_menu_search_1_N -> {
                    // 处理菜单项1的点击事件
                    showFragment(FaceSearch_1_N_fragment)
                    true // 返回true表示事件已被处理
                }
                R.id.face_menu_search_M_N -> {
                    // 处理菜单项1的点击事件
                    showFragment(FaceSearch_M_N_fragment)
                    true // 返回true表示事件已被处理
                }
                // 添加其他菜单项的处理逻辑
                else -> false // 返回false表示事件未被处理
            }
        }
    }

    fun saveFragment(fragmentList:ArrayList<Fragment>){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        for (fragment in fragmentList) {
            // 在这里对每个 Fragment 进行操作
            // 例如显示 Fragment、获取 Fragment 的属性等
            fragmentTransaction.add(R.id.face_fragment_container, fragment)
            fragmentTransaction.hide(fragment)
        }
        fragmentTransaction.commit()
    }

    fun showFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentManager.fragments.forEach { fragment ->
            fragmentTransaction.hide(fragment)
        }
        // 显示指定的 Fragment
        fragmentTransaction.show(fragment)
        fragmentTransaction.commit()
    }


}