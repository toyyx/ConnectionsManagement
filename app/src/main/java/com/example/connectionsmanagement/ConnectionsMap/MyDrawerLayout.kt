package com.example.connectionsmanagement.ConnectionsMap

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.RelativeLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentContainerView
import com.example.connectionsmanagement.R
import java.lang.Float.max
import java.lang.Float.min

//为方便获取点击触摸事件，制定MyDrawerLayout
class MyDrawerLayout (context: Context, attrs: AttributeSet) : DrawerLayout(context, attrs) {

    //设置平移与缩放所需变量
    private val gestureDetector: GestureDetector
    private val scaleGestureDetector: ScaleGestureDetector
    private var scale = 1f
    private var offsetX = 0f
    private var offsetY = 0f
    private var firstTouch = true


    init {
        //设置平移功能
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                val connectionsMapLayout= findViewById<RelativeLayout>(R.id.ConnectionsMap)
                    //获取实际平移后的中心位置
                    offsetX -=   distanceX
                    offsetY -=   distanceY

                    //修正位置，展现最终平移后的视图
                    adjustPosition(connectionsMapLayout)
                return true
            }
        })

        //设置缩放功能
        scaleGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.OnScaleGestureListener {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val connectionsMapLayout: RelativeLayout = findViewById(R.id.ConnectionsMap)  //需要缩放的视图
                val containerLayout= findViewById<FragmentContainerView>(R.id.fragment_container) //借助此视图，方便获取关系图显示区域的宽高
                val scaleFactor = detector.scaleFactor ?: 1f
                scale *= scaleFactor //缩放后的倍数
                val minScale = max(width/dpToPx(3000) ,containerLayout.height/dpToPx(3000)) //最小倍数
                val maxScale = 3f //最大倍数
                scale = max(minScale, min(scale, maxScale)) //修正后的倍数

                //缩放视图
                connectionsMapLayout.scaleX = scale
                connectionsMapLayout.scaleY = scale

                //修正视图
                adjustPosition(connectionsMapLayout)

                return true
            }
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean=true
            override fun onScaleEnd(detector: ScaleGestureDetector) {}
        })
    }

    fun dpToPx(dp: Int): Float {
        return dp * ConnectionsManagementApplication.context.resources.displayMetrics.density
    }

    //修正视图位置
    //备注：translationX/Y函数是相对于原视图中心点的偏移
    private fun adjustPosition(view: View) {
        val containerLayout = findViewById<FragmentContainerView>(R.id.fragment_container)
        //计算X/Y轴上最大/小偏移量
        val maxOffsetX = -(view.width/2-view.width*scale/2)
        val minOffsetX = -(view.width/2-width + view.width * scale/2)
        val maxOffsetY = -(view.height/2-view.height*scale/2)
        val minOffsetY = -(view.height/2-containerLayout.height + view.height * scale/2)
        //调整位置
        offsetX = min(maxOffsetX, max(offsetX, minOffsetX))
        offsetY = min(maxOffsetY, max(offsetY, minOffsetY))
        view.translationX = offsetX
        view.translationY = offsetY
    }

    //利用相对于窗口的绝对位置，修正视图位置
    //备注：在实现检查边界功能的过程中想到一种方法，但最终未使用，考虑到后续开发可能需要，暂时留存此方法
//    fun checkPosition(view: View){
//        //状态栏高度
//        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
//        val statusBarHeight = if (resourceId > 0) {
//            resources.getDimensionPixelSize(resourceId)
//        } else { 0 }
//        //获取视图的绝对坐标 （相对于窗口左上角）
//        val location=IntArray(2)
//        view.getLocationInWindow(location)
//        val containerLayout = findViewById<FragmentContainerView>(R.id.drawerFragment)
//        val toolbarLayout=findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
//        val maxOffsetX = -(view.width/2-view.width*scale/2)
//        val minOffsetX = -(view.width/2-width + view.width * scale/2)
//        val maxOffsetY = -(view.height/2-view.height*scale/2)
//        val minOffsetY = -(view.height/2-containerLayout.height + view.height * scale/2)
//        if(location[0]>maxOffsetX){
//            view.translationX=-(view.width-view.width*scale)/2
//        }else if(location[0]<minOffsetX){
//            view.translationX=-(view.width/2+view.width*scale/2-width)
//        }
//        if(location[1]>maxOffsetY+statusBarHeight+toolbarLayout.height){
//            view.translationY=-(view.height-view.height*scale)/2
//        }else if(location[1]<minOffsetY+statusBarHeight+toolbarLayout.height){
//            view.translationY=-(view.height/2+view.height*scale/2-containerLayout.height)
//        }
//    }

    //设置初始显示为视图中心
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
//        if (firstTouch) {
//            val connectionsMapLayout = findViewById<RelativeLayout>(R.id.ConnectionsMap)
//            val containerLayout= findViewById<FragmentContainerView>(R.id.fragment_container)
//            offsetX = (width - connectionsMapLayout.width) / 2f
//            offsetY = (containerLayout.height - connectionsMapLayout.height) / 2f
//            connectionsMapLayout.translationX = offsetX
//            connectionsMapLayout.translationY = offsetY
//            firstTouch = false
//        }
    }

    //处理触摸事件
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 检查右侧栏是否处于打开状态
        if (isDrawerOpen(GravityCompat.START) or isDrawerOpen(GravityCompat.END)) {
            // 如果右侧栏打开，不执行自定义触摸操作
            return super.onTouchEvent(event)
        }
        // 让 DrawerLayout 处理它的默认行为 如：侧边菜单打开时，点击阴影处，将关闭菜单
        super.onTouchEvent(event)

        //自定义处理触摸事件
        gestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)

        //后续需要利用触摸的手指数量时，可利用以下方法
//        val pointerCount = event.pointerCount
//        if (pointerCount == 1){
//            Toast.makeText(ConnectionsManagementApplication.context,"1个手指",Toast.LENGTH_SHORT).show()
//            gestureDetector.onTouchEvent(event)
//
//        }else if (pointerCount == 2){
//            Toast.makeText(ConnectionsManagementApplication.context,"2个手指",Toast.LENGTH_SHORT).show()
//            scaleGestureDetector.onTouchEvent(event)
//        }
        if (event.action == MotionEvent.ACTION_UP) {
            performClick()
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

}