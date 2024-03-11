package com.example.connectionsmanagement.ConnectionsMap

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.connectionsmanagement.MysqlServer.MySQLConnection
import com.example.connectionsmanagement.MysqlServer.Relation
import com.example.connectionsmanagement.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListFragment : Fragment() {
    // TODO: Rename and change types of parameters
    lateinit var thisView:View
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CardPersonAdapter
    var cardResults = arrayListOf<CardPerson>()//搜索结果队列
    lateinit var newCardResults: ArrayList<CardPerson>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
        //刷新关系列表
        if(ConnectionsManagementApplication.IsRelationsChanged){
            Toast.makeText(ConnectionsManagementApplication.context, "关系已变化", Toast.LENGTH_SHORT).show()
            ConnectionsManagementApplication.IsRelationsChanged=false
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
        thisView = inflater.inflate(R.layout.fragment_list, container, false)
        //获取RecyclerView
        recyclerView = thisView.findViewById<RecyclerView>(R.id.cardRecyclerView_ListFragment)
        recyclerView.layoutManager = LinearLayoutManager(ConnectionsManagementApplication.context)

        adapter = CardPersonAdapter(cardResults)
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
        return thisView
    }

    private suspend fun RefreshcardResults(): ArrayList<CardPerson>{
        ImageDownloader.RefreshRelations()
        var cardResults= arrayListOf<CardPerson>()
        //下载图片
        ConnectionsManagementApplication.NowRelations.forEach {
            //ImageDownloader.downloadImage(ConnectionsManagementApplication.context, it.image_path)
            val bitmap= BitmapFactory.decodeFile(ConnectionsManagementApplication.context.externalCacheDir.toString()+ImageDownloader.getSpecialFromString(it.image_path, "data_image/"))
            // 判断是否成功解码
            if (bitmap == null) {
                // 解码失败
                println("Failed to decode bitmap from file")
            } else {
                // 解码成功
                println("Bitmap decoded successfully from file")
            }
            val myCardPerson=CardPerson(bitmap,it.name,it.relationship,it.phone_number,it.notes)
            cardResults.add(myCardPerson)//加入搜索结果队列
        }
        return cardResults
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
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}