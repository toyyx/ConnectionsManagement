package com.example.connectionsmanagement.ConnectionsMap
//为配合drawer设置的Fragment，具体实现在ResultActivity
import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.contentValuesOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import com.example.connectionsmanagement.ConnectionsMap.ImageDownloader.getBitmapFromLocalPath
import com.example.connectionsmanagement.MysqlServer.MySQLConnection
import com.example.connectionsmanagement.MysqlServer.Relation
import com.example.connectionsmanagement.R
import com.google.gson.Gson
import com.google.gson.JsonObject
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


class DrawerFragment : Fragment() {
    // TODO: Rename and change types of parameters

    //展示关系选中变量
    lateinit var thisView:View
    var selectedRelations = arrayListOf("朋友","亲人", "同学", "其他")

    val popupLayout = LayoutInflater.from(ConnectionsManagementApplication.context).inflate(R.layout.pop_human_detail, null)
    //获取浏览、编辑状态的控件
    val humanImage = popupLayout.findViewById<CircleImageView>(R.id.popImage)
    val humanName_edit = popupLayout.findViewById<EditText>(R.id.popNameEditText)
    val humanGender_edit = popupLayout.findViewById<EditText>(R.id.popGenderEditText)
    val humanPhone_edit = popupLayout.findViewById<EditText>(R.id.popPhoneEditText)
    val humanEmail_edit = popupLayout.findViewById<EditText>(R.id.popEmailEditText)
    val humanNotes_edit = popupLayout.findViewById<EditText>(R.id.popNotesEditText)
    val yes_to_adjust_button=popupLayout.findViewById<Button>(R.id.YesToAdjust)
    val no_to_adjust_button=popupLayout.findViewById<Button>(R.id.NoToAdjust)

