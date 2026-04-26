package com.example.lab

import android.app.*
import android.content.*
import android.database.sqlite.*
import android.graphics.Color
import android.location.Geocoder
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.SmsManager
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var db: MyHelper
    private var player: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = MyHelper(this)
        val vView = findViewById<VideoView>(R.id.videoView)
        registerForContextMenu(findViewById(R.id.tvContext))

        // --- SQLITE DB Logic ---
        findViewById<Button>(R.id.btnInsert).setOnClickListener {
            val name = findViewById<EditText>(R.id.db_name).text.toString()
            val dept = findViewById<EditText>(R.id.db_dept).text.toString()
            db.insert(name, dept)
            Toast.makeText(this, "Data Saved", Toast.LENGTH_SHORT).show()
        }
// UPDATE
        findViewById<Button>(R.id.btnUpdate).setOnClickListener {
            try {
                val id = findViewById<EditText>(R.id.db_id).text.toString().toInt()
                val name = findViewById<EditText>(R.id.db_name).text.toString()
                val dept = findViewById<EditText>(R.id.db_dept).text.toString()

                val res = db.update(id, name, dept)

                if (res > 0)
                    Toast.makeText(this, "Updated Successfully", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(this, "Enter valid ID", Toast.LENGTH_SHORT).show()
            }
        }

// DELETE
        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            try {
                val id = findViewById<EditText>(R.id.db_id).text.toString().toInt()

                val res = db.delete(id)

                if (res > 0)
                    Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this, "Delete Failed", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(this, "Enter valid ID", Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<Button>(R.id.btnDisplay).setOnClickListener {
            val c = db.read()
            val sb = StringBuilder()
            while (c.moveToNext()) sb.append("ID:${c.getInt(0)} Name:${c.getString(1)} Dept:${c.getString(2)}\n")
            AlertDialog.Builder(this).setTitle("DB Data").setMessage(sb.toString()).show()
        }

        // --- SMS Logic (Fixed: Need Runtime Permission for real device) ---
        findViewById<Button>(R.id.btnSMS).setOnClickListener {
            val ph = findViewById<EditText>(R.id.sms_phone).text.toString()
            val msg = findViewById<EditText>(R.id.sms_msg).text.toString()
            try {
                SmsManager.getDefault().sendTextMessage(ph, null, msg, null, null)
                Toast.makeText(this, "SMS Sent to $ph", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) { Toast.makeText(this, "Failed: SMS Permission?", Toast.LENGTH_SHORT).show() }
        }

        // --- GECODER Logic (Fixed: Handles potential nulls) ---
        findViewById<Button>(R.id.btnLoc).setOnClickListener {
            try {
                val lat = findViewById<EditText>(R.id.loc_lat).text.toString().toDouble()
                val lon = findViewById<EditText>(R.id.loc_lon).text.toString().toDouble()
                val geocoder = Geocoder(this, Locale.getDefault())
                val list = geocoder.getFromLocation(lat, lon, 1)
                if (list!!.isNotEmpty()) {
                    Toast.makeText(this, list[0].getAddressLine(0), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) { Toast.makeText(this, "Enter valid Lat/Lon", Toast.LENGTH_SHORT).show() }
        }

        // --- NOTIFICATION Logic ---
        findViewById<Button>(R.id.btnNotify).setOnClickListener {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= 26) {
                manager.createNotificationChannel(NotificationChannel("exam", "Lab", NotificationManager.IMPORTANCE_HIGH))
            }
            val note = NotificationCompat.Builder(this, "exam")
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle("Lab Status").setContentText("System is Online").build()
            manager.notify(1, note)
        }

        // --- MULTIMEDIA & GRAPHICS ---
        findViewById<Button>(R.id.btnColor).setOnClickListener {
            it.setBackgroundColor(Color.rgb(Random().nextInt(256), Random().nextInt(256), Random().nextInt(256)))
        }
        findViewById<Button>(R.id.btnAudio).setOnClickListener {
            if (player == null) { player = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI); player?.start() }
            else { player?.stop(); player = null }
        }
        vView.setVideoURI(Uri.parse("android.resource://$packageName/${R.raw.test_video}"))
        vView.setMediaController(MediaController(this))

        // --- POPUP MENU ---
        findViewById<Button>(R.id.btnPopup).setOnClickListener { v ->
            val p = PopupMenu(this, v); p.menu.add("Clear Inputs"); p.show()
        }
    }

    // OPTIONS MENU
    override fun onCreateOptionsMenu(menu: Menu?): Boolean { menu?.add("Exit App"); return true }
    // CONTEXT MENU
    override fun onCreateContextMenu(m: ContextMenu?, v: View?, i: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(m, v, i); m?.setHeaderTitle("Action"); m?.add("Copy Record")
    }
}

// --- DATABASE HELPER ---
