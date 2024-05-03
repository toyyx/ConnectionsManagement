package com.example.connectionsmanagement.ConnectionsMap.List

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.connectionsmanagement.ConnectionsMap.MainActivity
import com.example.connectionsmanagement.Relations.Relation
import com.example.connectionsmanagement.R
import com.example.connectionsmanagement.Tools.ConnectionsManagementApplication
import com.example.connectionsmanagement.Tools.ImageDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


/**
 * A simple [Fragment] subclass.
 * Use the [ListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListFragment : Fragment() , CardPersonAdapter.onDeleteButtonClickListener {
    // TODO: Rename and change types of parameters
    lateinit var thisView:View
    private lateinit var recyclerView: RecyclerView
    lateinit var adapter: CardPersonAdapter
    var cardResults = arrayListOf<Relation>()//搜索结果队列
    lateinit var newCardResults: ArrayList<Relation>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(
            ConnectionsManagementApplication.context, "List_Fragment_onCreate",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onResume() {
        super.onResume()
        Toast.makeText(
            ConnectionsManagementApplication.context, "List_Fragment_onResume:${ConnectionsManagementApplication.IsRelationsChanged_forList}",
            Toast.LENGTH_SHORT
        ).show()
        //刷新关系列表
        if(ConnectionsManagementApplication.IsRelationsChanged_forList){
            Toast.makeText(ConnectionsManagementApplication.context, "关系已变化", Toast.LENGTH_SHORT).show()
            ConnectionsManagementApplication.IsRelationsChanged_forList=false
            GlobalScope.launch {
                val job1 = async {
                    newCardResults  = RefreshcardResults()
                }
                job1.await()
                withContext(Dispatchers.Main) {
                    cardResults.clear() // 清空旧的 cardResults
                    cardResults.addAll(newCardResults) // 将新的 cardResults 添加到列表中
                    //Toast.makeText(ConnectionsManagementApplication.context, "人物名字：${cardResults[0].name}", Toast.LENGTH_SHORT).show()
                    adapter.notifyDataSetChanged()//通知数据变化
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Toast.makeText(
            ConnectionsManagementApplication.context, "List_Fragment_onCreateView",
            Toast.LENGTH_SHORT
        ).show()
        thisView = inflater.inflate(R.layout.fragment_list, container, false)
        //获取RecyclerView
        recyclerView = thisView.findViewById<RecyclerView>(R.id.cardRecyclerView_ListFragment)
        recyclerView.layoutManager = LinearLayoutManager(ConnectionsManagementApplication.context)

        adapter = CardPersonAdapter(requireContext(),cardResults,this)
        recyclerView.adapter = adapter

        //获取人际关系列表
        GlobalScope.launch {
            val job1 = async {
                newCardResults  = RefreshcardResults()
            }
            job1.await()
            withContext(Dispatchers.Main) {
                cardResults.clear() // 清空旧的 cardResults
                cardResults.addAll(newCardResults) // 将新的 cardResults 添加到列表中
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
                cardResults.clear()//清空搜索结果队列
                // 在文本改变过程中实时触发的操作,charSequence包含了当前文本内容和正在输入的字符
                val input=charSequence.toString()//获取输入内容
                ConnectionsManagementApplication.NowRelations.forEach {
                    if(it.name.contains(input)){
                        cardResults.add(it)//加入搜索结果队列
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

    suspend fun RefreshcardResults(): ArrayList<Relation>{
        ImageDownloader.RefreshRelations()
        return ConnectionsManagementApplication.NowRelations
    }

    override fun onDeleteButtonClick() {
        onResume()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ListFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}