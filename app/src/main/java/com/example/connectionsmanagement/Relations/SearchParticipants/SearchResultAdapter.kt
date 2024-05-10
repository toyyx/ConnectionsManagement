package com.example.connectionsmanagement.Relations.SearchParticipants

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.connectionsmanagement.R
import de.hdodenhof.circleimageview.CircleImageView

//查询结果适配器
class SearchResultAdapter(private val results: List<SearchPerson>, private val listener: OnCheckBoxClickListener) :
    RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    //获取视图控件
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val resultImage = itemView.findViewById<CircleImageView>(R.id.searchImage)
        val resultName = itemView.findViewById<TextView>(R.id.searchName)
        val checkBox = itemView.findViewById<CheckBox>(R.id.searchSelect)

        //设置复选框的点击回调
        init {
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // 触发回调，并传递更新的数据
                    listener.onCheckBoxClick(position, isChecked,results[position].personId, results[position].name)
                }
            }
        }
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
        holder.checkBox.isChecked=result.selected
    }

    override fun getItemCount(): Int {
        return results.size
    }

    //复选框操作接口，在SearchParticipantsActivity中实现
    interface OnCheckBoxClickListener {
        fun onCheckBoxClick(position: Int, isChecked: Boolean, personId:Int, name: String)
    }
}


