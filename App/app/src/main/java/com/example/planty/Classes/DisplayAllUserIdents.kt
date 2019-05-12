package com.example.planty.Classes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.planty.Entities.Identified
import com.example.planty.R
import com.squareup.picasso.Picasso


class DisplayAllUserIdents(val userIdent: MutableList<Identified>): RecyclerView.Adapter<DisplayAllUserIdents.ViewHolder>(){
    override fun onBindViewHolder(holder: DisplayAllUserIdents.ViewHolder, position: Int) {
        val ident: Identified = userIdent[position]
        holder?.textViewName.text = "Name: " + ident.plantName
        holder?.textViewType.text = "Type: "+ident.baseID

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DisplayAllUserIdents.ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.previousidents, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return userIdent.size
    }
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val textViewName = itemView.findViewById(R.id.previousIdents_PlantName) as TextView
        val textViewType = itemView.findViewById(R.id.previousIdents_PlantType) as TextView

    }
}
////11:03