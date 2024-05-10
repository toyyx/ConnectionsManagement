package com.example.connectionsmanagement.Face.FaceSearch_1_N

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.connectionsmanagement.R
import org.json.JSONArray

//人脸检测1:N 结果适配器
class FaceSearch_1_N_result_Adapter(private var user_list: JSONArray) : RecyclerView.Adapter<FaceSearch_1_N_result_Adapter.ViewHolder>() {

    //获取视图控件
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val index_IV = itemView.findViewById<TextView>(R.id.faceSearch_1_N_result_index_TextView)
        val info_TV = itemView.findViewById<TextView>(R.id.faceSearch_1_N_result_info_TextView)
        val score_TV = itemView.findViewById<TextView>(R.id.faceSearch_1_N_result_score_TextView)
    }

    //设置单项的layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.facesearch_1_n_result_item, parent, false)
        return ViewHolder(view)
    }

    //控件设置
    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = user_list.getJSONObject(position)
        holder.index_IV.text=(position+1).toString()
        holder.info_TV.text=user.getString("user_info")
        holder.score_TV.text=user.get("score").toString()
    }

    override fun getItemCount(): Int {
        return user_list.length()
    }

    //更新数据源
    fun updateData(jsonArray: JSONArray) {
        this.user_list = jsonArray
        notifyDataSetChanged()
    }
}
