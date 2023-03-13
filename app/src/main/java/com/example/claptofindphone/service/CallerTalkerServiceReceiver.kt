package com.example.claptofindphone.service


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startForegroundService
import com.example.claptofindphone.model.CallList
import com.example.claptofindphone.utils.DataStoreRepository
import java.util.ArrayList


class CallerTalkerServiceReceiver : BroadcastReceiver() {
    private lateinit var dataStoreRepo: DataStoreRepository
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context?, intent: Intent?) {
        val callList: ArrayList<CallList> = ArrayList()

        if (ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            callList.addAll(getAllContacts(context))

        }

        val serviceIntent = Intent(context, CallScreeningService::class.java)

        if (intent != null) {
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            if (intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                    .equals(TelephonyManager.EXTRA_STATE_OFFHOOK)
            ) {
                //showToast(context, "Call started..$incomingNumber")

            } else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                    .equals(TelephonyManager.EXTRA_STATE_IDLE)
            ) {
                //showToast(context, "Call ended...$incomingNumber")
                //t1!!.speak(incomingNumber, TextToSpeech.QUEUE_FLUSH, null)
                context.stopService(serviceIntent)
            } else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                    .equals(TelephonyManager.EXTRA_STATE_RINGING)
            ) {
                //showToast(context, "Incoming call...$incomingNumbe r")
                val contact = callList.singleOrNull { e ->
                    e.phoneNo.replace(" ","")  == incomingNumber }
                serviceIntent.putExtra("incomingNumber", "${contact?.name ?: incomingNumber}")

                startForegroundService(context, serviceIntent)
            }
        }


    }




    private fun showToast(context: Context?, message: String?) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }
    @SuppressLint("Recycle", "Range")
    private fun getAllContacts(context: Context): ArrayList<CallList> {
        val callList: ArrayList<CallList> = ArrayList()
        val cr = context.contentResolver
        val cur: Cursor? = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )

        if (cur != null) {
            if (cur.count > 0) {
                while (cur.moveToNext()) {
                    val id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID)
                    )
                    val name = cur.getString(
                        cur.getColumnIndex(
                            ContactsContract.Contacts.DISPLAY_NAME
                        )
                    )

                    if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        val pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null
                        )
                        while (pCur!!.moveToNext()) {
                            val phoneNo = pCur.getString(
                                pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER
                                )
                            )

                            callList.add(CallList(name, phoneNo))
                        }

                        pCur.close()
                    }
                }
            }
        }

        cur?.close()
        return callList
    }



}