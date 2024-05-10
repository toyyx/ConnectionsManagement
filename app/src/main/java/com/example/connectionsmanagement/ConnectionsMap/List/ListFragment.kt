package com.example.connectionsmanagement.ConnectionsMap.List

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.connectionsmanagement.Relations.Relation
import com.example.connectionsmanagement.R
import com.example.connectionsmanagement.Tools.ConnectionsManagementApplication
import com.example.connectionsmanagement.Tools.Tools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//人脉列表展示
class ListFragment : Fragment() , ListPersonAdapter.onDeleteButtonClickListener {
    lateinit var thisView:View
    private lateinit var recyclerView: RecyclerView
    lateinit var adapter: ListPersonAdapter
    var listResults = arrayListOf<Relation>()//搜索结果列表
    lateinit var newListResults: ArrayList<Relation> //新人物列表

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Toast.makeText(ConnectionsManagementApplication.context, "List_Fragment_onCreate", Toast.LENGTH_SHORT).show()//调试使用
    }

    override fun onResume() {
        super.onResume()
//        Toast.makeText(ConnectionsManagementApplication.context, "List_Fragment_onResume:${ConnectionsManagementApplication.IsRelationsChanged_forList}", Toast.LENGTH_SHORT).show()//调试使用
        //当人脉变化时，更新本地人脉数据
        if(ConnectionsManagementApplication.IsRelationsChanged_forList){
//            Toast.makeText(ConnectionsManagementApplication.context, "关系已变化", Toast.LENGTH_SHORT).show()
            ConnectionsManagementApplication.IsRelationsChanged_forList=false
            GlobalScope.launch {
                val job1 = async {
                    newListResults  = RefreshListResults()
                }
                job1.await()
                withContext(Dispatchers.Main) {
                    listResults.clear() // 清空旧的 cardResults
                    listResults.addAll(newListResults) // 将新的 cardResults 添加到列表中
                    adapter.notifyDataSetChanged()//通知数据变化
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        Toast.makeText(ConnectionsManagementApplication.context, "List_Fragment_onCreateView", Toast.LENGTH_SHORT).show()//调试使用
        thisView = inflater.inflate(R.layout.fragment_list, container, false)
        //获取RecyclerView
        recyclerView = thisView.findViewById<RecyclerView>(R.id.cardRecyclerView_ListFragment)
        recyclerView.layoutManager = LinearLayoutManager(ConnectionsManagementApplication.context)
        //列表人脉适配器
        adapter = ListPersonAdapter(requireContext(),listResults,this)
        recyclerView.adapter = adapter

        //获取人际关系列表
        GlobalScope.launch {
            val job1 = async {
                newListResults  = RefreshListResults()
            }
            job1.await()
            withContext(Dispatchers.Main) {
                listResults.clear() // 清空旧的 cardResults
                listResults.addAll(newListResults) // 将新的 cardResults 添加到列表中
                adapter.notifyDataSetChanged()//通知数据变化
            }
        }

        //监听搜索框
        thisView.findViewById<EditText>(R.id.SearchParticipant_List_EditText).addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                // 在文本改变之前执行的操作，用于限制输入内容
                // charSequence 包含了当前文本内容和即将输入的字符
            }
            //实时搜索
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                listResults.clear()//清空搜索结果队列
                // 在文本改变过程中实时触发的操作,charSequence包含了当前文本内容和正在输入的字符
                val input=charSequence.toString()//获取输入内容
                //从本地的人脉数据中匹配名字包含了搜索字符的人物
                ConnectionsManagementApplication.NowRelations.forEach {
                    if(it.name.contains(input)){
                        listResults.add(it)//加入搜索结果队列
                    }
                }
                adapter.notifyDataSetChanged()//通知数据变化
            }

            override fun afterTextChanged(editable: Editable) {
                // 在文本改变完成后执行的操作
                // editable 包含了最终的文本内容
            }
        })
        return thisView
    }

    //刷新人物列表
    suspend fun RefreshListResults(): ArrayList<Relation>{
        Tools.RefreshRelations()
        return ConnectionsManagementApplication.NowRelations
    }

    //删除人物接口实现
    override fun onDeleteButtonClick() {
        onResume()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ListFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}