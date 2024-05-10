package com.example.connectionsmanagement.Communications.Show

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.connectionsmanagement.Communications.AddCommunicationActivity
import com.example.connectionsmanagement.Tools.ConnectionsManagementApplication
import com.example.connectionsmanagement.Tools.Tools.RefreshCommunications
import com.example.connectionsmanagement.Relations.SearchParticipants.SelectedParticipant
import com.example.connectionsmanagement.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//展示当天交际activity
class ShowCommunicationActivity : AppCompatActivity() ,
    ShowCommunicationsAdapter.onDeleteButtonClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ShowCommunicationsAdapter
    private  var showCommunications=ArrayList<ShowCommunication>()
    lateinit var noCommunicationTextView:TextView
    lateinit var nowDateTextView:TextView
    lateinit var nowDate:String //当天日期

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_communication)

        //设置初始信息
        nowDate=intent.getStringExtra("date")!!
        noCommunicationTextView=findViewById<TextView>(R.id.showNoCommunications_TextView)
        nowDateTextView=findViewById<TextView>(R.id.showCommunicationDate_TextView)
        nowDateTextView.text=nowDate

        //获取RecyclerView
        recyclerView = findViewById<RecyclerView>(R.id.ShowCommunication_RecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        //设置adapter
        adapter = ShowCommunicationsAdapter(this,showCommunications,this)
        recyclerView.adapter = adapter

        //刷新展示当日交际
        refreshShowCommunications()

        //增加交际按钮
        findViewById<Button>(R.id.addCommunication_Button).setOnClickListener {
            //进入增加交际页面
            val intent = Intent(this, AddCommunicationActivity::class.java)
            intent.putExtra("date",nowDate)
            startActivity(intent)
        }

        //返回按钮
        findViewById<Button>(R.id.backShowCommunication_Button).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
//        Toast.makeText(ConnectionsManagementApplication.context, "showCommunication onResume", Toast.LENGTH_SHORT).show()//调试使用
        //当交际变化时，更新本地数据
        if(ConnectionsManagementApplication.IsCommunicationsChanged){
            ConnectionsManagementApplication.IsCommunicationsChanged =false
            GlobalScope.launch {
                val job=async {RefreshCommunications()}
                job.await()
                withContext(Dispatchers.Main) {
                    refreshShowCommunications()
                }
            }

        }
    }

    //刷新展示当日交际
    fun refreshShowCommunications(){
        //清空当前数据
        showCommunications.clear()

        //选出所有交际中开始日期为当天日期的交际
        ConnectionsManagementApplication.Communications.forEach {
            if(it.startTime.contains(nowDate!!)){
                showCommunications.add(ShowCommunication(it.eventId,it.startTime,it.finishTime,it.title,it.detail,it.address,personIdArraytoSelectedParticipantArray(it.participants)))
            }
        }
        //若当天没有交际，出现提示文本
        if(showCommunications.size==0){
            noCommunicationTextView.visibility= View.VISIBLE
        }else{//若当天存在交际，展示交际信息
            noCommunicationTextView.visibility= View.GONE
            adapter.notifyDataSetChanged()//通知数据变化
        }
    }

    //人物ID数组转化为选中人物数组
    fun personIdArraytoSelectedParticipantArray(personIdArray:ArrayList<String>):ArrayList<SelectedParticipant>{
        var SelectedParticipantArray=ArrayList<SelectedParticipant>()
        //从本地数据的人脉中匹配查找
        personIdArray.forEach {
            ConnectionsManagementApplication.NowRelations.forEach { relation ->
                if(it==relation.personId.toString()){
                    SelectedParticipantArray.add(SelectedParticipant(relation.personId,relation.name))
                }
            }
        }
        return SelectedParticipantArray
    }

    //删除交际按钮
    override fun onDeleteButtonClick() {
        onResume()
    }
}