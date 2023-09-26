package com.example.connectionsmanagement.ConnectionsMap
//为配合drawer设置的Fragment，具体实现在ResultActivity
import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.contentValuesOf
import com.example.connectionsmanagement.R
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

 class DrawerFragment : Fragment() {
    // TODO: Rename and change types of parameters

    //展示关系选中变量
    var selected_relation_friend:Boolean=true
    var selected_relation_family:Boolean=true
    var selected_relation_classmate:Boolean=true
    var selected_relation_other:Boolean=true
    lateinit var thisView:View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //关系选择发生变动后刷新界面
        thisView = inflater.inflate(R.layout.drawer_fragment, container, false)
        thisView.findViewById<CheckBox>(R.id.checkBoxOfFriend).setOnCheckedChangeListener { buttonView, isChecked ->
            // 当 CheckBox 的选中状态发生变化时触发的操作
            selected_relation_friend = isChecked
            refresh()
        }
        thisView.findViewById<CheckBox>(R.id.checkBoxOfFamily).setOnCheckedChangeListener { buttonView, isChecked ->
            // 当 CheckBox 的选中状态发生变化时触发的操作
            selected_relation_family = isChecked
            refresh()
        }
        thisView.findViewById<CheckBox>(R.id.checkBoxOfClassmate).setOnCheckedChangeListener { buttonView, isChecked ->
            // 当 CheckBox 的选中状态发生变化时触发的操作
            selected_relation_classmate = isChecked
            refresh()
        }
        thisView.findViewById<CheckBox>(R.id.checkBoxOfOther).setOnCheckedChangeListener { buttonView, isChecked ->
            // 当 CheckBox 的选中状态发生变化时触发的操作
            selected_relation_other = isChecked
            refresh()
        }
        return thisView
    }

    companion object {
        // TODO: Rename and change types and number of parameters

    }


    //刷新人脉图谱
    @SuppressLint("Range")
    fun refresh() {
        //连接数据库
        val dbHelper = ConnectionsDatabaseHelper(ConnectionsManagementApplication.context, 1)
        val db = dbHelper.writableDatabase
        val relativeHumanLayout = thisView.findViewById<RelativeLayout>(R.id.ConnectionsHumanMap)
        val relativeLineLayout = thisView.findViewById<RelativeLayout>(R.id.ConnectionsLineMap)
        val connectionsList: ArrayList<MySuperTextView> = arrayListOf()
        lateinit var userSuperTextView:MySuperTextView

        //查询用户信息并创建视图
        val userCursor = db.query("UserInformation", null, "userId=?", arrayOf("${ConnectionsManagementApplication.NowUserId}"), null, null, null)
        if (userCursor.moveToFirst()) {
            val name = userCursor.getString(userCursor.getColumnIndex("name"))
            //从数据库取出Bitmap数据
            val imageByteArray = userCursor.getBlob(userCursor.getColumnIndex("image_data"))
            val imageBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
            //创建人物视图
            userSuperTextView = MySuperTextView(0,imageBitmap, name)
        }
        userCursor.close()

        //查询用户的人物关系（Connections表与Person表联合查询）
        val table1="Connections"//查询表1
        val table2="Person"//查询表2
        val joinCondition = "Connections.personId = Person.personId" // 两表的连接条件
        val valuesToMatch:ArrayList<String> = arrayListOf()  // 选中关系的匹配集合
        if(selected_relation_friend) {valuesToMatch.add("朋友")}
        if(selected_relation_family) {valuesToMatch.add("亲人")}
        if(selected_relation_classmate) {valuesToMatch.add("同学")}
        if(selected_relation_other) {valuesToMatch.add("其他")}
        //以用户ID、关系为查询条件
        val selection = "userId = ? and relationship IN (${valuesToMatch.joinToString(", ") { "?" }})" // 生成类似 "your_column IN (?, ?, ?)" 的条件
        val selectionArgs = arrayOf("${ConnectionsManagementApplication.NowUserId}")+ valuesToMatch.toTypedArray() // 使用集合中的值作为参数
        val cursor = db.query("$table1 INNER JOIN $table2 ON $joinCondition", null, selection, selectionArgs, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                //查询到的人物信息
                val id = cursor.getInt(cursor.getColumnIndex("personId"))
                val name = cursor.getString(cursor.getColumnIndex("name"))
                val gender=cursor.getString(cursor.getColumnIndexOrThrow("gender"))
                val phone_number=cursor.getString(cursor.getColumnIndexOrThrow("phone_number"))
                val email=cursor.getString(cursor.getColumnIndexOrThrow("email"))
                val notes=cursor.getString(cursor.getColumnIndexOrThrow("notes"))
                val imageByteArray = cursor.getBlob(cursor.getColumnIndex("image_data"))
                val imageBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
                //创建人物视图
                val mySuperTextView = MySuperTextView(id,imageBitmap, name)
                //将视图存入关系队列
                connectionsList.add(mySuperTextView)
                //设置人物详情弹窗
                mySuperTextView.setOnClickListener {
                    val layoutInflater = LayoutInflater.from(ConnectionsManagementApplication.context)
                    val popupLayout = layoutInflater.inflate(R.layout.pop_human_detail, null)
                    //获取浏览、编辑状态的控件
                    val humanImage = popupLayout.findViewById<CircleImageView>(R.id.popImage)
                    val humanName = popupLayout.findViewById<TextView>(R.id.popNameText)
                    val humanGender = popupLayout.findViewById<TextView>(R.id.popGenderText)
                    val humanPhone = popupLayout.findViewById<TextView>(R.id.popPhoneText)
                    val humanEmail = popupLayout.findViewById<TextView>(R.id.popEmailText)
                    val humanNotes = popupLayout.findViewById<TextView>(R.id.popNotesText)

                    val humanName_edit = popupLayout.findViewById<EditText>(R.id.popNameEditText)
                    val humanGender_edit = popupLayout.findViewById<EditText>(R.id.popGenderEditText)
                    val humanPhone_edit = popupLayout.findViewById<EditText>(R.id.popPhoneEditText)
                    val humanEmail_edit = popupLayout.findViewById<EditText>(R.id.popEmailEditText)
                    val humanNotes_edit = popupLayout.findViewById<EditText>(R.id.popNotesEditText)
                    val yes_to_adjust_button=popupLayout.findViewById<Button>(R.id.YesToAdjust)
                    val no_to_adjust_button=popupLayout.findViewById<Button>(R.id.NoToAdjust)

                    //将查询到的人物信息显示出来
                    humanImage.setImageBitmap(imageBitmap)
                    humanName.text=name
                    humanGender.text=gender
                    humanPhone.text=phone_number
                    humanEmail.text=email
                    humanNotes.text=notes

                    // 创建人物详情弹窗
                    val popupWindow = PopupWindow(popupLayout, dpToPx(200).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT, true)
                    // 设置点击外部区域关闭 PopupWindow
                    popupWindow.isOutsideTouchable = false
                    popupWindow.showAtLocation(thisView, Gravity.CENTER, 0, 0)
                    // 调整内容视图的位置以使其居中
                    val contentView = popupWindow.contentView
                    contentView.translationX = -contentView.width / 2f
                    contentView.translationY = -contentView.height / 2f

                    //设置弹窗右上角操作菜单
                    popupLayout.findViewById<Button>(R.id.popOperationButton).setOnClickListener {
                        val popupMenu = PopupMenu(this.requireContext(), it) // 创建PopupMenu，传入上下文和关联的视图
                        popupMenu.menuInflater.inflate(R.menu.adjust_or_delete_person, popupMenu.menu) // 加载菜单资源
                        popupMenu.setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                // 处理修改的点击事件
                                R.id.menu_adjust -> {
                                    //切换显示控件（转化为可编辑控件）
                                    humanName.visibility=View.GONE
                                    humanGender.visibility=View.GONE
                                    humanPhone.visibility=View.GONE
                                    humanEmail.visibility=View.GONE
                                    humanNotes.visibility=View.GONE
                                    humanName_edit.visibility=View.VISIBLE
                                    humanGender_edit.visibility=View.VISIBLE
                                    humanPhone_edit.visibility=View.VISIBLE
                                    humanEmail_edit.visibility=View.VISIBLE
                                    humanNotes_edit.visibility=View.VISIBLE
                                    humanName_edit.setText(humanName.text)
                                    humanGender_edit.setText(humanGender.text)
                                    humanPhone_edit.setText(humanPhone.text)
                                    humanEmail_edit.setText(humanEmail.text)
                                    humanNotes_edit.setText(humanNotes.text)
                                    yes_to_adjust_button.visibility=View.VISIBLE
                                    no_to_adjust_button.visibility=View.VISIBLE
                                    //确定修改人物信息
                                    yes_to_adjust_button.setOnClickListener {
                                        db.update("Person", contentValuesOf(
                                            "name" to humanName_edit.text.toString(),
                                            "gender" to humanGender_edit.text.toString(),
                                            "phone_number" to humanPhone_edit.text.toString(),
                                            "email" to humanEmail_edit.text.toString(),
                                            "notes" to humanNotes_edit.text.toString(),),"personId=?", arrayOf("$id"))
                                        refresh()//刷新界面
                                        popupWindow.dismiss()//关闭弹窗
                                    }
                                    no_to_adjust_button.setOnClickListener { popupWindow.dismiss() }
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
                                            db.delete("Person","personId=?", arrayOf("$id"))
                                            refresh()
                                            popupWindow.dismiss()
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
            } while (cursor.moveToNext())
        }
        //绘制关系图
        createGraph(userSuperTextView,connectionsList, relativeHumanLayout, relativeLineLayout)
        cursor.close()
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