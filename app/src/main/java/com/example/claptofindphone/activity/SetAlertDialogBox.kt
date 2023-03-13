package com.example.claptofindphone.activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.example.childmode.MyCustomDialog
import com.example.claptofindphone.R
import com.example.claptofindphone.databinding.CustomAlertbox3Binding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.*

class SetAlertDialogBox : DialogFragment() {

    private lateinit var picker: MaterialTimePicker
    private lateinit var calender: Calendar
    private lateinit var min: String
    private lateinit var hour: String
    private lateinit var timezone: String

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_alert)
        val binding = CustomAlertbox3Binding.inflate(inflater, container, false)
        binding.btnLockNow.setOnClickListener {
            MyCustomDialog(true, null).show(
                childFragmentManager,
                "MyCustomFragment"
            )
        }
        binding.btnSetTime.setOnClickListener {
            showTimePicker()
        }

        return binding.root

    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showTimePicker() {
        calender = Calendar.getInstance();
        picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(calender.get(Calendar.HOUR_OF_DAY))
            .setMinute(calender.get(Calendar.MINUTE))
            .setTitleText("select Alarm Time ")
            .build()

        picker.show(childFragmentManager, "foxAndroid")

        picker.addOnPositiveButtonClickListener {

            if (picker.hour > 12) {

                String.format("%02d", picker.hour - 12) + ":" + String.format(
                    "%02d",
                    picker.minute
                ) + "PM"
                hour = String.format("%02d", picker.hour - 12)
                min = String.format("%02d", picker.minute)
                timezone = "PM"
            } else {
                String.format("%0" + "2d", picker.hour - 12) + ":" + String.format(
                    "%02d",
                    picker.minute
                ) + "AM"
                hour = String.format("%0" + "2d", picker.hour - 12)
                min = String.format("%02d", picker.minute)
                timezone = "AM"
            }

            Log.d("TAG", "showTimePicker1:${picker.hour}:${picker.minute} ${calender.time}")
            calender.set(Calendar.HOUR_OF_DAY, picker.hour)
            calender.set(Calendar.MINUTE, picker.minute)
            calender.set(Calendar.SECOND, 0)
            calender.set(Calendar.MILLISECOND, 0)
            Log.d("TAG", "showTimePicker2: ${calender.time}")

            MyCustomDialog(false, calender).show(
                childFragmentManager,
                "MyCustomFragment"
            )


        }

        picker.addOnNegativeButtonClickListener {
            picker.dismiss()
        }
    }
}