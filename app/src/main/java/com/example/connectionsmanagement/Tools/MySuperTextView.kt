package com.example.connectionsmanagement.Tools

import android.graphics.Bitmap
import android.view.Gravity.CENTER_HORIZONTAL
import android.widget.LinearLayout
import com.coorchice.library.SuperTextView

//自定义SuperTextView，包含两个SuperTextView，一上一下，上面图片，下面文本
//备注:SuperTextView似乎存在着动态设置drawable时图片不可见的问题，仅当设置为背景时有效
//备注:考虑到SuperTextView其余的强大功能，为方便后续设计，本项目仍希望采用它，特自定义此控件
 class MySuperTextView(thisId:Int,imageBitmap: Bitmap,name:String):LinearLayout(
    ConnectionsManagementApplication.context,null) {
    private lateinit var superTextViewTop:SuperTextView
    private lateinit var superTextViewButton:SuperTextView
    val personId:Int=thisId

    init {
        //设置头像
        layoutParams=LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        orientation= VERTICAL
        superTextViewTop=SuperTextView(context).apply {
            gravity= CENTER_HORIZONTAL
            isShowState=true
            setDrawable(imageBitmap)
            isDrawableAsBackground=true
            val diyParams= LayoutParams(200, 200)
            layoutParams=diyParams
            corner=300f
        }
        //设置姓名
        superTextViewButton=SuperTextView(context).apply {
            text=name
            gravity= CENTER_HORIZONTAL
            val diyParams= LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            layoutParams=diyParams
        }
        addView(superTextViewTop)
        addView(superTextViewButton)
    }

}