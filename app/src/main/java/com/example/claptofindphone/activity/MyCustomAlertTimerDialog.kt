package com.example.childmode


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.DialogFragment
import com.example.claptofindphone.R
import com.example.claptofindphone.databinding.CustomAlerttimerBinding
import com.example.claptofindphone.service.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.*

class MyCustomAlertTimerDialog(
    var hour: String,
    var min: String,
    var calender: Calendar,
) : DialogFragment() {

    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_alert)
        val binding = CustomAlerttimerBinding.inflate(inflater, container, false)

        if (hour.isEmpty()){
            binding.tvTime.text = "00 : $min"
        }else{
            binding.tvTime.text = "$hour : $min"
        }

        binding.timer.text = SimpleDateFormat("HH:mm").format(calender.time)
        Log.d(TAG, "onCreateView: ${SimpleDateFormat("HH:mm").format(calender.time)}")
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnStart.setOnClickListener {
            setAlarm()
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

    private fun setAlarm() {
        alarmManager = getSystemService(requireActivity(), AlarmManager::class.java) as AlarmManager
        val intent = Intent(requireActivity(), AlarmReceiver::class.java)

        intent.putExtra("hour1", hour)
        intent.putExtra("min1", min)
        Log.d("TAG", "setAlarm1: $hour $min")
        pendingIntent = PendingIntent.getBroadcast(
            requireActivity(),
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calender.timeInMillis, pendingIntent)

        activity?.finish()
        activity?.moveTaskToBack(true)

    }

}