package com.example.connectionsmanagement.ConnectionsMap

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.connectionsmanagement.Calendar.CalendarFragment
import com.example.connectionsmanagement.ConnectionsMap.List.ListFragment
import com.example.connectionsmanagement.Face.FaceRecognitionActivity
import com.example.connectionsmanagement.Tools.ImageDownloader.RefreshUser
import com.example.connectionsmanagement.Tools.ImageDownloader.getBitmapFromLocalPath
import com.example.connectionsmanagement.R
import com.example.connectionsmanagement.RegisterAndLogin.EditUserActivity
import com.example.connectionsmanagement.RegisterAndLogin.LoginActivity
import com.example.connectionsmanagement.RegisterAndLogin.User
import com.example.connectionsmanagement.Relations.PopAddHumanActivity
import com.example.connectionsmanagement.Tools.ConnectionsManagementApplication
import com.example.connectionsmanagement.Tools.ImageDownloader
import com.example.connectionsmanagement.Tools.dpToPx
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    lateinit var nowUserPhone_TV:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawlayout=findViewById<DrawerLayout>(R.id.main_drawerLayout)
        nowUserPhone_TV=findViewById<TextView>(R.id.nowUserPhoneNumber_TextView)

        val List_fragment=ListFragment()
        val Drawer_fragment=DrawerFragment()
        val Calendar_fragment=CalendarFragment()

        // 创建一个 ArrayList，元素类型为 Fragment
        val fragmentList: ArrayList<Fragment> = ArrayList<Fragment>().apply{
            add(List_fragment)
            add(Drawer_fragment)
            add(Calendar_fragment)
        }
        saveFragment(fragmentList)
        showFragment(List_fragment)

        findViewById<Button>(R.id.toListFragment_Button).setOnClickListener {
            showFragment(List_fragment)
        }
        findViewById<Button>(R.id.toDrawerFragment_Button).setOnClickListener {
            showFragment(Drawer_fragment)
        }

        //底部导航栏
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.main_menu_item -> {
                    // 处理菜单项1的点击事件
                    showPopupWindow(bottomNavigationView)
                    true // 返回true表示事件已被处理
                }
                R.id.back_to_main->{
                    showFragment(List_fragment)
                    bottomNavigationView.menu.findItem(R.id.main_menu_item).setVisible(true)
                    bottomNavigationView.menu.findItem(R.id.back_to_main).setVisible(false)
                    findViewById<TextView>(R.id.nowTitle_textView).text = "人脉管理"
                    findViewById<Button>(R.id.toListFragment_Button).visibility=View.VISIBLE
                    findViewById<Button>(R.id.toDrawerFragment_Button).visibility=View.VISIBLE
                    true
                }

                R.id.main_menu_calendar -> {
                    // 处理菜单项1的点击事件
                    showFragment(Calendar_fragment)
                    bottomNavigationView.menu.findItem(R.id.main_menu_item).setVisible(false)
                    bottomNavigationView.menu.findItem(R.id.back_to_main).setVisible(true)
                    findViewById<TextView>(R.id.nowTitle_textView).text = "交际事件"
                    findViewById<Button>(R.id.toListFragment_Button).visibility=View.GONE
                    findViewById<Button>(R.id.toDrawerFragment_Button).visibility=View.GONE
                    true // 返回true表示事件已被处理
                }
                // 添加其他菜单项的处理逻辑
                else -> false // 返回false表示事件未被处理
            }
        }

        //右侧抽屉
        findViewById<CircleImageView>(R.id.toRightGrawer_userimage).setOnClickListener{
            drawlayout.openDrawer(Gravity.RIGHT)
        }

        //右侧抽屉的用户信息
        showUserInformation(ConnectionsManagementApplication.NowUser)

        findViewById<ImageButton>(R.id.editUser_ImageButton).setOnClickListener {
            val intent = Intent(this, EditUserActivity::class.java)
            startActivity(intent)
            drawlayout.closeDrawer(Gravity.RIGHT)
        }

        //左侧抽屉
        findViewById<ImageView>(R.id.toLeftGrawer_imageButton).setOnClickListener{
            drawlayout.openDrawer(Gravity.LEFT)
        }

        //左侧抽屉内容
        findViewById<Button>(R.id.exitLogin_Button).setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    override fun onResume() {
        super.onResume()
        Toast.makeText(this, "resume", Toast.LENGTH_SHORT).show()
        //刷新关系列表
        if(ConnectionsManagementApplication.IsUserChanged){
            ConnectionsManagementApplication.IsUserChanged=false
            GlobalScope.launch {
                val job1 = async {
                    RefreshUser()
                }
                job1.await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(ConnectionsManagementApplication.context, "用户已更新", Toast.LENGTH_SHORT).show()
                    showUserInformation(ConnectionsManagementApplication.NowUser)
                }
            }
        }

    }

    //主功能菜单
    private fun showPopupWindow(view: View) {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_layout, null)

        val width = resources.displayMetrics.widthPixels // 获取屏幕宽度
        // 创建 PopupWindow 对象
        val popupWindow = PopupWindow(
            popupView,
            width,
            500,
            true
        )
        // 创建圆角矩形形状
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.cornerRadius = 100f // 设置圆角半径，根据需要调整

        // 设置 PopupWindow 的背景为圆角矩形
        popupWindow.setBackgroundDrawable(drawable)

        // 计算偏移量，将PopupWindow显示在按钮的上方
        val xOffset = view.width / 2 - popupWindow!!.width / 2
        val yOffset = -view.height - popupWindow!!.height- dpToPx(10).toInt()

        // 设置PopupWindow显示在按钮上方，并相对于按钮的偏移量
        popupWindow?.showAsDropDown(view, xOffset, yOffset)

        // 点击PopupWindow外部区域时隐藏PopupWindow
        popupView.setOnTouchListener { _, _ ->
            popupWindow?.dismiss()
            true
        }

        popupView.findViewById<ImageButton>(R.id.addRelationButton).setOnClickListener {
            val intent = Intent(ConnectionsManagementApplication.context, PopAddHumanActivity::class.java)
            startActivity(intent)
            popupWindow.dismiss()
        }
        popupView.findViewById<ImageButton>(R.id.faceSearchButton).setOnClickListener {
            val intent = Intent(ConnectionsManagementApplication.context, FaceRecognitionActivity::class.java)
            startActivity(intent)
            popupWindow.dismiss()
        }
    }

    //更新右侧抽屉的用户信息
    fun showUserInformation(user: User){
        findViewById<CircleImageView>(R.id.toRightGrawer_userimage).setImageBitmap(getBitmapFromLocalPath(user.image_path!!))
        findViewById<CircleImageView>(R.id.nowUserImage_ImageView).setImageBitmap(getBitmapFromLocalPath(user.image_path!!))
        findViewById<TextView>(R.id.nowUserName_TextView).text = user.userName
        findViewById<TextView>(R.id.nowUserRealName_TextView).text = user.name
        findViewById<TextView>(R.id.nowUserGender_TextView).text = user.gender
        nowUserPhone_TV.text = user.phone_number
        findViewById<TextView>(R.id.nowUserEmail_TextView).text = user.email
    }

    //存储Fragment
    fun saveFragment(fragmentList:ArrayList<Fragment>){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        for (fragment in fragmentList) {
            // 在这里对每个 Fragment 进行操作
            // 例如显示 Fragment、获取 Fragment 的属性等
            fragmentTransaction.add(R.id.fragment_container, fragment)
            fragmentTransaction.hide(fragment)
        }
        fragmentTransaction.commit()
    }
    // 切换 Fragment 的方法
    fun showFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentManager.fragments.forEach { fragment ->
            fragmentTransaction.hide(fragment)
        }
        // 显示指定的 Fragment
        fragmentTransaction.show(fragment)
        if(fragment is CalendarFragment){
            fragment.adjustCalendar()
        }else if(fragment is DrawerFragment){
            if(ConnectionsManagementApplication.IsRelationsChanged_forDrawer) {
                ConnectionsManagementApplication.IsRelationsChanged_forDrawer=false
                GlobalScope.launch {
                    val job=async { ImageDownloader.RefreshRelations()}
                    job.await()
                    withContext(Dispatchers.Main) {
                        fragment.refresh()
                    }
                }
            }
        }else if(fragment is ListFragment){
            //刷新关系列表
            if(ConnectionsManagementApplication.IsRelationsChanged_forList){
                Toast.makeText(ConnectionsManagementApplication.context, "关系已变化", Toast.LENGTH_SHORT).show()
                ConnectionsManagementApplication.IsRelationsChanged_forList=false
                GlobalScope.launch {
                    val job1 = async {
                        fragment.newCardResults  = fragment.RefreshcardResults()
                    }
                    job1.await()
                    withContext(Dispatchers.Main) {
                        fragment.cardResults.clear() // 清空旧的 cardResults
                        fragment.cardResults.addAll(fragment.newCardResults) // 将新的 cardResults 添加到列表中
                        //Toast.makeText(ConnectionsManagementApplication.context, "人物名字：${cardResults[0].name}", Toast.LENGTH_SHORT).show()
                        fragment.adapter.notifyDataSetChanged()//通知数据变化
                    }
                }
            }
        }
        fragmentTransaction.commit()
    }

}