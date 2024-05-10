package com.example.connectionsmanagement.ConnectionsMap

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.connectionsmanagement.Calendar.CalendarFragment
import com.example.connectionsmanagement.ConnectionsMap.List.ListFragment
import com.example.connectionsmanagement.Face.FaceRecognitionActivity
import com.example.connectionsmanagement.R
import com.example.connectionsmanagement.RegisterAndLogin.EditUserActivity
import com.example.connectionsmanagement.RegisterAndLogin.LoginActivity
import com.example.connectionsmanagement.RegisterAndLogin.User
import com.example.connectionsmanagement.Relations.PopAddHumanActivity
import com.example.connectionsmanagement.Tools.ConnectionsManagementApplication
import com.example.connectionsmanagement.Tools.Tools
import com.example.connectionsmanagement.Tools.Tools.RefreshUser
import com.example.connectionsmanagement.Tools.Tools.dpToPx
import com.example.connectionsmanagement.Tools.Tools.getBitmapFromLocalPath
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//主功能界面
class MainActivity : AppCompatActivity() {
    lateinit var nowUserPhone_TV:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawlayout=findViewById<DrawerLayout>(R.id.main_drawerLayout)
        nowUserPhone_TV=findViewById<TextView>(R.id.nowUserPhoneNumber_TextView)

        val List_fragment=ListFragment()//列表展示fragment
        val Drawer_fragment=DrawerFragment()//图谱展示fragment
        val Calendar_fragment=CalendarFragment()//交际日历fragment

        //利用supportFragmentManager存储和展示fragment
        val fragmentList: ArrayList<Fragment> = ArrayList<Fragment>().apply{
            add(List_fragment)
            add(Drawer_fragment)
            add(Calendar_fragment)
        }
        saveFragment(fragmentList)
        showFragment(List_fragment)

        //列表展示按钮
        findViewById<Button>(R.id.toListFragment_Button).setOnClickListener {
            showFragment(List_fragment)
        }

        //图谱展示按钮
        findViewById<Button>(R.id.toDrawerFragment_Button).setOnClickListener {
            showFragment(Drawer_fragment)
        }

        //底部导航栏
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.main_menu_item -> {//弹出功能菜单
                    showPopupWindow(bottomNavigationView)
                    true // 返回true表示事件已被处理
                }
                R.id.back_to_main->{//返回人脉展示界面
                    showFragment(List_fragment)
                    bottomNavigationView.menu.findItem(R.id.main_menu_item).setVisible(true)
                    bottomNavigationView.menu.findItem(R.id.back_to_main).setVisible(false)
                    findViewById<TextView>(R.id.nowTitle_textView).text = "人脉管理"
                    findViewById<Button>(R.id.toListFragment_Button).visibility=View.VISIBLE
                    findViewById<Button>(R.id.toDrawerFragment_Button).visibility=View.VISIBLE
                    true
                }

