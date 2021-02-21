package com.tutorial.timeractivity

import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.tutorial.timeractivity.util.PrefUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    enum class TimerState{
        Stopped, Paused, Running
    }

    private lateinit var timer:CountDownTimer
    private var timerLengthSeconds = 0L
    private var secondsRemaining = 0L
    private var timerState: TimerState = TimerState.Stopped

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
            updateButtons()

        }

    }


    override fun onResume(){
        super.onResume()

        initTimer()

        //TODO: remove background timer, hide notification
    }


    override fun onPause() {
        super.onPause()

        if (timerState == TimerState.Running) {
            timer.cancel()
            //TODO: start background timer and show notification
        }
        else if(timerState == TimerState.Paused) {
            //TODO: show notification
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

        //TODO: change secondsRemaining according to where the background timer stopped


        //resume where we left off
        if (timerState == TimerState.Running)
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
        timerLengthSeconds = lengthInMinutes * 60L
        progress_countdown.max = timerLengthSeconds.toInt()
        progress_countdown.progress = 0
    }

    private fun setPreviousTimerLength(){
        timerLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(this)
        progress_countdown.max = timerLengthSeconds.toInt()
    }


    private fun updateCountdownUI() {
        val minutesUntilFinished = secondsRemaining / 60
        //todo : ??
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()

        textView_countdown.text = "$minutesUntilFinished:${
        if (secondsStr.length == 2) secondsStr
        else "0" + secondsStr}"

        progress_countdown.progress = secondsRemaining.toInt()
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
                fab_pause.isEnabled = true
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