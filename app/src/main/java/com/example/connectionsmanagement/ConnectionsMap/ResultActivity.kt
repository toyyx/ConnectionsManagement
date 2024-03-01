package com.example.connectionsmanagement.ConnectionsMap

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.connectionsmanagement.R
import com.google.android.material.navigation.NavigationView
import de.hdodenhof.circleimageview.CircleImageView


class ResultActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchResultAdapter
    val dbHelper = ConnectionsDatabaseHelper(ConnectionsManagementApplication.context, 1)
    val db = dbHelper.writableDatabase

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result_activity_layout)
//        Toast.makeText(this, "create", Toast.LENGTH_SHORT).show()

        //设置右侧抽屉的搜索功能
        //获取RecyclerView
        recyclerView = findViewById(R.id.recyclerViewSearchResults)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val searchResults = arrayListOf<SearchPerson>()//搜索结果队列
        adapter = SearchResultAdapter(searchResults)
        recyclerView.adapter = adapter

        //监听搜索框
        findViewById<EditText>(R.id.editTextSearch).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                // 在文本改变之前执行的操作，用于限制输入内容
                // charSequence 包含了当前文本内容和即将输入的字符
            }
            //实时搜索
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                searchResults.clear()//清空搜索结果队列
                // 在文本改变过程中实时触发的操作,charSequence包含了当前文本内容和正在输入的字符
                val input=charSequence.toString()//获取输入内容
                val table1="Connections"//表1
                val table2="Person"//表2
                val joinCondition = "Connections.personId = Person.personId" // 两表的连接条件
                val cursor = db.query("$table1 INNER JOIN $table2 ON $joinCondition", null, "userId=? and name LIKE ?", arrayOf("${ConnectionsManagementApplication.NowUserId}","%$input%"), null, null, null)
                if (cursor.moveToFirst()) {
                    do{
                        var imageByteArray = cursor.getBlob(cursor.getColumnIndex("image_data"))
                        var imageBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
                        var name = cursor.getString(cursor.getColumnIndex("name"))
                        var notes = cursor.getString(cursor.getColumnIndex("notes"))
                        val mySearchPerson=SearchPerson(imageBitmap,name,notes)
                        searchResults.add(mySearchPerson)//加入搜索结果队列
                    }while (cursor.moveToNext())
                }
                adapter.notifyDataSetChanged()//通知数据变化
            }

            override fun afterTextChanged(editable: Editable) {
                // 在文本改变完成后执行的操作
                // editable 包含了最终的文本内容
            }
        })

        //设置toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
//                it.setHomeAsUpIndicator(R.mipmap.ic_result_menu) //设置home键图标 即：toolbar最左侧按钮
        }

        //设置左侧抽屉显示的用户个人信息
        // 查找 NavigationView
        val navigationView = findViewById<NavigationView>(R.id.navView)
        // 获取 NavigationView 的 headerLayout 视图
        val headerView = navigationView?.getHeaderView(0)
        //数据库中查询用户信息
        val cursor = db.query("UserInformation", null, "userId=?", arrayOf("${ConnectionsManagementApplication.NowUserId}"), null, null, null)
        if (cursor.moveToFirst()) {
            var name = cursor.getString(cursor.getColumnIndex("name"))
            var email = cursor.getString(cursor.getColumnIndex("email"))
            //从数据库取出Bitmap数据
            var imageByteArray = cursor.getBlob(cursor.getColumnIndex("image_data"))
            var imageBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
            headerView?.findViewById<TextView>(R.id.userName)?.text=name//用户名
            headerView?.findViewById<TextView>(R.id.userMail)?.text=email//用户邮箱
            headerView?.findViewById<CircleImageView>(R.id.userImage)?.setImageBitmap(imageBitmap)//用户头像
        }

        //设置左滑菜单
        val draLayout = findViewById<DrawerLayout>(R.id.resultShow)
        val navView = findViewById<NavigationView>(R.id.navView)
        navView.setCheckedItem(R.id.navMain)
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                //跳转个人主页
                R.id.navMain -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }

                //跳转人脉关系展示界面
                R.id.navConnections -> {
                    val intent = Intent(this, ResultActivity::class.java)
                    startActivity(intent)
                }

                //关于本软件的弹窗
                R.id.navAbout -> {
                    AlertDialog.Builder(this).apply {
                        setTitle("关于本软件")
                        setMessage("软件名称：人脉管理\n\n软件说明：用于科学管理人际关系\n\n开发人员：姚扬鑫 徐润 张凯璇 陆孜逸")
                        setCancelable(false)
                        setPositiveButton("OK") { _, _ -> }
                        show()
                    }
                }
            }
            draLayout.closeDrawers()
            true
        }
    }

    override fun onStart() {
        super.onStart()
//        Toast.makeText(this, "onStart", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
//        Toast.makeText(this, "resume", Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
//        Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()
//        Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show()
    }

    override fun onRestart() {
        super.onRestart()
//        Toast.makeText(this, "onRestart", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
//        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show()
    }


//    //视角回归中心 备注：目前未使用
//    fun toCenter(){
//        val connectionsMapLayout= findViewById<RelativeLayout>(R.id.ConnectionsMap)
//        val displayMetrics = resources.displayMetrics
//        val screenWidth = displayMetrics.widthPixels
//        val screenHeight = displayMetrics.heightPixels
//        connectionsMapLayout.translationX = - dpToPx(1500)+screenWidth/2
//        connectionsMapLayout.translationY = - dpToPx(1500)+screenHeight/2
//    }

    //关系图界面的顶部菜单
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.result_toolbar, menu)
        return true
    }

    //顶部菜单的功能
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //打开侧边菜单
            android.R.id.home -> {
                val draLayout = findViewById<DrawerLayout>(R.id.resultShow)
                draLayout.openDrawer(GravityCompat.START)
            }
            //选择添加人物方式
            R.id.addHuman -> {
                val popupMenu = PopupMenu(this, findViewById(R.id.addHuman)) // 创建PopupMenu，传入上下文和关联的视图
                popupMenu.menuInflater.inflate(R.menu.manual_or_automatic_to_add, popupMenu.menu) // 加载菜单资源
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_manual -> {
                            // 处理手动的点击事件
                            val intent = Intent(this, PopAddHumanActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            true
                        }
                        R.id.menu_automatic -> {
                            // 处理自动的点击事件（待开发）
                            true
                        }
                        // 添加更多菜单项的处理逻辑
                        else -> false
                    }
                }
                popupMenu.show() // 显示弹出菜单}
            }
                //打开搜索人物界面
                R.id.searchHuman -> {val draLayout = findViewById<DrawerLayout>(R.id.resultShow)
                    draLayout.openDrawer(GravityCompat.END)}
            }
            return true
        }

}