                R.id.main_menu_calendar -> {//交际日历页面
                    showFragment(Calendar_fragment)
                    bottomNavigationView.menu.findItem(R.id.main_menu_item).setVisible(false)
                    bottomNavigationView.menu.findItem(R.id.back_to_main).setVisible(true)
                    findViewById<TextView>(R.id.nowTitle_textView).text = "交际事件"
                    findViewById<Button>(R.id.toListFragment_Button).visibility=View.GONE
                    findViewById<Button>(R.id.toDrawerFragment_Button).visibility=View.GONE
                    true
                }
                // 添加其他菜单项的处理逻辑
                else -> false // 返回false表示事件未被处理
            }
        }

        //右侧抽屉
        findViewById<CircleImageView>(R.id.toRightGrawer_userimage).setOnClickListener{
            drawlayout.openDrawer(Gravity.RIGHT)
        }

        //更新右侧抽屉的用户信息
        showUserInformation(ConnectionsManagementApplication.NowUser)

        //编辑用户信息按钮
        findViewById<ImageButton>(R.id.editUser_ImageButton).setOnClickListener {
            val intent = Intent(this, EditUserActivity::class.java)
            startActivity(intent)
            drawlayout.closeDrawer(Gravity.RIGHT)
        }

        //左侧抽屉
        findViewById<ImageView>(R.id.toLeftGrawer_imageButton).setOnClickListener{
            drawlayout.openDrawer(Gravity.LEFT)
        }

        //退出登录按钮
        findViewById<Button>(R.id.exitLogin_Button).setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        //展示软件使用说明弹窗
        findViewById<TextView>(R.id.main_show_useTips_TV).setOnClickListener {
            showUseTips()
        }
        //create时展示一次软件使用说明
        showUseTips()
    }

    override fun onResume() {
        super.onResume()
//        Toast.makeText(this, "resume", Toast.LENGTH_SHORT).show()//调试使用
        //当用户数据变化时，更新本地用户数据
        if(ConnectionsManagementApplication.IsUserChanged){
            ConnectionsManagementApplication.IsUserChanged=false
            GlobalScope.launch {
                val job1 = async {
                    RefreshUser()
                }
                job1.await()
                withContext(Dispatchers.Main) {
//                    Toast.makeText(ConnectionsManagementApplication.context, "用户已更新", Toast.LENGTH_SHORT).show()//调试使用
                    showUserInformation(ConnectionsManagementApplication.NowUser)//更新用户展示信息
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

        //添加人物菜单项
        popupView.findViewById<ImageButton>(R.id.addRelationButton).setOnClickListener {
            //进入人物添加页面
            val intent = Intent(ConnectionsManagementApplication.context, PopAddHumanActivity::class.java)
            startActivity(intent)
            popupWindow.dismiss()
        }

        //人脸识别菜单项
        popupView.findViewById<ImageButton>(R.id.faceSearchButton).setOnClickListener {
            //进入人脸识别功能页面
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
        for (fragment in fragmentList) {//存储并隐藏fragment
            fragmentTransaction.add(R.id.fragment_container, fragment)
            fragmentTransaction.hide(fragment)
        }
        fragmentTransaction.commit()
    }

    // 切换 Fragment 的方法
    fun showFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        //隐藏所有fragment
        fragmentManager.fragments.forEach { fragment ->
            fragmentTransaction.hide(fragment)
        }

        // 显示指定的 Fragment
        fragmentTransaction.show(fragment)
        if(fragment is CalendarFragment){//切换交际日历时调整选中日期
            fragment.adjustCalendar()
        }else if(fragment is DrawerFragment){//切换图谱时检查人脉更新
            if(ConnectionsManagementApplication.IsRelationsChanged_forDrawer) {
                ConnectionsManagementApplication.IsRelationsChanged_forDrawer=false
                GlobalScope.launch {
                    val job=async { Tools.RefreshRelations()}
                    job.await()
                    withContext(Dispatchers.Main) {
                        fragment.refresh()
                    }
                }
            }
        }else if(fragment is ListFragment){//切换列表时检查人脉更新
            //刷新关系列表
            if(ConnectionsManagementApplication.IsRelationsChanged_forList){
//                Toast.makeText(ConnectionsManagementApplication.context, "关系已变化", Toast.LENGTH_SHORT).show()//调试使用
                ConnectionsManagementApplication.IsRelationsChanged_forList=false
                GlobalScope.launch {
                    val job1 = async {
                        fragment.newListResults  = fragment.RefreshListResults()
                    }
                    job1.await()
                    withContext(Dispatchers.Main) {
                        fragment.listResults.clear() // 清空旧的 cardResults
                        fragment.listResults.addAll(fragment.newListResults) // 将新的 cardResults 添加到列表中
                        fragment.adapter.notifyDataSetChanged()//通知数据变化
                    }
                }
            }
        }
        fragmentTransaction.commit()
    }

    //软件使用说明弹窗
    fun showUseTips(){
        val instructions = """
                    欢迎使用《人脉管理小助手》！
                    
                    本应用旨在帮助您高效管理人脉，轻松记录交际日程，并提供便捷的人脸识别功能。
            
            1. 人脉管理：
                    - 记录人脉信息：您可以轻松记录人脉的基本信息，包括姓名、联系方式、备注等。
                    - 增删改查操作：您可以对已有的人脉信息进行添加、删除、编辑和查询操作，以便及时管理和更新您的人脉关系。
                    - 人脉展示方式：您可以选择以列表方式或图谱方式展示人脉信息，以满足不同需求的查看方式。
            
            2. 交际日历：
                    - 记录交际事件：您可以在日历上记录与相识人脉的交际事件，包括已发生的和即将发生的事件。
                    - 操作便捷：在日历上进行操作，您可以轻松查看当日的交际事件，并进行事件的增加、删除、编辑和查询。
            
            3. 人脸识别：
                    - 人脸检测：软件支持人脸检测功能，帮助您识别照片中的人脸。
                    - 人脸对比：您可以进行人脸对比，以确定两张照片中的人是否为同一人。
                    - 人脸搜索（1：N）：在当前人脉库中进行单张人脸的搜索，以快速查找匹配的人脉信息。
                    - 人脸搜索（M：N）：支持在当前人脉库中进行多张人脸的搜索，以寻找可能的匹配结果。
            
                    我们希望《人脉管理小助手》能够为您的人脉管理提供便利，并提升您的交际效率。如果您在使用过程中有任何问题或建议，欢迎随时联系我们21013132@mail.ecust.edu.cn
            
                    祝您使用愉快！
        """.trimIndent()
        // 创建一个对话框，并将滚动视图设置为其内容
        val builder = AlertDialog.Builder(this)
        builder.setTitle("《人脉管理小助手》使用说明")
        builder.setMessage(instructions)
        builder.setPositiveButton("确定") { dialog, which ->
            dialog.dismiss()
        }

        // 显示对话框
        val dialog = builder.create()
        dialog.show()
    }


}