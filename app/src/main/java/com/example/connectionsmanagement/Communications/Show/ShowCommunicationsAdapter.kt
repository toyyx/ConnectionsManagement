package com.example.connectionsmanagement.Communications.Show

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
import com.example.connectionsmanagement.Communications.ShowCommunicationDetailActivity
import com.example.connectionsmanagement.Tools.ConnectionsManagementApplication
import com.example.connectionsmanagement.MysqlServer.MySQLConnection
import com.example.connectionsmanagement.R
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

//交际展示的适配器
class ShowCommunicationsAdapter(private val context: Context, private val shows: List<ShowCommunication>, private val listener: onDeleteButtonClickListener) : RecyclerView.Adapter<ShowCommunicationsAdapter.ViewHolder>()  {
    lateinit var view: View

    //获取视图控件
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val showTime1 = itemView.findViewById<TextView>(R.id.showCommunicationTime_TextView1)
        val showTime2 = itemView.findViewById<TextView>(R.id.showCommunicationTime_TextView2)
        val showTitle = itemView.findViewById<TextView>(R.id.showCommunicationTitle_TextView)
        val showAddress = itemView.findViewById<TextView>(R.id.showCommunicationAddress_TextView)
        val showContent= itemView.findViewById<LinearLayout>(R.id.contentCommunication_LinearLayout)
        val horizontalScrollView = itemView.findViewById<HorizontalScrollView>(R.id.showCommunication_HorizontalScrollView)
        val deleteCommunication=itemView.findViewById<Button>(R.id.deleteCommunication_Button)
        val detailCommunication=itemView.findViewById<ImageButton>(R.id.toDetailCommunication_ImageButton)
    }

    //设置单项的layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        view = LayoutInflater.from(parent.context).inflate(R.layout.show_communication_item, parent, false)
        return ViewHolder(view)
    }

    //控件设置
    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val show = shows[position]
        holder.showTime1.text=show.startTime
        holder.showTime2.text=show.finishTime
        holder.showTitle.text="主题:${show.title}"
        holder.showAddress.text="地址:${show.address}"


        // 获取屏幕宽度，设置控件
        val screenWidth = context.resources.displayMetrics.widthPixels
        val layoutParams = holder.showContent.layoutParams
        layoutParams.width = screenWidth
        holder.showContent.layoutParams = layoutParams

        //设置滑动块功能
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

        //删除交际按钮
        holder.deleteCommunication.setOnClickListener {
            //创建对话框
            val alertDialog = AlertDialog.Builder(context)
                .setTitle("确认删除")
                .setMessage("确认删除这次交际吗？")
                .setCancelable(false)
                .setPositiveButton("确认") { dialog, which ->
                    // 点击确认按钮，执行删除操作
                    GlobalScope.launch {
                        val job = async { MySQLConnection.fetchWebpageContent("DeleteCommunication",show.eventId,"") }
                        // 等待所有协程执行完毕，并获取结果
                        val jsonString:String = job.await()
                        println("Server Response: $jsonString")
                        // 解析 JSON 字符串为 JSON 对象
                        val jsonObject = JSONObject(jsonString)
                        withContext(Dispatchers.Main) {
                            //相应结果为success
                            if(jsonObject.getString("result")=="success"){
                                Toast.makeText(context, "删除交际成功", Toast.LENGTH_SHORT).show()
                                ConnectionsManagementApplication.IsCommunicationsChanged =true
                                listener.onDeleteButtonClick()
                            }else{
                                Toast.makeText(context, "删除交际失败", Toast.LENGTH_SHORT).show()
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

        //查看交际详情
        holder.detailCommunication.setOnClickListener {
            //进入详细交际页面
            val intent = Intent(context, ShowCommunicationDetailActivity::class.java)
            intent.putExtra("thisCommunication", Gson().toJson(show))
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return shows.size
    }

    //删除按钮的功能接口，在ShowCommunicationActivity中实现
    interface onDeleteButtonClickListener {
        fun onDeleteButtonClick()
    }


}