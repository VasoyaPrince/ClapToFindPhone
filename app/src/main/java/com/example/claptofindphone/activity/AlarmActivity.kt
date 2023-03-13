package com.example.claptofindphone.activity

import android.os.Bundle
import android.os.CountDownTimer
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.claptofindphone.databinding.ActivityAlarmBinding
import java.util.*


class AlarmActivity : AppCompatActivity() {
    lateinit var binding: ActivityAlarmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )

        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val hour: String? = intent.getStringExtra("hour")
        val min: String? = intent.getStringExtra("min")


        supportActionBar?.hide()

        val duration: Long =
            java.util.concurrent.TimeUnit.MINUTES.toMillis((hour!!.toLong() * 60) + min!!.toLong())

        val timer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val sDuration = String.format(
                    Locale.ENGLISH, "%02d:%02d",
                    java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                    java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                            java.util.concurrent.TimeUnit.MINUTES.toSeconds(
                                java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(
                                    millisUntilFinished
                                )
                            )
                )

                binding.time.text = sDuration
            }

            override fun onFinish() {
                finish()
            }
        }
        timer.start()

    }

    override fun onBackPressed() {
        //super.onBackPressed()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }

    private class MyGestureDetector : SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent, e2: MotionEvent, velocityX: Float,
            velocityY: Float
        ): Boolean {
            return true
        }

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideNavigationBar()
        hideSystemUI()
    }

    private fun hideNavigationBar() {
        val decorView: View = this.window.decorView
        val uiOptions: Int = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        val timer = Timer()
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                this@AlarmActivity.runOnUiThread {
                    decorView.setSystemUiVisibility(
                        uiOptions
                    )
                }
            }
        }
        timer.scheduleAtFixedRate(task, 1, 2)
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(
            window,
            window.decorView.findViewById(android.R.id.content)
        ).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())

            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}