    //设置平移与缩放所需变量
    lateinit var gestureDetector: GestureDetector
    lateinit var scaleGestureDetector: ScaleGestureDetector
    private var scale = 1f
    private var offsetX = 0f
    private var offsetY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(ConnectionsManagementApplication.context, "drawerfragment onCreate", Toast.LENGTH_SHORT).show()

    }

    override fun onStart() {
        super.onStart()
        Toast.makeText(ConnectionsManagementApplication.context, "drawerfragment onStart", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        Toast.makeText(ConnectionsManagementApplication.context, "drawerfragment onResume", Toast.LENGTH_SHORT).show()
        if(ConnectionsManagementApplication.IsRelationsChanged==true) {
            ConnectionsManagementApplication.IsRelationsChanged=false
            GlobalScope.launch {
                val job=async {ImageDownloader.RefreshRelations()}
                job.await()
                withContext(Dispatchers.Main) {
                    refresh()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //关系选择发生变动后刷新界面
        thisView = inflater.inflate(R.layout.drawer_fragment, container, false)

        thisView.findViewById<CheckBox>(R.id.checkBoxOfFriend).setOnCheckedChangeListener { _, isChecked ->
            // 当 CheckBox 的选中状态发生变化时触发的操作
            if(isChecked){
                selectedRelations.add("朋友")
            }else{
                selectedRelations.remove("朋友")
            }

            refresh()
        }
        thisView.findViewById<CheckBox>(R.id.checkBoxOfFamily).setOnCheckedChangeListener { buttonView, isChecked ->
            // 当 CheckBox 的选中状态发生变化时触发的操作
            if(isChecked){
                selectedRelations.add("亲人")
            }else{
                selectedRelations.remove("亲人")
            }
            refresh()
        }
        thisView.findViewById<CheckBox>(R.id.checkBoxOfClassmate).setOnCheckedChangeListener { buttonView, isChecked ->
            // 当 CheckBox 的选中状态发生变化时触发的操作
            if(isChecked){
                selectedRelations.add("同学")
            }else{
                selectedRelations.remove("同学")
            }
            refresh()
        }
        thisView.findViewById<CheckBox>(R.id.checkBoxOfOther).setOnCheckedChangeListener { buttonView, isChecked ->
            // 当 CheckBox 的选中状态发生变化时触发的操作
            if(isChecked){
                selectedRelations.add("其他")
            }else{
                selectedRelations.remove("其他")
            }
            refresh()
        }

        //设置平移功能
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                val connectionsMapLayout= thisView.findViewById<RelativeLayout>(R.id.ConnectionsMap)
                //获取实际平移后的中心位置
                offsetX -=   distanceX
                offsetY -=   distanceY

                //修正位置，展现最终平移后的视图
                adjustPosition(connectionsMapLayout)
                return true
            }
        })

        //设置缩放功能
        scaleGestureDetector = ScaleGestureDetector(ConnectionsManagementApplication.context, object : ScaleGestureDetector.OnScaleGestureListener {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val connectionsMapLayout: RelativeLayout = thisView.findViewById(R.id.ConnectionsMap)  //需要缩放的视图
                val containerLayout= requireActivity().findViewById<FragmentContainerView>(R.id.fragment_container) //借助此视图，方便获取关系图显示区域的宽高
                val scaleFactor = detector.scaleFactor ?: 1f
                scale *= scaleFactor //缩放后的倍数
                val minScale = java.lang.Float.max(
                    thisView.width / dpToPx(3000),
                    thisView.height / dpToPx(3000)
                ) //最小倍数
                val maxScale = 3f //最大倍数
                scale = java.lang.Float.max(minScale, java.lang.Float.min(scale, maxScale)) //修正后的倍数

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

        thisView.setOnTouchListener { _, event ->
            // 让 DrawerLayout 处理它的默认行为 如：侧边菜单打开时，点击阴影处，将关闭菜单
            //super.onTouchEvent(event)

            //自定义处理触摸事件
            gestureDetector.onTouchEvent(event)
            scaleGestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP) {
                thisView.performClick()
            }
            return@setOnTouchListener true
        }
        refresh()
        return thisView
    }


    companion object {
        // TODO: Rename and change types and number of parameters

    }

    fun dpToPx(dp: Int): Float {
        return dp * ConnectionsManagementApplication.context.resources.displayMetrics.density
    }

    //修正视图位置
    //备注：translationX/Y函数是相对于原视图中心点的偏移
    private fun adjustPosition(view: View) {
        //计算X/Y轴上最大/小偏移量
        val maxOffsetX = view.width * scale/2-thisView.width/2
        val minOffsetX = -(view.width * scale/2-thisView.width/2)
        val maxOffsetY = view.height * scale/2-thisView.height/2
        val minOffsetY = -(view.height * scale/2-thisView.height/2)

        //调整位置
        offsetX = java.lang.Float.min(maxOffsetX, java.lang.Float.max(offsetX, minOffsetX))
        offsetY = java.lang.Float.min(maxOffsetY, java.lang.Float.max(offsetY, minOffsetY))
        view.translationX = offsetX
        view.translationY = offsetY
    }

    //刷新人脉图谱
    fun refresh() {
        val relativeHumanLayout = thisView.findViewById<RelativeLayout>(R.id.ConnectionsHumanMap)
        val relativeLineLayout = thisView.findViewById<RelativeLayout>(R.id.ConnectionsLineMap)
        val connectionsList: ArrayList<MySuperTextView> = arrayListOf()
        lateinit var userSuperTextView:MySuperTextView

        //查询用户信息并创建视图
        userSuperTextView=MySuperTextView(0,getBitmapFromLocalPath(ConnectionsManagementApplication.NowUser.image_path!!),ConnectionsManagementApplication.NowUser.name!!)

        //查询用户的人物关系
        ConnectionsManagementApplication.NowRelations.forEach {
            if(selectedRelations.contains(it.relationship)){
                val temp_relation=it
                //创建人物视图
                val mySuperTextView = MySuperTextView(it.personId, getBitmapFromLocalPath(it.image_path), it.name)
                //将视图存入关系队列
                connectionsList.add(mySuperTextView)
                //设置人物详情弹窗
                mySuperTextView.setOnClickListener {
                    //将查询到的人物信息显示出来
                    humanImage.setImageBitmap(getBitmapFromLocalPath(temp_relation.image_path))
                    humanName_edit.setText(temp_relation.name)
                    humanGender_edit.setText(temp_relation.gender)
                    humanPhone_edit.setText(temp_relation.phone_number)
                    humanEmail_edit.setText(temp_relation.email)
                    humanNotes_edit.setText(temp_relation.notes)

                    // 创建人物详情弹窗
                    val popupWindow = PopupWindow(popupLayout, dpToPx(200).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT, true)
                    // 设置点击外部区域关闭 PopupWindow
                    popupWindow.isOutsideTouchable = false
                    popupWindow.setBackgroundDrawable(ColorDrawable(80000000))
                    popupWindow.showAtLocation(thisView, Gravity.CENTER, 0, 0)

                    //设置弹窗右上角操作菜单
                    popupLayout.findViewById<Button>(R.id.popOperationButton).setOnClickListener {
                        val popupMenu = PopupMenu(this.requireContext(), it) // 创建PopupMenu，传入上下文和关联的视图
                        popupMenu.menuInflater.inflate(R.menu.adjust_or_delete_person, popupMenu.menu) // 加载菜单资源
                        popupMenu.setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                // 处理修改的点击事件
                                R.id.menu_adjust -> {
                                    //切换显示控件（转化为可编辑控件）
                                    humanName_edit.isEnabled=true
                                    humanGender_edit.isEnabled=true
                                    humanPhone_edit.isEnabled=true
                                    humanEmail_edit.isEnabled=true
                                    humanNotes_edit.isEnabled=true
                                    yes_to_adjust_button.visibility=View.VISIBLE
                                    no_to_adjust_button.visibility=View.VISIBLE
                                    //确定修改人物信息
                                    yes_to_adjust_button.setOnClickListener {
                                        // 创建OkHttpClient实例
                                        val client = OkHttpClient()

                                        // 构建MultipartBody，用于上传图片
                                        val requestBody = MultipartBody.Builder()
                                            .setType(MultipartBody.FORM)
                                            .addFormDataPart("personId",temp_relation.personId.toString())
                                            .addFormDataPart("name",humanName_edit.text.toString())
                                            .addFormDataPart("gender",humanGender_edit.text.toString())
                                            .addFormDataPart("phone_number",humanPhone_edit.text.toString())
                                            .addFormDataPart("email",humanEmail_edit.text.toString())
                                            .addFormDataPart("notes",humanNotes_edit.text.toString())
                                            .build()

                                        // 创建POST请求
                                        val request = Request.Builder()
                                            .url("http://121.199.71.143:8080/connection_server-1.0-SNAPSHOT/UpdateRelationServlet")
                                            .post(requestBody)
                                            .build()

                                        // 发送请求并处理响应
                                        client.newCall(request).enqueue(object : Callback {
                                            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                                                // 处理服务器响应，根据需要更新UI或执行其他操作
                                                val responseBody =  response.body?.string()//JsonString
                                                if (responseBody != null) {
                                                    // 处理服务器响应内容，这里的 responseBody 就是网页内容
                                                    // 可以在这里对网页内容进行解析、处理等操作
                                                    println("Server Response: $responseBody")
                                                    activity?.runOnUiThread {
                                                        // 将JSON字符串解析为JsonObject
                                                        val jsonObject = Gson().fromJson(responseBody, JsonObject::class.java)
                                                        // 读取特定键的值
                                                        if(jsonObject["result"].asString=="success"){
                                                            Toast.makeText(ConnectionsManagementApplication.context,"修改关系成功",Toast.LENGTH_SHORT).show()
                                                            ConnectionsManagementApplication.IsRelationsChanged=true
                                                        }else{
                                                            Toast.makeText(ConnectionsManagementApplication.context,"修改关系失败",Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            }
                                            override fun onFailure(call: okhttp3.Call, e: IOException) {
                                                activity?.runOnUiThread {
                                                    // 处理请求失败情况，例如网络连接问题
                                                    Toast.makeText(ConnectionsManagementApplication.context, "网络连接失败", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        })
                                        humanName_edit.isEnabled=false
                                        humanGender_edit.isEnabled=false
                                        humanPhone_edit.isEnabled=false
                                        humanEmail_edit.isEnabled=false
                                        humanNotes_edit.isEnabled=false
                                        yes_to_adjust_button.visibility=View.GONE
                                        no_to_adjust_button.visibility=View.GONE
                                        popupWindow.dismiss()//关闭弹窗
                                    }
                                    no_to_adjust_button.setOnClickListener {
                                        humanName_edit.isEnabled=false
                                        humanGender_edit.isEnabled=false
                                        humanPhone_edit.isEnabled=false
                                        humanEmail_edit.isEnabled=false
                                        humanNotes_edit.isEnabled=false
                                        yes_to_adjust_button.visibility=View.GONE
                                        no_to_adjust_button.visibility=View.GONE
                                        popupWindow.dismiss() }
                                    true
                                }
                                // 处理删除的点击事件
                                R.id.menu_delete -> {
                                    //创建对话框
                                    val alertDialog = AlertDialog.Builder(this.requireContext())
                                        .setTitle("确认删除")
                                        .setMessage("确认删除这个人物吗？")
                                        .setCancelable(false)
                                        .setPositiveButton("确认") { dialog, which ->
                                            // 点击确认按钮，执行删除操作
                                            GlobalScope.launch {
                                                val job1 = async { deleteRelation(temp_relation.personId) }
                                                job1.await()
                                                withContext(Dispatchers.Main) {
                                                    onResume()
                                                    popupWindow.dismiss()
                                                }
                                            }
                                        }
                                        .setNegativeButton("取消") { dialog, which ->
                                            // 点击取消按钮，不执行任何操作
                                        }
                                        .create()
                                    // 显示对话框
                                    alertDialog.show()
                                    true
                                }
                                // 添加更多菜单项的处理逻辑
                                else -> false
                            }
                        }
                        popupMenu.show() // 显示操作菜单
                    }
                }
            }
        }

        //绘制关系图
        createGraph(userSuperTextView,connectionsList, relativeHumanLayout, relativeLineLayout)
    }

    suspend fun deleteRelation(personId:Int):Boolean{
        val jsonString = MySQLConnection.fetchWebpageContent("DeleteRelation",personId.toString(),"")
        try {
            // 解析 JSON 字符串为 JSON 对象
            val jsonObject = JSONObject(jsonString)
        }catch(e: JSONException){
            // 如果转换失败，则处理异常，例如输出错误信息或者提示用户输入有效的 JSON 字符串
            withContext(Dispatchers.Main) {
                Toast.makeText(ConnectionsManagementApplication.context, "网络错误", Toast.LENGTH_SHORT).show()
            }
            return false
        }
        return withContext(Dispatchers.Main) {
            //相应结果为success
            if(JSONObject(jsonString).getString("result")=="success"){
                ConnectionsManagementApplication.IsRelationsChanged=true
                Toast.makeText(ConnectionsManagementApplication.context, "删除成功", Toast.LENGTH_SHORT).show()
                return@withContext true
            }else{
                Toast.makeText(ConnectionsManagementApplication.context, "删除失败", Toast.LENGTH_SHORT).show()
                return@withContext false
            }
        }
    }

    //人脉关系图绘制 备注：目前已实现人物围绕中心绕圈分布，重叠时圆圈自动外扩，展现一圈圈的人脉关系图
    private fun createGraph(
        centerSuperTextView:MySuperTextView,
        mySuperTextViewList: ArrayList<MySuperTextView>,
        relativeHumanLayout: RelativeLayout,
        relativeLineLayout: RelativeLayout
    ) {
        val baseRadius = 300 //基础半径
        val centerView = centerSuperTextView //中心人物 即：用户自身
        //每次刷新时清空关系图
        relativeHumanLayout.removeAllViews()
        relativeLineLayout.removeAllViews()

        //将中心人物放置关系图中心
        val centerViewParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        centerViewParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        relativeHumanLayout.addView(centerView, centerViewParams)

        //待中心视图布置完成后，添加其余人物视图
        centerView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                //获取中心人物视图的位置
                val centerX = centerView.x
                val centerY = centerView.y

                //视图重叠最大间距
                val viewSpacing = sqrt(
                    centerView.width.toDouble().pow(2.0) + centerView.height.toDouble().pow(2.0)
                )

                val finishedViewGroup: ArrayList<MySuperTextView> = arrayListOf() //本圈视图队列
                //绘制其余人物视图
                refreshAll(
                    mySuperTextViewList,
                    finishedViewGroup,
                    centerX,
                    centerY,
                    baseRadius,
                    relativeHumanLayout,
                    relativeLineLayout,
                    viewSpacing,
                    0
                )
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
        val line = View(activity)
        line.setBackgroundColor(Color.BLACK)  // 设置线条颜色

        // 计算线条的位置和大小
        val width =
            sqrt((startX - endX).toDouble().pow(2.0) + (startY - endY).toDouble().pow(2.0)).toInt()
        val height = 5
        val angle = atan2((endY - startY).toDouble(), (endX - startX).toDouble()) * 180 / Math.PI
        line.layoutParams = RelativeLayout.LayoutParams(width, height)

        // 设置线条的位置和旋转角度
        line.x = (startX + endX) / 2 - width / 2
        line.y = (startY + endY) / 2 - height / 2
        line.rotation = angle.toFloat()

        // 将线条添加到布局中
        layout.addView(line)
    }

    //判断两视图是否重叠
    fun viewsOverlap(view1: View, view2: View, viewSpacing: Double): Boolean {
        val viewDistance =
            sqrt((view1.x - view2.x).toDouble().pow(2.0) + (view1.y - view2.y).toDouble().pow(2.0))
        return viewDistance < viewSpacing
    }

    fun viewsOverlap(viewX: Int, viewY: Int, compareView: View, viewSpacing: Double): Boolean {
        val viewDistance = sqrt(
            (viewX - compareView.x).toDouble().pow(2.0) + (viewY - compareView.y).toDouble()
                .pow(2.0)
        )
        return viewDistance < viewSpacing
    }

    //判断视图与视图组是否重叠
    fun viewsGroupOverlap(
        newView: View,
        currentViewGroup: ArrayList<MySuperTextView>,
        viewSpacing: Double
    ): Boolean {
        var result = false
        for (tempView in currentViewGroup) {
            if (viewsOverlap(newView, tempView, viewSpacing)) {
                result = true
            }
        }
        return result
    }

    fun viewsGroupOverlap(
        newViewX: Int,
        newViewY: Int,
        currentViewGroup: ArrayList<MySuperTextView>,
        viewSpacing: Double
    ): Boolean {
        var result = false
        for (tempView in currentViewGroup) {
            if (viewsOverlap(newViewX, newViewY, tempView, viewSpacing)) {
                result = true
            }
        }
        return result
    }

    //将视图放入布局
    fun arrangeView(newView: View, X: Int, Y: Int, relativeLayout: RelativeLayout) {
        val viewParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        viewParams.leftMargin = X
        viewParams.topMargin = Y
        relativeLayout.addView(newView, viewParams)
    }

    //刷新除中心外的所有视图 备注：需要在中心视图布置完成后使用
    fun refreshAll(
        mySuperTextViewList: ArrayList<MySuperTextView>,
        finishedViewGroup: ArrayList<MySuperTextView>,
        centerX: Float,
        centerY: Float,
        currentRadius: Int,
        humanLayout: RelativeLayout,
        lineLayout: RelativeLayout,
        viewSpacing: Double,
        location: Int
    ) {
        //判断当前布置视图进度
        if (location + 1 > mySuperTextViewList.size) {
            //当完成所有视图的布置后，进行视图与中心的连线
            for (view in mySuperTextViewList) {
                drawLineBetweenViews(lineLayout, humanLayout.getChildAt(0), view)
            }
            return
        } else {
            //视图布置进行中，开始新视图布置
            val newView = mySuperTextViewList[location] //新视图
            finishedViewGroup.add(newView)  //加入本圈视图队列
            val currentViewSize = finishedViewGroup.size //本圈视图数量
            var currentAngle = 0.0 //当前角度
            var angleIncrement = 2 * Math.PI / currentViewSize //本圈角度增加量
            var curRadius = currentRadius //当前半径
            val radiusIncrement = 100 //扩圈时的半径增加量

            //调整本圈其余视图位置
            for (i in humanLayout.childCount - currentViewSize + 1 until humanLayout.childCount) {
                humanLayout.getChildAt(i).x = (centerX + curRadius * cos(currentAngle)).toFloat()
                humanLayout.getChildAt(i).y = (centerY - curRadius * sin(currentAngle)).toFloat()
                currentAngle += angleIncrement
            }

            //布置本圈新视图
            var x = (centerX + curRadius * cos(currentAngle)).toInt() //x坐标
            var y = (centerY - curRadius * sin(currentAngle)).toInt() //y坐标
            arrangeView(newView, x, y, humanLayout)

            //设置本圈恢复标志
            var restoreFlag = false

            //布置新视图后判断是否重叠
            while (viewsGroupOverlap(x, y, ArrayList(mySuperTextViewList.take(location)), viewSpacing)) {
                //重叠后恢复本圈其余视图至正确位置
                if (!restoreFlag) {
                    currentAngle = 0.0
                    angleIncrement = 2 * Math.PI / (currentViewSize - 1)
                    for (i in humanLayout.childCount - currentViewSize until humanLayout.childCount - 1) {
                        humanLayout.getChildAt(i).x =
                            (centerX + curRadius * cos(currentAngle)).toFloat()
                        humanLayout.getChildAt(i).y =
                            (centerY - curRadius * sin(currentAngle)).toFloat()
                        currentAngle += angleIncrement
                    }
                    restoreFlag = true //本圈已恢复
                }

                //移除新视图，清空本圈视图队列
                finishedViewGroup.clear()
                humanLayout.removeView(newView)

                //开启新圈
                finishedViewGroup.add(newView) //新视图为新圈第一个视图
                curRadius += radiusIncrement //新圈半径
                x = (centerX + curRadius * cos(0.0)).toInt() //x坐标
                y = (centerY - curRadius * sin(0.0)).toInt() //y坐标
                arrangeView(newView, x, y, humanLayout) //布置新视图
            }

            //待当前视图布局完成后，进行下一个视图的布局
            newView.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    newView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    refreshAll(
                        mySuperTextViewList,
                        finishedViewGroup,
                        centerX,
                        centerY,
                        curRadius,
                        humanLayout,
                        lineLayout,
                        viewSpacing,
                        location + 1
                    )
                    return
                }
            })
        }
    }
}