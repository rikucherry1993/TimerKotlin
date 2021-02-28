package com.tutorial.timeractivity.util

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.tutorial.timeractivity.AppConstants
import com.tutorial.timeractivity.MainActivity
import com.tutorial.timeractivity.R
import com.tutorial.timeractivity.TimeNotificationActionReceiver
import java.text.SimpleDateFormat
import java.util.*

class NotificationUtil {

    companion object{
        private const val CHANNEL_ID_TIMER = "menu_timer"
        private const val CHANNEL_NAME_TIMER = "Timer App Timer"
        private const val TIMER_ID = 0

        fun showTimerExpired(context: Context) {
            // 确保在计时器结束以后可以再次被开启？

            // 需要传入一个特定对象的context和一个可以被intent调用
            // 用于控制计时器的application component（broadcast receiver）
            val startIntent = Intent(context, TimeNotificationActionReceiver::class.java)

            // note: property形式的setter
            startIntent.action = AppConstants.ACTION_START
            val startPendingIntent = PendingIntent.getBroadcast(context, 0, startIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT)

            // 需要在timer expired，running，passed三种情况传递notification, 所以写一个公共的builder
            val nBuilder = getBasicNotificationBuilder(context, CHANNEL_ID_TIMER, true)
            nBuilder.setContentTitle("Timer Expired!!")
                    .setContentText("Start again?")
                    .setContentIntent(getPendingIntentWithStack(context, MainActivity::class.java))
                    .addAction(R.drawable.ic_play_arrow, "Start", startPendingIntent)

            // 同样需要一个公共的channel
            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER,true)

            nManager.notify(TIMER_ID, nBuilder.build())
        }

        fun showTimerRunning(context: Context, wakeUpTime: Long) {
            val stopIntent = Intent(context, TimeNotificationActionReceiver::class.java)
            stopIntent.action = AppConstants.ACTION_STOP
            val stopPendingIntent = PendingIntent.getBroadcast(context, 0, stopIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT)

            val pauseIntent = Intent(context, TimeNotificationActionReceiver::class.java)
            pauseIntent.action = AppConstants.ACTION_PAUSE
            val pausePendingIntent = PendingIntent.getBroadcast(context, 0, pauseIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT)

            val df = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)

            val nBuilder = getBasicNotificationBuilder(context, CHANNEL_ID_TIMER, true)
            nBuilder.setContentTitle("Timer is Running.")
                    .setContentText("End: ${df.format(Date(wakeUpTime))}")
                    .setContentIntent(getPendingIntentWithStack(context, MainActivity::class.java))
                    .setOngoing(true) //cannot be dismissed by user
                    .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
                    .addAction(R.drawable.ic_pause, "Pause", pausePendingIntent)

            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER,true)

            nManager.notify(TIMER_ID, nBuilder.build())
        }


        fun showTimerPaused(context: Context) {
            val resumeIntent = Intent(context, TimeNotificationActionReceiver::class.java)
            resumeIntent.action = AppConstants.ACTION_RESUME
            val resumePendingIntent = PendingIntent.getBroadcast(context, 0, resumeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT)

            val nBuilder = getBasicNotificationBuilder(context, CHANNEL_ID_TIMER, true)
            nBuilder.setContentTitle("Timer is paused.")
                    .setContentText("Resume?")
                    .setContentIntent(getPendingIntentWithStack(context, MainActivity::class.java))
                    .setOngoing(true) //cannot be dismissed by user
                    .addAction(R.drawable.ic_play_arrow, "Resume", resumePendingIntent)

            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER,true)

            nManager.notify(TIMER_ID, nBuilder.build())
        }

        fun hideTimerNotification(context: Context){
            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.cancel(TIMER_ID)
        }

        private fun getBasicNotificationBuilder(context: Context, channelId: String, playSound: Boolean)
                : NotificationCompat.Builder {
            // note: 创建提示音的方式
            val notificationSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val nBuilder = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_timer)
                    .setAutoCancel(true)
                    .setDefaults(0)

            if (playSound) nBuilder.setSound(notificationSound)
            return nBuilder
        }

        // TODO:???
        private fun <T> getPendingIntentWithStack(context: Context, javaClass: Class<T>): PendingIntent {
            val resultIntent = Intent(context, javaClass)
            // note: 确保已经被创建的activity不会被重复创建
            resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            val stackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addParentStack(javaClass)
            stackBuilder.addNextIntent(resultIntent)

            return stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT)
        }

        @TargetApi(26)
        private fun NotificationManager.createNotificationChannel(channelId: String, channelName: String,
        playSound: Boolean) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelImportance = if (playSound) NotificationManager.IMPORTANCE_DEFAULT
                else NotificationManager.IMPORTANCE_LOW
                val nChannel = NotificationChannel(channelId, channelName, channelImportance)
                nChannel.enableLights(true)
                nChannel.lightColor = Color.BLUE
                this.createNotificationChannel(nChannel)
            }
        }
    }
}