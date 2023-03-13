package com.example.claptofindphone.service


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.claptofindphone.activity.AlarmActivity

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        val hour: String? = intent?.getStringExtra("hour1")
        val min: String? = intent?.getStringExtra("min1")
        Log.d("TAG", "onReceive2: $hour $min")
        val i = Intent(context, AlarmActivity::class.java)
        i.putExtra("hour", hour)
        i.putExtra("min", min)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context?.startActivity(i)
        //Toast.makeText(context, "alarm Done..", Toast.LENGTH_SHORT).show()
    }
}