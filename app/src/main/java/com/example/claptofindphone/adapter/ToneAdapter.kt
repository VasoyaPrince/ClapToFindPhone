package com.example.claptofindphone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.claptofindphone.database.TonsViewModel
import com.example.claptofindphone.databinding.TonesLayoutBinding
import com.example.claptofindphone.model.Tone

class ToneAdapter(
    val context: Context,
    private val tones: List<Tone>,
    private val tonsViewModel: TonsViewModel,
    private var onItemClicked: (tone: Tone) -> Unit,
) : RecyclerView.Adapter<ToneAdapter.ToneViewModel>() {

    class ToneViewModel(itemView: TonesLayoutBinding) : RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToneViewModel {
        val binding = TonesLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return ToneViewModel(binding)
    }

    override fun onBindViewHolder(holder: ToneViewModel, position: Int) {
        val tone = tones[position]
//        val toneUri: Uri = Uri.parse(tone.toneUri)
        holder.binding.toneName.text = tone.toneName
        holder.binding.rbTone.isChecked = tone.isSelected == 1
        holder.binding.toneCardView.setOnClickListener {
            playAndSetTone(tone)

        }
        holder.binding.rbTone.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                playAndSetTone(tone)
            }
        }
    }

    private fun playAndSetTone(tone: Tone) {

        onItemClicked(tone)
        tone.isSelected = 1
        tonsViewModel.updateTone(tone)
    }

    override fun getItemCount(): Int {
        return tones.size
    }


}