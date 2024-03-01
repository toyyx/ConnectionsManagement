package com.example.connectionsmanagement.ConnectionsMap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.connectionsmanagement.R
import de.hdodenhof.circleimageview.CircleImageView

class CardPersonAdapter(private val results: List<CardPerson>) : RecyclerView.Adapter<CardPersonAdapter.ViewHolder>() {

    //获取视图控件
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardImage = itemView.findViewById<CircleImageView>(R.id.cardImage)
        val cardName = itemView.findViewById<TextView>(R.id.cardName)
        val cardRelation = itemView.findViewById<TextView>(R.id.cardRelation)
        val cardPhoneNumber=itemView.findViewById<TextView>(R.id.cardPhoneNumber)
        val cardNotes = itemView.findViewById<TextView>(R.id.cardNotes)
    }

    //设置单项的layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.main_card_human, parent, false)
        return ViewHolder(view)
    }

    //控件设置
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = results[position]
        holder.cardImage.setImageBitmap(result.image)
        holder.cardName.text=result.name
        holder.cardRelation.text=result.relation
        holder.cardPhoneNumber.text=result.phonenumber
        holder.cardNotes.text=result.notes
    }

    override fun getItemCount(): Int {
        return results.size
    }
}