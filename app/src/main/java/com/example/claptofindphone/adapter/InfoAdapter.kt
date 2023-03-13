package com.example.claptofindphone.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.claptofindphone.databinding.InfoLayoutBinding

class InfoAdapter(private var context: Context, private var items: List<String>) :
    RecyclerView.Adapter<InfoAdapter.InfoHolder>() {

    class InfoHolder(itemView: InfoLayoutBinding) : RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoHolder {
        val binding = InfoLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return InfoHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: InfoHolder, position: Int) {
        val item = items[position]
        holder.binding.information.text = "${position + 1}. $item"
    }

    override fun getItemCount(): Int {
        return items.size
    }
}