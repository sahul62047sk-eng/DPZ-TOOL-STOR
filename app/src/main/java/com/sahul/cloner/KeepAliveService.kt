package com.sahul.cloner
import android.app.*
import android.content.*
import android.os.*
import androidx.core.app.NotificationCompat
class KeepAliveService : Service() {
    private var wakeLock: PowerManager.WakeLock? = null
    private val CHANNEL_ID = "SahulCloneBackgroundChannel"
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Sahul Cloner Engine", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sahul Cloner Active")
            .setContentText("Cloned apps background mein chal rahe hain...")
            .setSmallIcon(R.drawable.sahul)
            .setOngoing(true)
            .build()
        startForeground(101, notification)
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SahulCloner::BackgroundLock")
        wakeLock?.acquire()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onDestroy() {
        super.onDestroy()
        if (wakeLock?.isHeld == true) wakeLock?.release()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
