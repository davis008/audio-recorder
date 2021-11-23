package org.bcu.audiorecorder

import android.os.Handler
import android.os.Looper

class Timer(listener: OnTimerTickListener) {


    interface OnTimerTickListener {
        fun onTimerTick(duration: String)
    }

    private var handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private var duration = 0L
    private var delay = 100L


    init {
        runnable = Runnable {
            duration += delay
            handler.postDelayed(runnable, delay)
            listener.onTimerTick(format())
        }
    }

    fun start() {
        handler.postDelayed(runnable, delay)
    }

    fun pause() {
        handler.removeCallbacks(runnable)
    }

    fun stop() {
        handler.removeCallbacks(runnable)
        delay = 0L
    }

    fun format(): String {
        val milliseconds: Long = duration % 1000
        val seconds: Long = (duration / 1000) % 60
        val minutes: Long = (duration / (1000 * 60)) % 60
        val hours: Long = (duration / (1000 * 60 * 60)) % 24

        var formatted: String= if(hours>0)
        "%02d:%02d:%02d.%2d".format(hours, minutes, seconds, milliseconds/10)
        else
        "%02d:%02d.%2d".format(minutes, seconds, milliseconds/100)
        return formatted
    }
}