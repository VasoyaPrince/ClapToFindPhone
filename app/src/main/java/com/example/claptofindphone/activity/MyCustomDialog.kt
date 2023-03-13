package com.example.childmode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.claptofindphone.R
import com.example.claptofindphone.databinding.CustomLayoutBinding
import java.util.*

class MyCustomDialog(
    private val isNow: Boolean,
    private val calender: Calendar?
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_alert)
        val binding = CustomLayoutBinding.inflate(inflater, container, false)


        binding.btnSet.setOnClickListener {

            if (isNow) {
                val hour: String = binding.edHour.text.toString()
                val min: String = binding.edMin.text.toString()
                if (hour.isNotEmpty() && min.isNotEmpty()) {
                    MyCustomDialog2(hour, min).show(childFragmentManager, "MyCustomFragment2")
                }
            } else {
                val hour: String = binding.edHour.text.toString()
                val min: String = binding.edMin.text.toString()
                if (hour.isNotEmpty() && min.isNotEmpty()) {
                    MyCustomAlertTimerDialog(hour, min, calender!!).show(
                        childFragmentManager,
                        "MyCustomAlertTimerDialog"
                    )
                }
            }
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
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