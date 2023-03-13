package com.example.claptofindphone.service

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.example.claptofindphone.activity.DontTouchPopupActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class TouchService : Service(), SensorEventListener {

    companion object{
        var serviceStop = MutableLiveData<Boolean>()
    }

    private var sensorMan: SensorManager? = null
    private var accelerometer: Sensor? = null
    private lateinit var mGravity: FloatArray
    private var mAccel = 0f
    private var mAccelCurrent = 0f
    private var mAccelLast = 0f

    override fun onCreate() {
        super.onCreate()
        sensorMan = getSystemService(AppCompatActivity.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorMan!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        mAccel = 0.00f
        mAccelCurrent = SensorManager.GRAVITY_EARTH
        mAccelLast = SensorManager.GRAVITY_EARTH
    }
    override fun onBind(intent: Intent): IBinder? {
        return  null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        sensorMan?.registerListener(
            this@TouchService, accelerometer,
            SensorManager.SENSOR_DELAY_UI
        )
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                mGravity = event.values.clone()
                // Shake detection
                val x: Float = mGravity[0]
                val y: Float = mGravity[1]
                val z: Float = mGravity[2]
                mAccelLast = mAccelCurrent
                mAccelCurrent = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val delta = mAccelCurrent - mAccelLast
                mAccel = mAccel * 0.9f + delta
                if (mAccel > 0.5) {
                   // CoroutineScope(Dispatchers.IO).launch {
//                        if (isFlashValue) {
//                            flashOn()
//                        }
                    sensorMan?.unregisterListener(
                        this@TouchService, accelerometer
                    )
                    serviceStop.postValue(false)
                        startActivity(Intent(this@TouchService, DontTouchPopupActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK ))
                    stopSelf()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}