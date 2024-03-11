package com.example.connectionsmanagement.ConnectionsMap

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.connectionsmanagement.ConnectionsMap.ImageDownloader.downloadImage
import com.example.connectionsmanagement.ConnectionsMap.ImageDownloader.getBitmapFromLocalPath
import com.example.connectionsmanagement.MysqlServer.MySQLConnection
import com.example.connectionsmanagement.MysqlServer.Relation
import com.example.connectionsmanagement.R
import com.example.connectionsmanagement.RegisterAndLogin.LoginActivity
import com.example.connectionsmanagement.RegisterAndLogin.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CardPersonAdapter
    var cardResults = arrayListOf<CardPerson>()//搜索结果队列
    val dbHelper = ConnectionsDatabaseHelper(ConnectionsManagementApplication.context, 1)
    val db = dbHelper.writableDatabase

    lateinit var newCardResults: ArrayList<CardPerson>

//    override fun onActivityCreated(){
//
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        switchFragment(ListFragment())

        findViewById<Button>(R.id.toListFragment_Button).setOnClickListener {
            switchFragment(ListFragment())
        }
        findViewById<Button>(R.id.toDrawerFragment_Button).setOnClickListener {
            switchFragment(DrawerFragment())
        }

//        //获取RecyclerView
//        recyclerView = findViewById(R.id.cardRecyclerView)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//
//        adapter = CardPersonAdapter(cardResults)
//        recyclerView.adapter = adapter
//        //获取人际关系列表
//        GlobalScope.launch {
//            val job1 = async {
//                newCardResults  = RefreshcardResults()
//            }
//            job1.await()
//            withContext(Dispatchers.Main) {
//                cardResults.clear() // 清空旧的 cardResults
//                cardResults.addAll(newCardResults) // 将新的 cardResults 添加到列表中
//                //Toast.makeText(ConnectionsManagementApplication.context, "人物名字：${cardResults[0].name}", Toast.LENGTH_SHORT).show()
//                adapter.notifyDataSetChanged()//通知数据变化
//            }
//        }

        //底部导航栏
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.main_menu_item -> {
                    // 处理菜单项1的点击事件
                    showPopupWindow(bottomNavigationView)
                    Toast.makeText(this,"已点击",Toast.LENGTH_SHORT).show() // 显示Toast消息
                    true // 返回true表示事件已被处理
                }
                // 添加其他菜单项的处理逻辑
                else -> false // 返回false表示事件未被处理
            }
        }

        //右侧抽屉
        findViewById<CircleImageView>(R.id.toRightGrawer_userimage).setOnClickListener{
            findViewById<DrawerLayout>(R.id.main_drawerLayout).openDrawer(Gravity.RIGHT)
        }

        //右侧抽屉的用户信息
        showUserInformation(ConnectionsManagementApplication.NowUser)

        //左侧抽屉
        findViewById<ImageView>(R.id.toLeftGrawer_imageButton).setOnClickListener{
            findViewById<DrawerLayout>(R.id.main_drawerLayout).openDrawer(Gravity.LEFT)
        }

        //左侧抽屉内容
        findViewById<Button>(R.id.exitLogin_Button).setOnClickListener{
            val intent = Intent(ConnectionsManagementApplication.context, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    override fun onResume() {
        super.onResume()
//        //刷新关系列表
//        if(ConnectionsManagementApplication.IsRelationsChanged){
//            Toast.makeText(this, "关系已变化", Toast.LENGTH_SHORT).show()
//            ConnectionsManagementApplication.IsRelationsChanged=false
//            GlobalScope.launch {
//                val job1 = async {
//                    newCardResults  = RefreshcardResults()
//                }
//                job1.await()
//                withContext(Dispatchers.Main) {
//                    cardResults.clear() // 清空旧的 cardResults
//                    cardResults.addAll(newCardResults) // 将新的 cardResults 添加到列表中
//                    Toast.makeText(ConnectionsManagementApplication.context, "人物名字：${cardResults[0].name}", Toast.LENGTH_SHORT).show()
//                    adapter.notifyDataSetChanged()//通知数据变化
//                }
//            }
//            //adapter.notifyDataSetChanged()//通知数据变化
//        }
//        Toast.makeText(this, "resume", Toast.LENGTH_SHORT).show()
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
            width,
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
    }

//    private suspend fun RefreshRelations(){
//        //将 JSON 字符串 jsonString 解析为 ArrayList<Relation>
//        val jsonString =MySQLConnection.fetchWebpageContent("SearchRelations",ConnectionsManagementApplication.NowUser.userId.toString(),"")
//        val listType = object : TypeToken<ArrayList<Relation>>() {}.type
//        ConnectionsManagementApplication.NowRelations = Gson().fromJson(jsonString, listType)
//    }

//    private suspend fun RefreshcardResults(): ArrayList<CardPerson>{
//        RefreshRelations()
//        var cardResults= arrayListOf<CardPerson>()
//        //下载图片
//        ConnectionsManagementApplication.NowRelations.forEach {
//            downloadImage(ConnectionsManagementApplication.context,it.image_path)
//            val bitmap=BitmapFactory.decodeFile(externalCacheDir.toString()+ImageDownloader.getSpecialFromString(it.image_path, "data_image/"))
//            // 判断是否成功解码
//            if (bitmap == null) {
//                // 解码失败
//                println("Failed to decode bitmap from file")
//            } else {
//                // 解码成功
//                println("Bitmap decoded successfully from file")
//                println(externalCacheDir.toString())
//            }
//            val myCardPerson=CardPerson(bitmap,it.name,it.relationship,it.phone_number,it.notes)
//            //releaseBitmap(bitmap)//
//            cardResults.add(myCardPerson)//加入搜索结果队列
//        }
//        return cardResults
//
//    }



    //更新右侧抽屉的用户信息
    fun showUserInformation(user: User){
        findViewById<CircleImageView>(R.id.toRightGrawer_userimage).setImageBitmap(getBitmapFromLocalPath(user.image_path!!))
        findViewById<CircleImageView>(R.id.nowUserImage_ImageView).setImageBitmap(getBitmapFromLocalPath(user.image_path!!))
        findViewById<TextView>(R.id.nowUserName_TextView).text = user.userName
        findViewById<TextView>(R.id.nowUserRealName_TextView).text = user.name
        findViewById<TextView>(R.id.nowUserGender_TextView).text = user.gender
        findViewById<TextView>(R.id.nowUserPhoneNumber_TextView).text = user.phone_number
        findViewById<TextView>(R.id.nowUserEmail_TextView).text = user.email
    }

    // 切换 Fragment 的方法
    fun switchFragment(fragment: Fragment) {
        val  fragmentManager: FragmentManager = supportFragmentManager;
        val  fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }


}