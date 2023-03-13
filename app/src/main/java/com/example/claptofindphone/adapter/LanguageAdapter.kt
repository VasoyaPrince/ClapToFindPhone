package com.example.claptofindphone.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.claptofindphone.databinding.LanguageLayoutBinding
import com.example.claptofindphone.model.Language
import com.example.claptofindphone.model.Tone

class LanguageAdapter(private var context: Context,
                      private var items: List<Language>,
                      private var onItemClicked: (Language: Language) -> Unit) :
    RecyclerView.Adapter<LanguageAdapter.LanguageHolder>() {

    class LanguageHolder(itemView: LanguageLayoutBinding) : RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageHolder {
        val binding = LanguageLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return LanguageHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LanguageHolder, position: Int) {
        val item = items[position]
        holder.binding.name.text = item.name
        holder.binding.selectedRadio.isChecked = item.isSelected
        holder.binding.flagImageView.setImageResource(item.img)
        holder.binding.root.setOnClickListener {
            if (!item.isSelected) {
                val selectedIndex = items.indexOfFirst { it.isSelected }
                items[selectedIndex].isSelected = false
                items[position].isSelected = true
                notifyItemChanged(position)
                notifyItemChanged(selectedIndex)
                onItemClicked(item)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}