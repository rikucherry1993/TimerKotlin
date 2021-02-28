package com.tutorial.timeractivity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tutorial.timeractivity.util.NotificationUtil
import com.tutorial.timeractivity.util.PrefUtil

class TimeNotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action){
            AppConstants.ACTION_STOP -> {
                MainActivity.removeAlarm(context)
                PrefUtil.setTimerState(MainActivity.TimerState.Stopped, context)
                NotificationUtil.hideTimerNotification(context)
            }
            AppConstants.ACTION_PAUSE -> {
                var secondRemaining = PrefUtil.getPreviousTimerRemainingSeconds(context)
                val alarmSetTime = PrefUtil.getAlarmSetTime(context)
                val nowSeconds = MainActivity.nowSeconds

                secondRemaining -= nowSeconds - alarmSetTime
                PrefUtil.setPreviousTimerRemainingSeconds(secondRemaining, context)

                MainActivity.removeAlarm(context)
                PrefUtil.setTimerState(MainActivity.TimerState.Paused, context)
                NotificationUtil.showTimerPaused(context)
            }
            AppConstants.ACTION_RESUME -> {
                var secondRemaining = PrefUtil.getPreviousTimerRemainingSeconds(context)
                val wakeUpTime = MainActivity.setAlarm(context, MainActivity.nowSeconds, secondRemaining)
                PrefUtil.setTimerState(MainActivity.TimerState.Running, context)
                NotificationUtil.showTimerRunning(context, wakeUpTime)
            }
            AppConstants.ACTION_START -> {
                val minutesRemaining = PrefUtil.getTimerLength(context)
                val secondsRemaining = minutesRemaining * 60L
                val wakeUpTime = MainActivity.setAlarm(context, MainActivity.nowSeconds, secondsRemaining)
                PrefUtil.setTimerState(MainActivity.TimerState.Running, context)
                PrefUtil.setPreviousTimerRemainingSeconds(secondsRemaining,context)
                NotificationUtil.showTimerRunning(context, wakeUpTime)
            }
        }
    }
}