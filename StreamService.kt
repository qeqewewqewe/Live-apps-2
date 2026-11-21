package com.live.app

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import com.pedro.common.ConnectChecker
import com.pedro.library.rtmp.RtmpDisplay

class StreamService : Service(), ConnectChecker {
    companion object { @Volatile var running = false }
    private var display: RtmpDisplay? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val ch = "live"
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(NotificationChannel(ch, "Live", NotificationManager.IMPORTANCE_LOW))
        val n = Notification.Builder(this, ch).setContentTitle("Live chalche")
            .setSmallIcon(android.R.drawable.ic_media_play).build()
        startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)

        val code = intent?.getIntExtra("code", 0) ?: 0
        val data = intent?.getParcelableExtra<Intent>("data")
        val url = intent?.getStringExtra("url") ?: return START_NOT_STICKY

        val d = RtmpDisplay(this, true, this)
        d.setIntentResult(code, data)
        // Game sound (internal audio). Mic mixing: see notes.
        val ok = d.prepareVideo(1280, 720, 30, 2500 * 1000, 0, resources.displayMetrics.densityDpi) &&
            d.prepareInternalAudio(128 * 1000, 44100, true, false, false)
        if (ok) { d.startStream(url); running = true; display = d } else stopSelf()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        display?.stopStream(); display = null; running = false
        super.onDestroy()
    }
    override fun onBind(i: Intent?): IBinder? = null
    override fun onConnectionStarted(url: String) {}
    override fun onConnectionSuccess() {}
    override fun onConnectionFailed(reason: String) { stopSelf() }
    override fun onNewBitrate(bitrate: Long) {}
    override fun onDisconnect() {}
    override fun onAuthError() {}
    override fun onAuthSuccess() {}
}
