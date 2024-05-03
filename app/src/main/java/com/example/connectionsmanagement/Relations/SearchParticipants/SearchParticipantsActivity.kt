package com.example.connectionsmanagement.Relations.SearchParticipants

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.connectionsmanagement.Communications.AddCommunicationActivity
import com.example.connectionsmanagement.Communications.ShowCommunicationDetailActivity
import com.example.connectionsmanagement.Tools.ConnectionsManagementApplication
import com.example.connectionsmanagement.Tools.ImageDownloader.getBitmapFromLocalPath
import com.example.connectionsmanagement.R
import com.google.gson.Gson


class SearchParticipantsActivity : AppCompatActivity() ,
    SearchResultAdapter.OnCheckBoxClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchResultAdapter
    val searchResults = arrayListOf<SearchPerson>()//搜索结果队列
    lateinit var sender:String
    var selectParticipants=ArrayList<SelectedParticipant>()
    private var showSelectedParticipants:TextView?=null
    var added=false//是否已添加的标记


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_participants)
        //获取选择参与者的记录
        sender=intent.getStringExtra("sender")!!
        selectParticipants= ArrayList(Gson().fromJson(intent.getStringExtra("nowParticipantsJson"),Array<SelectedParticipant>::class.java).toList().toMutableList())

        //退出
        findViewById<Button>(R.id.backParticipant_Button).setOnClickListener {
            finish()
        }

        //确定选择参与者
        findViewById<Button>(R.id.sureParticipant_Button).setOnClickListener {
            lateinit var intent:Intent
            if(sender=="AddCommunicationActivity"){
                intent = Intent(this, AddCommunicationActivity::class.java)
            }else if(sender=="ShowCommunicationDetailActivity"){
                intent = Intent(this, ShowCommunicationDetailActivity::class.java)
            }
            intent.putExtra("selectedParticipants", Gson().toJson(selectParticipants))
            startActivity(intent)
        }

        showSelectedParticipants=findViewById<TextView>(R.id.showSelectedParticipants_TextView)

        //获取RecyclerView
        recyclerView = findViewById<RecyclerView>(R.id.SearchParticipants_RecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)


        adapter = SearchResultAdapter(searchResults,this)
        recyclerView.adapter = adapter

        searchResults.clear()//清空搜索结果队列

        ConnectionsManagementApplication.NowRelations.forEach {
            selectParticipants.forEach {selectedParticipant ->
                if(it.personId==selectedParticipant.personId){
                    searchResults.add(SearchPerson(it.personId,getBitmapFromLocalPath(it.image_path),it.name,true))
                    added=true
                }
            }
            if(!added){
                searchResults.add(SearchPerson(it.personId,getBitmapFromLocalPath(it.image_path),it.name,false))
            }
            added=false
        }
        adapter.notifyDataSetChanged()//通知数据变化

        //监听搜索框
        findViewById<EditText>(R.id.SearchParticipant_EditText).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                // 在文本改变之前执行的操作，用于限制输入内容
                // charSequence 包含了当前文本内容和即将输入的字符
            }
            //实时搜索
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                searchResults.clear()//清空搜索结果队列
                // 在文本改变过程中实时触发的操作,charSequence包含了当前文本内容和正在输入的字符
                val input=charSequence.toString()//获取输入内容
                ConnectionsManagementApplication.NowRelations.forEach {
                    if(it.name.contains(input)){
                        selectParticipants.forEach {selectedParticipant ->
                            if(it.personId==selectedParticipant.personId){
                                searchResults.add(SearchPerson(it.personId,getBitmapFromLocalPath(it.image_path),it.name,true))
                                added=true
                            }
                        }
                        if(!added){
                            searchResults.add(SearchPerson(it.personId,getBitmapFromLocalPath(it.image_path),it.name,false))
                        }
                        added=false
                    }
                }
                adapter.notifyDataSetChanged()//通知数据变化
            }

            override fun afterTextChanged(editable: Editable) {
                // 在文本改变完成后执行的操作
                // editable 包含了最终的文本内容
            }
        })
    }

    // 实现回调接口的方法，在该方法中更新数据
    override fun onCheckBoxClick(position: Int, isChecked: Boolean, personId:Int, name: String) {
        // 根据位置和选中状态更新数据
        if(isChecked){
            selectParticipants.forEach {
                if(it.personId==personId){
                    added=true
                }
            }
            if(!added){
                selectParticipants.add(SelectedParticipant(personId,name))
            }
            added=false
        }else{
            selectParticipants.removeIf { it.personId == personId }
        }

        val stringBuilder = StringBuilder("已选择：")
        var first=true
        selectParticipants.forEach {
            if(first){
                stringBuilder.append(it.name)
                first=false
            }
            else
                stringBuilder.append("、${it.name}")
        }
        showSelectedParticipants?.text=stringBuilder.toString()
    }

    // 修改 RecyclerView 中特定项的 CheckBox 属性
    private fun modifyCheckBoxInRecyclerView(position: Int, isChecked: Boolean) {
        // 获取 RecyclerView 的布局管理器
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        // 获取指定位置的 ViewHolder
        val viewHolder = layoutManager.findViewByPosition(position)?.let { recyclerView.getChildViewHolder(it) }
        // 如果 ViewHolder 不为 null，并且是 MyAdapter.ViewHolder 类型
        if (viewHolder is SearchResultAdapter.ViewHolder) {
            // 设置 CheckBox 的选中状态
            viewHolder.checkBox.isChecked = isChecked
        }
    }
}