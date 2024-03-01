package com.example.connectionsmanagement.ConnectionsMap

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.connectionsmanagement.R

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CardPersonAdapter
    val dbHelper = ConnectionsDatabaseHelper(ConnectionsManagementApplication.context, 1)
    val db = dbHelper.writableDatabase

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //获取RecyclerView
        recyclerView = findViewById(R.id.cardRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val cardResults = arrayListOf<CardPerson>()//搜索结果队列
        adapter = CardPersonAdapter(cardResults)
        recyclerView.adapter = adapter

        val table1="Connections"//表1
        val table2="Person"//表2
        val joinCondition = "Connections.personId = Person.personId" // 两表的连接条件
        val cursor = db.query("$table1 INNER JOIN $table2 ON $joinCondition", null, "userId=?", arrayOf("${ConnectionsManagementApplication.NowUserId}"), null, null, null)
        if (cursor.moveToFirst()) {
            do{
                var imageByteArray = cursor.getBlob(cursor.getColumnIndex("image_data"))
                var imageBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
                var name = cursor.getString(cursor.getColumnIndex("name"))
                var relation = cursor.getString(cursor.getColumnIndex("relationship"))
                var phoneNumber = cursor.getString(cursor.getColumnIndex("phone_number"))
                var notes = cursor.getString(cursor.getColumnIndex("notes"))
                val myCardPerson=CardPerson(imageBitmap,name,relation,phoneNumber,notes)
                cardResults.add(myCardPerson)//加入搜索结果队列
            }while (cursor.moveToNext())
        }
        adapter.notifyDataSetChanged()//通知数据变化
    }
}