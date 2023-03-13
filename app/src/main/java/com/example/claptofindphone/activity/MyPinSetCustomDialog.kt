package com.example.claptofindphone.activity


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.claptofindphone.R
import com.example.claptofindphone.databinding.CustomdialogboxBinding
import com.example.claptofindphone.utils.DataStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MyPinSetCustomDialog : DialogFragment() {
    private lateinit var dataStoreRepo: DataStoreRepository

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_alert)
        val binding = CustomdialogboxBinding.inflate(inflater, container, false)

        dataStoreRepo = DataStoreRepository(requireActivity())
        CoroutineScope(Dispatchers.IO).launch {

            val question = dataStoreRepo.getQuestionValue.first().toString()
            binding.que.text = question

            binding.btnForget.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    val ans = dataStoreRepo.getAnsValue.first()
                    val tvAns = binding.ans.text.toString()
                    if (tvAns == ans) {
                        startActivity(Intent(requireActivity(), PinActivity::class.java))
                        activity?.finish()
                        dismiss()
                    } else {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(requireActivity(), "Results don't match", Toast.LENGTH_SHORT)
                                .show()
                            dismiss()
                        }

                    }
                }
            }

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