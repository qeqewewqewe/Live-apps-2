package com.live.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var status: TextView

    private val projection = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { r ->
        if (r.resultCode == RESULT_OK && r.data != null) {
            val full = findViewById<EditText>(R.id.url).text.toString().trim().trimEnd('/') +
                "/" + findViewById<EditText>(R.id.key).text.toString().trim()
            val i = Intent(this, StreamService::class.java)
                .putExtra("code", r.resultCode).putExtra("data", r.data).putExtra("url", full)
            startForegroundService(i)
            status.text = "Live starting..."
        } else status.text = "Permission denied"
    }

    private val perms = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        val m = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projection.launch(m.createScreenCaptureIntent())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.status)
        findViewById<Button>(R.id.btn).setOnClickListener {
            if (StreamService.running) {
                stopService(Intent(this, StreamService::class.java))
                status.text = "Stopped"
            } else perms.launch(arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS))
        }
    }
}
