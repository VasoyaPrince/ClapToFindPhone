package com.example.claptofindphone.activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.claptofindphone.databinding.ActivitySplashPinShowBinding
import com.example.claptofindphone.utils.DataStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashPinShowActivity : AppCompatActivity() {
    lateinit var binding: ActivitySplashPinShowBinding
    private lateinit var dataStoreRepo: DataStoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashPinShowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataStoreRepo = DataStoreRepository(this)

        binding.btnSetPin.setOnClickListener {

            val checkPin = binding.etPin.text.toString()
            CoroutineScope(Dispatchers.IO).launch {


                val pin = dataStoreRepo.getPinValue.first()

                if (checkPin == pin) {
                    startActivity(Intent(this@SplashPinShowActivity, MainActivity::class.java))
                    finish()
                } else {
                    Handler(Looper.getMainLooper()).post {
                        val builder: AlertDialog.Builder =
                            AlertDialog.Builder(this@SplashPinShowActivity)
                        builder.setMessage("Password is not match !")
                        builder.setTitle("Alert !")
                        builder.setCancelable(false)
                        builder
                            .setPositiveButton(
                                "Ok"
                            ) { dialog, _ ->

                                dialog.dismiss()
                            }
                        val alertDialog: AlertDialog = builder.create()
                        alertDialog.show()
                    }
                }
            }
        }

        binding.forgetPassword.setOnClickListener {
            MyPinSetCustomDialog().show(supportFragmentManager, "MyPinSetCustomDialog")
        }
    }
}