package com.example.connectionsmanagement.Communications.Show

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.connectionsmanagement.Communications.AddCommunicationActivity
import com.example.connectionsmanagement.Tools.ConnectionsManagementApplication
import com.example.connectionsmanagement.Tools.ImageDownloader.RefreshCommunications
import com.example.connectionsmanagement.Relations.SearchParticipants.SelectedParticipant
import com.example.connectionsmanagement.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShowCommunicationActivity : AppCompatActivity() ,
    ShowCommunicationsAdapter.onDeleteButtonClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ShowCommunicationsAdapter
    private  var showCommunications=ArrayList<ShowCommunication>()
    lateinit var noCommunicationTextView:TextView
    lateinit var nowDateTextView:TextView
    lateinit var nowDate:String

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_communication)

        nowDate=intent.getStringExtra("date")!!
        noCommunicationTextView=findViewById<TextView>(R.id.showNoCommunications_TextView)
        nowDateTextView=findViewById<TextView>(R.id.showCommunicationDate_TextView)
        nowDateTextView.text=nowDate

        //获取RecyclerView
        recyclerView = findViewById<RecyclerView>(R.id.ShowCommunication_RecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ShowCommunicationsAdapter(this,showCommunications,this)
        recyclerView.adapter = adapter

        refreshShowCommunications()

        findViewById<Button>(R.id.addCommunication_Button).setOnClickListener {
            //进入主页面
            val intent = Intent(this, AddCommunicationActivity::class.java)
            intent.putExtra("date",nowDate)
            startActivity(intent)
        }

        findViewById<Button>(R.id.backShowCommunication_Button).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        Toast.makeText(ConnectionsManagementApplication.context, "showCommunication onResume", Toast.LENGTH_SHORT).show()
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
        showCommunications.clear()
        ConnectionsManagementApplication.Communications.forEach {
            if(it.startTime.contains(nowDate!!)){
                showCommunications.add(ShowCommunication(it.eventId,it.startTime,it.finishTime,it.title,it.detail,it.address,personIdArraytoSelectedParticipantArray(it.participants)))
            }
        }
        if(showCommunications.size==0){
            noCommunicationTextView.visibility= View.VISIBLE
        }else{
            noCommunicationTextView.visibility= View.GONE
            adapter.notifyDataSetChanged()//通知数据变化
        }
    }

    fun personIdArraytoSelectedParticipantArray(personIdArray:ArrayList<String>):ArrayList<SelectedParticipant>{
        var SelectedParticipantArray=ArrayList<SelectedParticipant>()
        personIdArray.forEach {
            ConnectionsManagementApplication.NowRelations.forEach { relation ->
                if(it==relation.personId.toString()){
                    SelectedParticipantArray.add(SelectedParticipant(relation.personId,relation.name))
                }
            }
        }
        return SelectedParticipantArray
    }

    override fun onDeleteButtonClick() {
        onResume()
    }
}