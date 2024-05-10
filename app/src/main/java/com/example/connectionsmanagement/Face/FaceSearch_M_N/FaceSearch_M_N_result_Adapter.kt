package com.example.connectionsmanagement.Face.FaceSearch_M_N

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.connectionsmanagement.Face.FaceSearch_1_N.FaceSearch_1_N_result_Adapter
import com.example.connectionsmanagement.R
import com.example.connectionsmanagement.Tools.Tools
import org.json.JSONArray
import org.json.JSONObject

//人脸搜索M:N 结果适配器
class FaceSearch_M_N_result_Adapter(private var search_face_list: JSONArray,private var detect_face_list: JSONArray) : RecyclerView.Adapter<FaceSearch_M_N_result_Adapter.ViewHolder>() {

    //获取视图控件
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image_base64_IV = itemView.findViewById<ImageView>(R.id.faceSearch_M_N_result_corp_image_base64_ImageView)
        val age_TV=itemView.findViewById<TextView>(R.id.faceSearch_M_N_result_age_TextView)
        val gender_TV = itemView.findViewById<TextView>(R.id.faceSearch_M_N_result_gender_TextView)
        val face_shape_TV=itemView.findViewById<TextView>(R.id.faceSearch_M_N_result_face_shape_TextView)
        val emotion_TV = itemView.findViewById<TextView>(R.id.faceSearch_M_N_result_emotion_TextView)
        val user_list_RV = itemView.findViewById<RecyclerView>(R.id.faceSearch_M_N_result_user_list_RecyclerView)
        val user_list_RV_adapter= FaceSearch_1_N_result_Adapter(JSONArray())
        init {
            user_list_RV.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = user_list_RV_adapter
            }
        }

    }

    //设置单项的layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.facesearch_m_n_result_item, parent, false)
        return ViewHolder(view)
    }

    //控件设置
    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val detect_result = detect_face_list.getJSONObject(position)
        val search_result = search_face_list.getJSONObject(position)
        holder.image_base64_IV.setImageBitmap(Tools.getBitmapFromBase64(detect_result.getString("corp_image_base64")))
        holder.age_TV.text=detect_result.get("age").toString()
        holder.gender_TV.text=getStringFromJSONObject(detect_result.getJSONObject("gender"),"type","probability")
        holder.face_shape_TV.text=getStringFromJSONObject(detect_result.getJSONObject("face_shape"),"type","probability")
        holder.emotion_TV.text=getStringFromJSONObject(detect_result.getJSONObject("emotion"),"type","probability")
        holder.user_list_RV_adapter.updateData(search_result.getJSONArray("user_list"))
    }

    override fun getItemCount(): Int {
        return detect_face_list.length()
    }

    //更新数据源
    fun updateData(search_face_list: JSONArray,detect_face_list: JSONArray) {
        this.search_face_list=search_face_list
        this.detect_face_list=detect_face_list
        notifyDataSetChanged()
    }

    //从JSONObject中获取两个数据并转化为字符串
    fun getStringFromJSONObject(jsonObject: JSONObject, parameter1:String, parameter2:String):String{
        val stringBuilder=StringBuilder()
        stringBuilder.append(jsonObject.get(parameter1).toString())
        stringBuilder.append("(")
        stringBuilder.append(jsonObject.get(parameter2).toString())
        stringBuilder.append(")")
        return stringBuilder.toString()
    }


}