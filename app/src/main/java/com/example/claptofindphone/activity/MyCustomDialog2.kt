package com.example.childmode


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.claptofindphone.R
import com.example.claptofindphone.activity.AlarmActivity
import com.example.claptofindphone.databinding.CustomAlertbox2Binding

class MyCustomDialog2(var hour: String , var min: String) : DialogFragment() {


    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_alert)
        val binding = CustomAlertbox2Binding.inflate(inflater, container, false)

        if (hour.isEmpty()){
            binding.tvTime.text = "00 : $min"
        }else{
            binding.tvTime.text = "$hour : $min"
        }
        binding.btnCancel.setOnClickListener {
            dismiss()

        }
        binding.btnStart.setOnClickListener {
            val intent = Intent(requireActivity(), AlarmActivity::class.java)
            intent.putExtra("hour", hour)
            intent.putExtra("min", min)
            startActivity(intent)
            dismiss()
            activity?.finish()
        }

        return binding.root

    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}