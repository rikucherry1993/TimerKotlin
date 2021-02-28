package com.tutorial.timeractivity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.tutorial.timeractivity.util.NotificationUtil
import com.tutorial.timeractivity.util.PrefUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    
    // 类似于static member
    companion object {
        fun setAlarm(context: Context, nowSeconds: Long, remainingSeconds: Long): Long{
            val wakeUpTime = (nowSeconds + remainingSeconds) * 1000
            // note: as: unsafe casting(as? is safe), the equivalent of (type)foo in java.
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            // note: 'ClassName::class.java'(kotlin) = ClassName.class(java) (class object)
            val intent = Intent(context, TimeExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)

            PrefUtil.setAlarmSetTime(nowSeconds,context)
            return wakeUpTime
        }

        fun removeAlarm(context: Context){
            val intent = Intent(context, TimeExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)

            // note: 0代表Alarm未开启
            PrefUtil.setAlarmSetTime(0,context)
        }


        // note: kotlin的property写法。initializer，getter，setter都是optional的。type有时也可以省略
        val nowSeconds: Long
            get() = Calendar.getInstance().timeInMillis / 1000


    }

    enum class TimerState{
        Stopped, Paused, Running
    }

    private lateinit var timer: CountDownTimer
    private var timerLengthSeconds: Long = 0
    private var secondsRemaining: Long = 0
    private var timerState = TimerState.Stopped

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        supportActionBar?.setIcon(R.drawable.ic_timer)
        supportActionBar?.title = "   Timer"

        // 使用kotlin-android-extensions插件直接访问xml中的元素
        fab_play.setOnClickListener{v ->
            startTimer()
            timerState = TimerState.Running
            updateButtons()
        }

        fab_pause.setOnClickListener { v ->
            timer.cancel()
            timerState = TimerState.Paused
            updateButtons()

        }

        fab_stop.setOnClickListener { v ->
            timer.cancel()
            onTimerFinished()

        }

    }


    override fun onResume(){
        super.onResume()

        initTimer()

        removeAlarm(this)
        NotificationUtil.hideTimerNotification(this)
    }


    override fun onPause() {
        super.onPause()

        if (timerState == TimerState.Running) {
            timer.cancel()
            // note: 创建后台Alarm，将在现在时刻+闹钟的剩余时间唤醒
            val wakeUpTime = setAlarm(this, nowSeconds, secondsRemaining)
            NotificationUtil.showTimerRunning(this,wakeUpTime)
        }
        else if(timerState == TimerState.Paused) {
            NotificationUtil.showTimerPaused(this)
        }

        PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds, this)
        PrefUtil.setPreviousTimerRemainingSeconds(secondsRemaining, this)
        PrefUtil.setTimerState(timerState, this)

    }

    private fun initTimer(){
        timerState = PrefUtil.getTimerState(this);
            if (timerState == TimerState.Stopped)
                setNewTimerLength()
            else
                setPreviousTimerLength()

        secondsRemaining = if (timerState == TimerState.Running || timerState == TimerState.Paused)
            PrefUtil.getPreviousTimerRemainingSeconds(this)
        else
            timerLengthSeconds

        val alarmSetTime = PrefUtil.getAlarmSetTime(this)
        // note: alarmSetTime > 0 means alarm is set.
        if (alarmSetTime > 0)
            secondsRemaining -= nowSeconds - alarmSetTime

        // note: Alarm has finished on background
        if (secondsRemaining <= 0)
            onTimerFinished()
        else if (timerState == TimerState.Running)
            startTimer()

        updateButtons()
        updateCountdownUI()

    }


    private fun onTimerFinished() {
        timerState = TimerState.Stopped
        setNewTimerLength()

        progress_countdown.progress = 0
        //计时器停止时，将剩余秒数设为初始总秒数
        PrefUtil.setPreviousTimerRemainingSeconds(timerLengthSeconds, this)
        secondsRemaining = timerLengthSeconds

        updateButtons()
        updateCountdownUI()
    }


    private fun startTimer(){
        timerState = TimerState.Running
        timer = object : CountDownTimer(secondsRemaining * 1000, 1000){
            override fun onFinish() = onTimerFinished()
            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                updateCountdownUI()
            }
        }.start()
    }

    private fun setNewTimerLength(){
        val lengthInMinutes = PrefUtil.getTimerLength(this)
        timerLengthSeconds = (lengthInMinutes * 60L)
        progress_countdown.max = timerLengthSeconds.toInt()
    }

    private fun setPreviousTimerLength(){
        timerLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(this)
        progress_countdown.max = timerLengthSeconds.toInt()
    }


    private fun updateCountdownUI() {
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()

        textView_countdown.text = "$minutesUntilFinished:${if (secondsStr.length == 2) secondsStr
        else "0" + secondsStr}"

        progress_countdown.progress =  (timerLengthSeconds - secondsRemaining).toInt()
    }

    private fun updateButtons(){
        when(timerState){
            TimerState.Running -> {
                fab_play.isEnabled = false
                fab_pause.isEnabled = true
                fab_stop.isEnabled = true
            }
            TimerState.Stopped -> {
                fab_play.isEnabled = true
                fab_pause.isEnabled = false
                fab_stop.isEnabled = false
            }
            TimerState.Paused -> {
                fab_play.isEnabled = true
                fab_pause.isEnabled = false
                fab_stop.isEnabled = true
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}