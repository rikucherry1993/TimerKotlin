package com.tutorial.timeractivity.util

import android.content.Context
import androidx.preference.PreferenceManager
import com.tutorial.timeractivity.MainActivity

class PrefUtil {
    companion object {
        // Something like static members in Java/C#
        fun getTimerLength(context: Context): Int {
            //TODO: placeholder
            return 1
        }

        private const val PREVIOUS_TIMER_LENGTH_SECONDS_ID = "PREVIOUS_TIMER_LENGTH_SECONDS_ID"

        // 若在timer执行过程中变更属性，回到timer画面后，timer应当按照之前的属性设置执行完毕
        fun getPreviousTimerLengthSeconds(context: Context): Long {
            val preference = PreferenceManager.getDefaultSharedPreferences(context)
            return preference.getLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID, 0)
        }

        fun setPreviousTimerLengthSeconds(seconds: Long, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID, seconds)
            editor.apply()
        }

        private const val TIMER_STATE_ID = "TIMER_STATE_ID"

        fun getTimerState(context: Context): MainActivity.TimerState{
            val preference = PreferenceManager.getDefaultSharedPreferences(context)
            val ordinal =  preference.getInt(TIMER_STATE_ID, 0)
            return MainActivity.TimerState.values()[ordinal]
        }

        fun setTimerState(state: MainActivity.TimerState, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            val ordinal = state.ordinal
            editor.putInt(TIMER_STATE_ID, ordinal)
            editor.apply()
        }

        private const val PREVIOUS_TIMER_REMAINING_SECONDS_ID = "PREVIOUS_TIMER_REMAINING_SECONDS_ID"

        // 若在timer执行过程中变更属性，回到timer画面后，timer应当按照之前的属性设置执行完毕
        fun getPreviousTimerRemainingSeconds(context: Context): Long {
            val preference = PreferenceManager.getDefaultSharedPreferences(context)
            return preference.getLong(PREVIOUS_TIMER_REMAINING_SECONDS_ID, 0)
        }

        fun setPreviousTimerRemainingSeconds(seconds: Long, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(PREVIOUS_TIMER_REMAINING_SECONDS_ID, seconds)
            editor.apply()
        }

        // ★在共享偏好的键名中使用package ID
        private const val ALARM_SET_TIME_ID = "com.tutorial.timer.backgrounded_time"

        fun getAlarmSetTime(context: Context): Long {
            val preference = PreferenceManager.getDefaultSharedPreferences(context)
            return preference.getLong(ALARM_SET_TIME_ID, 0)
        }

        fun setAlarmSetTime(time: Long, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(ALARM_SET_TIME_ID, time)
            editor.apply()
        }

    }

}