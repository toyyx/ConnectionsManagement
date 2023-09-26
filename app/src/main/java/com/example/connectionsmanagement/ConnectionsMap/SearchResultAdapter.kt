package com.example.connectionsmanagement.ConnectionsMap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.connectionsmanagement.R
import de.hdodenhof.circleimageview.CircleImageView

//查询结果适配器
class SearchResultAdapter(private val results: List<SearchPerson>) :
    RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    //获取视图控件
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val resultImage = itemView.findViewById<CircleImageView>(R.id.searchImage)
        val resultName = itemView.findViewById<TextView>(R.id.searchName)
        val resultNotes = itemView.findViewById<TextView>(R.id.searchNotes)
    }

    //设置单项的layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_item, parent, false)
        return ViewHolder(view)
    }

    //控件设置
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = results[position]
        holder.resultImage.setImageBitmap(result.image)
        holder.resultName.text=result.name
        holder.resultNotes.text=result.notes
    }

    override fun getItemCount(): Int {
        return results.size
    }
}