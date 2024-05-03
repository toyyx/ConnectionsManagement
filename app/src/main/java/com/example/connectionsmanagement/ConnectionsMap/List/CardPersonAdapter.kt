package com.example.connectionsmanagement.ConnectionsMap.List

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.connectionsmanagement.MysqlServer.MySQLConnection
import com.example.connectionsmanagement.Relations.Relation
import com.example.connectionsmanagement.R
import com.example.connectionsmanagement.Relations.EditRelationActivity
import com.example.connectionsmanagement.Tools.ConnectionsManagementApplication
import com.example.connectionsmanagement.Tools.ImageDownloader
import com.google.gson.Gson
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class CardPersonAdapter(private val context: Context, private val results: List<Relation>, private val listener: onDeleteButtonClickListener) : RecyclerView.Adapter<CardPersonAdapter.ViewHolder>() {

    //获取视图控件
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardImage = itemView.findViewById<CircleImageView>(R.id.cardImage)
        val cardName = itemView.findViewById<TextView>(R.id.cardName)
        val cardRelation = itemView.findViewById<TextView>(R.id.cardRelation)
        val cardPhoneNumber=itemView.findViewById<TextView>(R.id.cardPhoneNumber)
        val cardNotes = itemView.findViewById<TextView>(R.id.cardNotes)

        val cardPerson_LinearLayout= itemView.findViewById<LinearLayout>(R.id.contentPerson_List_LinearLayout)
        val horizontalScrollView = itemView.findViewById<HorizontalScrollView>(R.id.cardPerson_HorizontalScrollView)
        val deletePerson=itemView.findViewById<Button>(R.id.deletePerson_List_Button)
        val detailPerson=itemView.findViewById<ImageButton>(R.id.toDetailPerson_List_ImageButton)
    }

    //设置单项的layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.main_card_human, parent, false)
        return ViewHolder(view)
    }

    //控件设置
    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = results[position]
        holder.cardImage.setImageBitmap(ImageDownloader.getBitmapFromLocalPath(result.image_path))
        holder.cardName.text=result.name
        holder.cardRelation.text=result.relationship
        holder.cardPhoneNumber.text=result.phone_number
        holder.cardNotes.text=result.notes

        // 获取屏幕宽度
        val screenWidth = context.resources.displayMetrics.widthPixels
        val layoutParams = holder.cardPerson_LinearLayout.layoutParams
        layoutParams.width = screenWidth
        holder.cardPerson_LinearLayout.layoutParams = layoutParams

        val horizontalScrollView=holder.horizontalScrollView
        horizontalScrollView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    // 手指抬起时，滑动停止
                    // 在这里执行滑动停止后的动作
                    val maxScrollX = horizontalScrollView.getChildAt(0).width - horizontalScrollView.width
                    val scrollX = horizontalScrollView.scrollX
                    val boundary = maxScrollX  / 2 // 中间位置的阈值
                    if (scrollX <= boundary) {
                        // 滑动到了中间左侧，自动调整至最左
                        horizontalScrollView.post{horizontalScrollView.smoothScrollTo(0, 0)}
                    } else if (scrollX > boundary) {
                        // 滑动到了中间右侧，自动调整至最右
                        horizontalScrollView.post{horizontalScrollView.smoothScrollTo(maxScrollX, 0)}
                    }
                }
            }
            false
        }

        holder.deletePerson.setOnClickListener {
            //创建对话框
            val alertDialog = AlertDialog.Builder(context)
                .setTitle("确认删除")
                .setMessage("确认删除这个人物吗？")
                .setCancelable(false)
                .setPositiveButton("确认") { dialog, which ->
                    // 点击确认按钮，执行删除操作
                    GlobalScope.launch {
                        val job = async { MySQLConnection.fetchWebpageContent("DeleteRelation",result.personId.toString(),"") }
                        // 等待所有协程执行完毕，并获取结果
                        val jsonString:String = job.await()
                        println("Server Response: $jsonString")
                        // 解析 JSON 字符串为 JSON 对象
                        val jsonObject = JSONObject(jsonString)
                        withContext(Dispatchers.Main) {
                            //相应结果为success
                            if(jsonObject.getString("result")=="success"){
                                Toast.makeText(context, "删除人物成功", Toast.LENGTH_SHORT).show()
                                ConnectionsManagementApplication.IsRelationsChanged_forList=true
                                ConnectionsManagementApplication.IsRelationsChanged_forDrawer=true
                                listener.onDeleteButtonClick()
                            }else{
                                Toast.makeText(context, "删除人物失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                .setNegativeButton("取消") { dialog, which ->
                    // 点击取消按钮，不执行任何操作
                }
                .create()
            // 显示对话框
            alertDialog.show()
        }

        holder.detailPerson.setOnClickListener {
            //进入主页面
            val intent = Intent(context, EditRelationActivity::class.java)
            intent.putExtra("thisRelation", Gson().toJson(result))
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return results.size
    }

    interface onDeleteButtonClickListener {
        fun onDeleteButtonClick()
    }
}