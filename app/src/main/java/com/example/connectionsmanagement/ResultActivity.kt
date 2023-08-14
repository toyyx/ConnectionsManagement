package com.example.connectionsmanagement


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.drawerlayout.widget.DrawerLayout
import com.allen.library.CircleImageView
import com.coorchice.library.SuperTextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result_activity_layout)

        Toast.makeText(this,"create",Toast.LENGTH_SHORT).show()

            //设置toolbar
            val toolbar: Toolbar = findViewById(R.id.toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.let {
                it.setDisplayHomeAsUpEnabled(true)
                it.setHomeAsUpIndicator(R.mipmap.ic_result_menu)
            }

            //设置左滑菜单
            val draLayout = findViewById<DrawerLayout>(R.id.resultShow)
            val navView = findViewById<NavigationView>(R.id.navView)
            navView.setCheckedItem(R.id.navMain)
            navView.setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.navMain -> {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                    R.id.navConnections -> {
                        val intent = Intent(this, ResultActivity::class.java)
                        startActivity(intent)
                    }
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
        Toast.makeText(this,"onStart",Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        Toast.makeText(this,"resume",Toast.LENGTH_SHORT).show()

        refresh()

        //设置按钮增加人物
        val addBt: FloatingActionButton = supportFragmentManager.findFragmentById(R.id.drawerFragment)!!.requireView().findViewById(R.id.addButton)
        addBt.setOnClickListener{
            val intent=Intent(this,PopAddHumanActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        //设置按钮... 功能暂未设计
        val delBt: FloatingActionButton = supportFragmentManager.findFragmentById(R.id.drawerFragment)!!.requireView().findViewById(R.id.deleteButton)
        delBt.setOnClickListener {

        }
    }

    override fun onPause() {
        super.onPause()
        Toast.makeText(this,"onPause",Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()
        Toast.makeText(this,"onStop",Toast.LENGTH_SHORT).show()
    }

    override fun onRestart() {
        super.onRestart()
        Toast.makeText(this,"onRestart",Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this,"onDestroy",Toast.LENGTH_SHORT).show()
    }

    //顶部菜单
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val draLayout=findViewById<DrawerLayout>(R.id.resultShow)
        when(item.itemId){
            android.R.id.home -> draLayout.openDrawer(GravityCompat.START)
        }
        return true
    }

    //刷新人脉图谱
    @SuppressLint("Range")
    fun refresh(){
        //连接数据库
        val dbHelper=ConnectionsDatabaseHelper(this,"ConnectionsStore.db",1)
        val db=dbHelper.writableDatabase
        val cursor=db.query("Human",null,null,null,null,null,null)
        if(cursor.moveToFirst()){
            val relativeHumanLayout=findViewById<RelativeLayout>(R.id.ConnectionsMap)
            val relativeLineLayout=findViewById<RelativeLayout>(R.id.ConnectionsLineMap)
            val connectionsList: ArrayList<MySuperTextView> = arrayListOf()
            do{
                val id=cursor.getInt(cursor.getColumnIndex("id"))
                val name=cursor.getString(cursor.getColumnIndex("name"))
                val notes=cursor.getString(cursor.getColumnIndex("notes"))

                //从数据库取出Bitmap数据
                val imageByteArray=cursor.getBlob(cursor.getColumnIndex("image_data"))
                val imageBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)

                //显示数据库信息 （仅测试阶段使用）
                Toast.makeText(this,"姓名:${name}备注:${notes}图片ByteArray:${imageByteArray}",Toast.LENGTH_SHORT).show()

                //创建人物视图
                val mySuperTextView=MySuperTextView(imageBitmap,name)
                connectionsList.add(mySuperTextView)
            }while(cursor.moveToNext())
            //在关系图上进行绘制
            createGraph(connectionsList,relativeHumanLayout,relativeLineLayout)
        }
        cursor.close()
    }

    //人脉关系图绘制 备注：目前仅实现关系第一圈，较多人物后将出现重叠
    private fun createGraph(mySuperTextViewList: ArrayList<MySuperTextView>,relativeHumanLayout:RelativeLayout,relativeLineLayout:RelativeLayout){
        val connectionsSize=mySuperTextViewList.size-1 //人际关系数量
        val radius = 300 //半径
        val angleIncrement = 2 * Math.PI / connectionsSize //增长角度
        val centerView = mySuperTextViewList[0] //中心人物 即：用户自身

        //每次刷新时清空关系图
        relativeHumanLayout.removeAllViews()
        relativeLineLayout.removeAllViews()

        //将中心人物放置关系图中心
        val centerViewParams=RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        centerViewParams.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE)
        relativeHumanLayout.addView(centerView,centerViewParams)

        //计算坐标并添加其余人物视图
        centerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // 在这里，视图已经被布局，此时才可以获取中心人物视图的位置
                val centerX = centerView.x
                val centerY = centerView.y

                // 计算其他视图位置
                for (i in 0 until connectionsSize) {
                    val angle = i * angleIncrement //角度
                    val x = (centerX + radius * cos(angle)).toInt() //x坐标
                    val y = (centerY + radius * sin(angle)).toInt() //y坐标

                    //获取人物视图
                    val view = mySuperTextViewList[i+1]
                    val viewParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
                    viewParams.leftMargin = x
                    viewParams.topMargin = y
                    relativeHumanLayout.addView(view, viewParams)

                    //添加人物视图之间的连线
                    view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            drawLineBetweenViews(relativeLineLayout,centerView,view)
                            view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        }
                    })

                }
                // 完成了需要在此回调中做的操作，注销监听器，以避免多次调用
                centerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    //在两个视图之间连线
    fun drawLineBetweenViews(layout: RelativeLayout, view1: View, view2: View) {
        // 计算两个视图的中心点
        val startX = view1.x + view1.width / 2
        val startY = view1.y + view1.height / 2
        val endX = view2.x + view2.width / 2
        val endY = view2.y + view2.height / 2

        // 创建一个新的View作为线条
        val line = View(this)
        line.setBackgroundColor(Color.BLACK)  // 设置线条颜色

        // 计算线条的位置和大小
        val width = sqrt((startX - endX).toDouble().pow(2.0) + (startY - endY).toDouble().pow(2.0)).toInt()
        val height=5
        val angle = atan2((endY - startY).toDouble(), (endX - startX).toDouble()) * 180 / Math.PI
        line.layoutParams = RelativeLayout.LayoutParams(width, height)

        // 设置线条的位置和旋转角度
        line.x = (startX + endX)/2- width/2
        line.y = (startY + endY)/2- 5/ 2
        line.rotation = angle.toFloat()

        // 将线条添加到布局中
        layout.addView(line)
    }
}