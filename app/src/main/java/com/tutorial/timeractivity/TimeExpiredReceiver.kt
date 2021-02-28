package com.tutorial.timeractivity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tutorial.timeractivity.util.NotificationUtil
import com.tutorial.timeractivity.util.PrefUtil

class TimeExpiredReceiver : BroadcastReceiver() {

    //★the receiver is not exported because only this app is able to call it.
    override fun onReceive(context: Context, intent: Intent) {
        NotificationUtil.showTimerExpired(context)

        PrefUtil.setTimerState(MainActivity.TimerState.Stopped, context)
        PrefUtil.setAlarmSetTime(0, context)
    }
}