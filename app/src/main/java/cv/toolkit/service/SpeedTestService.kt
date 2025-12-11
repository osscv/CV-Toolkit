package cv.toolkit.service

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import cv.toolkit.MainActivity
import cv.toolkit.R
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicLong

class SpeedTestService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var startTime = 0L

    companion object {
        const val CHANNEL_ID = "speed_test_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "START"
        const val ACTION_STOP = "STOP"
        const val EXTRA_URL = "url"
        const val EXTRA_USER_AGENT = "user_agent"
        const val EXTRA_THREAD_COUNT = "thread_count"

        // Shared state for UI synchronization
        var isRunning = false
            private set
        val totalBytes = AtomicLong(0L)
        var serviceStartTime = 0L
            private set
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val url = intent.getStringExtra(EXTRA_URL) ?: return START_NOT_STICKY
                val userAgent = intent.getStringExtra(EXTRA_USER_AGENT) ?: ""
                val threadCount = intent.getIntExtra(EXTRA_THREAD_COUNT, 6)
                startTest(url, userAgent, threadCount)
            }
            ACTION_STOP -> stopTest()
        }
        return START_NOT_STICKY
    }

    private fun startTest(url: String, userAgent: String, threadCount: Int) {
        if (isRunning) return
        isRunning = true
        totalBytes.set(0L)
        startTime = System.currentTimeMillis()
        serviceStartTime = startTime

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, createNotification("0.00 MB/s", "0.00 MB"), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, createNotification("0.00 MB/s", "0.00 MB"))
        }

        repeat(threadCount) {
            scope.launch {
                while (isActive && isRunning) {
                    try {
                        val conn = URL(url).openConnection() as HttpURLConnection
                        conn.connectTimeout = 10000
                        conn.readTimeout = 10000
                        if (userAgent.isNotBlank()) conn.setRequestProperty("User-Agent", userAgent)
                        conn.inputStream.use { input ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } != -1 && isRunning) {
                                totalBytes.addAndGet(bytesRead.toLong())
                            }
                        }
                        conn.disconnect()
                    } catch (_: Exception) { }
                }
            }
        }

        scope.launch {
            while (isActive && isRunning) {
                val bytes = totalBytes.get()
                val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
                val mb = bytes / (1024.0 * 1024.0)
                val speed = if (elapsed > 0) mb / elapsed else 0.0
                updateNotification("%.2f MB/s".format(speed), "%.2f MB".format(mb))
                delay(500)
            }
        }
    }

    private fun stopTest() {
        isRunning = false
        serviceStartTime = 0L
        scope.coroutineContext.cancelChildren()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Speed Test", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun createNotification(speed: String, total: String): Notification {
        val stopIntent = Intent(this, SpeedTestService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)
        val openIntent = Intent(this, MainActivity::class.java)
        val openPending = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Speed: $speed")
            .setContentText("Total: $total")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(openPending)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPending)
            .build()
    }

    private fun updateNotification(speed: String, total: String) {
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, createNotification(speed, total))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        isRunning = false
        serviceStartTime = 0L
        scope.cancel()
        super.onDestroy()
    }
}
