package com.example.connectionsmanagement.Face.FaceDetect

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.connectionsmanagement.R
import com.example.connectionsmanagement.Tools.ImageDownloader.getBitmapFromBase64
import org.json.JSONArray
import org.json.JSONObject


class FaceDetect_result_Adapter(private var results: JSONArray) : RecyclerView.Adapter<FaceDetect_result_Adapter.ViewHolder>() {

    //获取视图控件
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image_base64_IV = itemView.findViewById<ImageView>(R.id.faceDetect_result_corp_image_base64_ImageView)
        val face_token_TV = itemView.findViewById<TextView>(R.id.faceDetect_result_face_token_TextView)
        val face_probability_TV = itemView.findViewById<TextView>(R.id.faceDetect_result_face_probability_TextView)
        val age_TV=itemView.findViewById<TextView>(R.id.faceDetect_result_age_TextView)
        val gender_TV = itemView.findViewById<TextView>(R.id.faceDetect_result_gender_TextView)
        val face_shape_TV=itemView.findViewById<TextView>(R.id.faceDetect_result_face_shape_TextView)
        val emotion_TV = itemView.findViewById<TextView>(R.id.faceDetect_result_emotion_TextView)
        val spoofing_TV=itemView.findViewById<TextView>(R.id.faceDetect_result_spoofing_TextView)
        val face_type_TV = itemView.findViewById<TextView>(R.id.faceDetect_result_face_type_TextView)
    }

    //设置单项的layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.facedetect_result_item, parent, false)
        return ViewHolder(view)
    }

    //控件设置
    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = results.getJSONObject(position)
        holder.image_base64_IV.setImageBitmap(getBitmapFromBase64(result.getString("corp_image_base64")))
        holder.face_token_TV.text=result.getString("face_token")
        holder.face_probability_TV.text=result.get("face_probability").toString()
        holder.age_TV.text=result.get("age").toString()
        holder.gender_TV.text=getStringFromJSONObject(result.getJSONObject("gender"),"type","probability")
        holder.face_shape_TV.text=getStringFromJSONObject(result.getJSONObject("face_shape"),"type","probability")
        holder.emotion_TV.text=getStringFromJSONObject(result.getJSONObject("emotion"),"type","probability")
        holder.spoofing_TV.text=result.get("spoofing").toString()
        holder.face_type_TV.text=getStringFromJSONObject(result.getJSONObject("face_type"),"type","probability")
    }

    override fun getItemCount(): Int {
        return results.length()
    }

    fun updateData(jsonArray: JSONArray) {
        this.results = jsonArray
        notifyDataSetChanged()
    }

    fun getStringFromJSONObject(jsonObject: JSONObject,parameter1:String,parameter2:String):String{
        val stringBuilder=StringBuilder()
        stringBuilder.append(jsonObject.get(parameter1).toString())
        stringBuilder.append("(")
        stringBuilder.append(jsonObject.get(parameter2).toString())
        stringBuilder.append(")")
        return stringBuilder.toString()
    }


}


