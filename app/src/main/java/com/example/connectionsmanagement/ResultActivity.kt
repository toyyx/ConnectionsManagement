package com.example.connectionsmanagement

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
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
//                it.setHomeAsUpIndicator(R.mipmap.ic_result_menu) //设置home键图标 即：toolbar最左侧按钮
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
        refresh() //刷新关系图
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

    //刷新人脉图谱
    @SuppressLint("Range")
    fun refresh(){
        //连接数据库
        val dbHelper=ConnectionsDatabaseHelper(this,"ConnectionsStore.db",1)
        val db=dbHelper.writableDatabase
        val cursor=db.query("Human",null,null,null,null,null,null)
        if(cursor.moveToFirst()){
            val relativeHumanLayout=findViewById<RelativeLayout>(R.id.ConnectionsHumanMap)
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

                //将视图存入关系队列
                connectionsList.add(mySuperTextView)

            }while(cursor.moveToNext())
            //在关系图上进行绘制
            createGraph(connectionsList,relativeHumanLayout,relativeLineLayout)
        }
        cursor.close()
    }

    //人脉关系图绘制 备注：目前已实现人物围绕中心绕圈分布，重叠时圆圈自动外扩，展现一圈圈的人脉关系图
    private fun createGraph(mySuperTextViewList: ArrayList<MySuperTextView>,relativeHumanLayout:RelativeLayout,relativeLineLayout:RelativeLayout){
        val baseRadius = 300 //基础半径
        val centerView = mySuperTextViewList[0] //中心人物 即：用户自身

        //每次刷新时清空关系图
        relativeHumanLayout.removeAllViews()
        relativeLineLayout.removeAllViews()

        //将中心人物放置关系图中心
        val centerViewParams=RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        centerViewParams.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE)
        relativeHumanLayout.addView(centerView,centerViewParams)

        //待中心视图布置完成后，添加其余人物视图
        centerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                //获取中心人物视图的位置
                val centerX = centerView.x
                val centerY = centerView.y

                //视图重叠最大间距
                val viewSpacing =sqrt(centerView.width.toDouble().pow(2.0) + centerView.height.toDouble().pow(2.0))
                val finishedViewGroup: ArrayList<MySuperTextView> = arrayListOf() //本圈视图队列

                //绘制其余人物视图
                refreshAll(mySuperTextViewList,finishedViewGroup,centerX,centerY,baseRadius,relativeHumanLayout,relativeLineLayout,viewSpacing,1)

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
        line.y = (startY + endY)/2- height/ 2
        line.rotation = angle.toFloat()

        // 将线条添加到布局中
        layout.addView(line)
    }

    //判断两视图是否重叠
    fun viewsOverlap(view1: View, view2: View,viewSpacing:Double): Boolean {
        val viewDistance=sqrt((view1.x-view2.x).toDouble().pow(2.0) + (view1.y-view2.y).toDouble().pow(2.0))
        return viewDistance<viewSpacing
    }
    fun viewsOverlap(viewX: Int,viewY: Int,compareView: View,viewSpacing:Double): Boolean {
        val viewDistance=sqrt((viewX-compareView.x).toDouble().pow(2.0) + (viewY-compareView.y).toDouble().pow(2.0))
        return viewDistance<viewSpacing
    }

    //判断视图与视图组是否重叠
    fun viewsGroupOverlap(newView: View,currentViewGroup:ArrayList<MySuperTextView>,viewSpacing:Double):Boolean{
        var result = false
        for(tempView in currentViewGroup){
            if(viewsOverlap(newView,tempView,viewSpacing)){
                result=true
            }
        }
        return result
    }
    fun viewsGroupOverlap(newViewX: Int,newViewY: Int,currentViewGroup:ArrayList<MySuperTextView>,viewSpacing:Double):Boolean{
        var result = false
        for(tempView in currentViewGroup){
            if(viewsOverlap(newViewX,newViewY,tempView,viewSpacing)){
                result=true
            }
        }
        return result
    }

    //将视图放入布局
    fun arrangeView(newView: View,X:Int,Y:Int,relativeLayout:RelativeLayout){
        val viewParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        viewParams.leftMargin = X
        viewParams.topMargin = Y
        relativeLayout.addView(newView, viewParams)
    }

    //刷新除中心外的所有视图 备注：需要在中心视图布置完成后使用
    fun refreshAll(mySuperTextViewList:ArrayList<MySuperTextView>,finishedViewGroup:ArrayList<MySuperTextView>,centerX:Float,centerY:Float,currentRadius:Int,humanLayout:RelativeLayout,lineLayout:RelativeLayout,viewSpacing:Double,location:Int){
        //判断当前布置视图进度
        if(location+1>mySuperTextViewList.size){
            //当完成所有视图的布置后，进行视图与中心的连线
            for(view in mySuperTextViewList){
                drawLineBetweenViews(lineLayout,humanLayout.getChildAt(0),view)
            }
        }else{
            //视图布置进行中，开始新视图布置
            val newView=mySuperTextViewList[location] //新视图
            finishedViewGroup.add(newView)  //加入本圈视图队列
            val currentViewSize=finishedViewGroup.size //本圈视图数量
            var currentAngle= 0.0 //当前角度
            var angleIncrement=2 * Math.PI/currentViewSize //本圈角度增加量
            var curRadius=currentRadius //当前半径
            val radiusIncrement=100 //扩圈时的半径增加量

            //调整本圈其余视图位置
            for (i in humanLayout.childCount-currentViewSize+1 until humanLayout.childCount) {
                humanLayout.getChildAt(i).x = (centerX + curRadius * cos(currentAngle)).toFloat()
                humanLayout.getChildAt(i).y = (centerY - curRadius * sin(currentAngle)).toFloat()
                currentAngle += angleIncrement
            }

            //布置本圈新视图
            var x = (centerX + curRadius * cos(currentAngle)).toInt() //x坐标
            var y = (centerY - curRadius * sin(currentAngle)).toInt() //y坐标
            arrangeView(newView, x, y, humanLayout)

            //设置本圈恢复标志
            var restoreFlag=false

            //布置新视图后判断是否重叠
            while(viewsGroupOverlap(x, y,ArrayList(mySuperTextViewList.take(location)),viewSpacing)){
                //重叠后恢复本圈其余视图至正确位置
                if(!restoreFlag){
                    currentAngle=0.0
                    angleIncrement=2 * Math.PI/(currentViewSize-1)
                    for (i in humanLayout.childCount-currentViewSize until humanLayout.childCount) {
                        humanLayout.getChildAt(i).x = (centerX + curRadius * cos(currentAngle)).toFloat()
                        humanLayout.getChildAt(i).y = (centerY - curRadius * sin(currentAngle)).toFloat()
                        currentAngle += angleIncrement
                    }
                    restoreFlag=true //本圈已恢复
                }

                //移除新视图，清空本圈视图队列
                finishedViewGroup.clear()
                humanLayout.removeView(newView)

                //开启新圈
                finishedViewGroup.add(newView) //新视图为新圈第一个视图
                curRadius += radiusIncrement //新圈半径
                x = (centerX + curRadius * cos(0.0)).toInt() //x坐标
                y = (centerY - curRadius * sin(0.0)).toInt() //y坐标
                arrangeView(newView,x,y,humanLayout) //布置新视图
            }

            //待当前视图布局完成后，进行下一个视图的布局
            newView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    newView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    refreshAll(mySuperTextViewList,finishedViewGroup,centerX,centerY,curRadius,humanLayout,lineLayout,viewSpacing,location+1)
                }
            })
        }
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
            android.R.id.home -> {val draLayout=findViewById<DrawerLayout>(R.id.resultShow)
                                        draLayout.openDrawer(GravityCompat.START)}
            //添加人物
            R.id.addHuman -> {val intent=Intent(this,PopAddHumanActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intent)}
            //待定...
            R.id.delHuman -> Toast.makeText(this, "You clicked delHuman",
                Toast.LENGTH_SHORT).show()
        }
        return true
    }

}

