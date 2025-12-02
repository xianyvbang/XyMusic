package cn.xybbz.config.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.util.Log
import cn.xybbz.common.constants.Constants
import cn.xybbz.config.service.ReportReceiver
import cn.xybbz.config.service.alert.AlertService

/**
 * 定时任务
 */

class AlarmConfig(
    private val application: Context
) {
    private lateinit var am: AlarmManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var reportPendingIntent: PendingIntent
    private var calendar: Calendar? = null

    fun createGetUpAlarmManager(application: Context, requestCode: Int) {
        am = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    }

    /**
     * 启动定时关闭
     * @param [calendar] 日历
     * @param [ifPlayEndClose] 是否播放接收后关闭
     */
    @SuppressLint("NewApi")
    fun getUpAlarmManagerStartWork(calendar: Calendar, ifPlayEndClose: Boolean) {
        this.calendar = calendar
        // 6.0及以上

        Log.i("=====","当前时间${calendar.timeInMillis}, 当前: ${System.currentTimeMillis()}")
//        am!!.setExactAndAllowWhileIdle(

        val intent = Intent(application, AlertService::class.java).apply{
            putExtra("ifPlayEndClose", ifPlayEndClose)
        }
        //不同的任务requesCode需要定义成不同的，否则，后面的会把前面的任务给覆盖掉
        pendingIntent = PendingIntent.getBroadcast(
            application, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis, pendingIntent
        )
    }

    fun cancelAllAlarm() {
        try {
            am.cancel(pendingIntent)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "取消定时关闭",e)
        }
    }

    /**
     * 设置10秒后的定时任务
     */
    fun scheduleNextReport() {
        val triggerTime = System.currentTimeMillis() + 10_000
        reportPendingIntent = PendingIntent.getBroadcast(
            application, 0,
            Intent(application, ReportReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (canScheduleExactAlarm()) {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, reportPendingIntent)
        }

    }

    fun canScheduleExactAlarm(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 (API 31) 及以上
            am.canScheduleExactAlarms()
        } else {
            // Android 11 及以下，不需要权限，也不存在权限限制
            true
        }
    }

    fun cancelScheduleNextReport() {
        try {
            am.cancel(reportPendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(Constants.LOG_ERROR_PREFIX, "取消定时关闭",e)
        }
    }

    /**
     * 返回AlarmManager
     */
    fun returnAm():AlarmManager{
        return am
    }
}