package com.example.claptofindphone.adapter


import android.content.Context
import android.content.Intent
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.claptofindphone.databinding.FeaturesiconBinding
import com.example.claptofindphone.model.Data

class FeaturesAdapter(private var context: Context, private var featuresIcon: List<Data>) :
    RecyclerView.Adapter<FeaturesAdapter.FeaturesHolder>() {

    class FeaturesHolder(itemView: FeaturesiconBinding) : RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturesHolder {
        val binding = FeaturesiconBinding.inflate(LayoutInflater.from(context), parent, false)
        return FeaturesHolder(binding)
    }

    override fun onBindViewHolder(holder: FeaturesHolder, position: Int) {
        val item = featuresIcon[position]
        val mSlideLeft = Slide()
        mSlideLeft.slideEdge = Gravity.START
        TransitionManager.beginDelayedTransition(holder.binding.cardView, mSlideLeft)
        holder.binding.tvName.visibility = View.VISIBLE
        holder.binding.imageView.setImageResource(item.drawable)
        holder.binding.tvName.text = item.name
        item.activityClass?.let {

        holder.binding.cardView.setOnClickListener {
            val intent = Intent(context, item.activityClass)
            item.bundle?.let {
                intent.putExtras(it)
            }
            context.startActivity(intent)

        }
        }

    }

    override fun getItemCount(): Int {

        return featuresIcon.size
    }

}