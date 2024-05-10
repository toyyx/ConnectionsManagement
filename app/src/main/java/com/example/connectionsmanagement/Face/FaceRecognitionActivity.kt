package com.example.connectionsmanagement.Face

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.connectionsmanagement.Face.FaceDetect.FaceDetect_Fragment
import com.example.connectionsmanagement.Face.FaceSearch_1_N.FaceSearch_1_N_Fragment
import com.example.connectionsmanagement.Face.FaceSearch_M_N.FaceSearch_M_N_Fragment
import com.example.connectionsmanagement.R
import com.google.android.material.navigation.NavigationView

//人脸识别功能activity
class FaceRecognitionActivity : AppCompatActivity() {
    lateinit var loadingCL: ConstraintLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_recognition)

        //加载中的动画
        loadingCL=findViewById(R.id.loadingImage_ConstraintLayout)
        loadingCL.setOnClickListener {
            // 执行点击事件的操作
            true // 表示点击事件已被消费
        }

        val FaceDetect_fragment= FaceDetect_Fragment()
        val FaceMatch_fragment=FaceMatch_Fragment()
        val FaceSearch_1_N_fragment= FaceSearch_1_N_Fragment()
        val FaceSearch_M_N_fragment= FaceSearch_M_N_Fragment()

        //存储并切换fragment
        val fragmentList: ArrayList<Fragment> = ArrayList<Fragment>().apply{
            add(FaceDetect_fragment)
            add(FaceMatch_fragment)
            add(FaceSearch_1_N_fragment)
            add(FaceSearch_M_N_fragment)
        }
        saveFragment(fragmentList)
        showFragment(FaceDetect_fragment)

        //返回按钮
        findViewById<ImageView>(R.id.back_faceRecognition_ImageView).setOnClickListener {
            finish()
        }

        //人脸功能列表
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
                    showFragment(FaceSearch_1_N_fragment)
                    true
                }
                R.id.face_menu_search_M_N -> {
                    showFragment(FaceSearch_M_N_fragment)
                    true
                }
                // 添加其他菜单项的处理逻辑
                else -> false // 返回false表示事件未被处理
            }
        }
    }

    //存储fragment
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

    //切换fragment
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