package com.simplemobiletools.clock.extensions

import android.annotation.SuppressLint
import android.app.*
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.net.Uri
import android.os.PowerManager
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.widget.Toast
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.ReminderActivity
import com.simplemobiletools.clock.activities.SnoozeReminderActivity
import com.simplemobiletools.clock.activities.SplashActivity
import com.simplemobiletools.clock.helpers.*
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.clock.models.MyTimeZone
import com.simplemobiletools.clock.receivers.AlarmReceiver
import com.simplemobiletools.clock.receivers.DateTimeWidgetUpdateReceiver
import com.simplemobiletools.clock.receivers.HideAlarmReceiver
import com.simplemobiletools.clock.receivers.HideTimerReceiver
import com.simplemobiletools.clock.services.SnoozeService
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.ALARM_SOUND_TYPE_ALARM
import com.simplemobiletools.commons.helpers.DAY_MINUTES
import com.simplemobiletools.commons.helpers.SILENT
import com.simplemobiletools.commons.helpers.isOreoPlus
import java.util.*
import kotlin.math.pow
import android.app.PendingIntent
import android.util.Log
import kotlin.collections.ArrayList


val Context.config: Config get() = Config.newInstance(applicationContext)

val Context.dbHelper: DBHelper get() = DBHelper.newInstance(applicationContext)

fun Context.getFormattedDate(calendar: Calendar): String {
    val dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7    // make sure index 0 means monday
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH)

    val dayString = resources.getStringArray(R.array.week_days_short)[dayOfWeek]
    val monthString = resources.getStringArray(R.array.months)[month]
    return "$dayString, $dayOfMonth $monthString"
}

fun Context.getEditedTimeZonesMap(): HashMap<Int, String> {
    val editedTimeZoneTitles = config.editedTimeZoneTitles
    val editedTitlesMap = HashMap<Int, String>()
    editedTimeZoneTitles.forEach {
        val parts = it.split(EDITED_TIME_ZONE_SEPARATOR.toRegex(), 2)
        editedTitlesMap[parts[0].toInt()] = parts[1]
    }
    return editedTitlesMap
}

fun Context.getAllTimeZonesModified(): ArrayList<MyTimeZone> {
    val timeZones = getAllTimeZones()
    val editedTitlesMap = getEditedTimeZonesMap()
    timeZones.forEach {
        if (editedTitlesMap.keys.contains(it.id)) {
            it.title = editedTitlesMap[it.id]!!
        } else {
            it.title = it.title.substring(it.title.indexOf(' ')).trim()
        }
    }
    return timeZones
}

fun Context.getModifiedTimeZoneTitle(id: Int) = getAllTimeZonesModified().firstOrNull { it.id == id }?.title ?: getDefaultTimeZoneTitle(id)

fun Context.createNewAlarm(timeInMinutes: Int, weekDays: Int): Alarm {
    val defaultAlarmSound = getDefaultAlarmSound(ALARM_SOUND_TYPE_ALARM)
    return Alarm(0, timeInMinutes, weekDays, isEnabled = false, vibrate = false, soundTitle = defaultAlarmSound.title, soundUri = defaultAlarmSound.uri, label = "")
}

fun Context.scheduleNextAlarm(alarm: Alarm, showToast: Boolean) {
    val calendar = Calendar.getInstance()
    calendar.firstDayOfWeek = Calendar.MONDAY
    for (i in 0..7) {
        val currentDay = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
        val isCorrectDay = alarm.days and 2.0.pow(currentDay).toInt() != 0
        val currentTimeInMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        if (isCorrectDay && (alarm.timeInMinutes > currentTimeInMinutes || i > 0) ) {
            val triggerInMinutes = alarm.timeInMinutes - currentTimeInMinutes + (i * DAY_MINUTES)
            setupAlarmClock(alarm, triggerInMinutes * 60 - calendar.get(Calendar.SECOND))
            val children = dbHelper.getChildAlarms(alarm.id)
            for (child in children){
                val triggerInMinutes = child.timeInMinutes - currentTimeInMinutes + (i * DAY_MINUTES)
                setupAlarmClock(child, triggerInMinutes * 60 - calendar.get(Calendar.SECOND))
            }
            if(!alarm.isChild) {
                val intent = Intent()
                intent.action = NEW_ALARM
                intent.putExtra("hours", alarm?.timeInMinutes / 60)
                intent.putExtra("minutes", alarm?.timeInMinutes % 60)
                intent.putExtra("days", alarm?.days)
                intent.putExtra("id", alarm?.id)
                intent.putExtra("label", alarm?.label)
                applicationContext.sendBroadcast(intent)
            }

            if (showToast) {
                showRemainingTimeMessage(triggerInMinutes)
            }
            break
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }
}

fun Context.showRemainingTimeMessage(totalMinutes: Int) {
    val fullString = String.format(getString(R.string.alarm_goes_off_in), formatMinutesToTimeString(totalMinutes))
    toast(fullString, Toast.LENGTH_LONG)
}

fun Context.setupAlarmClock(alarm: Alarm, triggerInSeconds: Int) {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val targetMS = System.currentTimeMillis() + triggerInSeconds * 1000
    AlarmManagerCompat.setAlarmClock(alarmManager, targetMS, getOpenAlarmTabIntent(), getAlarmIntent(alarm))
//    val children = dbHelper.getChildAlarms(alarm.id)
//    for (child in children){
//        AlarmManagerCompat.setAlarmClock(alarmManager, targetMS, getOpenAlarmTabIntent(), getAlarmIntent(child))
//    }
}

fun Context.getOpenAlarmTabIntent(): PendingIntent {
    val intent = getLaunchIntent() ?: Intent(this, SplashActivity::class.java)
    intent.putExtra(OPEN_TAB, TAB_ALARM)
    return PendingIntent.getActivity(this, OPEN_ALARMS_TAB_INTENT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}

fun Context.getOpenTimerTabIntent(): PendingIntent {
    val intent = getLaunchIntent() ?: Intent(this, SplashActivity::class.java)
    intent.putExtra(OPEN_TAB, TAB_TIMER)
    return PendingIntent.getActivity(this, TIMER_NOTIF_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}

fun Context.getAlarmIntent(alarm: Alarm): PendingIntent {
    val intent = Intent(this, AlarmReceiver::class.java)
    intent.putExtra(ALARM_ID, alarm.id)
    return PendingIntent.getBroadcast(this, alarm.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}

fun Context.cancelAlarmClock(alarm: Alarm) {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val children = dbHelper.getChildAlarms(alarm.id)
    alarmManager.cancel(getAlarmIntent(alarm))
    for (child in children){
        alarmManager.cancel(getAlarmIntent(child))
    }

    val intent = Intent()
    intent.action = ALARM_CANCELED
    intent.putExtra("hours", alarm?.timeInMinutes / 60)
    intent.putExtra("minutes", alarm?.timeInMinutes % 60)
    intent.putExtra("days", alarm?.days)
    intent.putExtra("id", alarm?.id)
    intent.putExtra("label", alarm?.label)
    applicationContext.sendBroadcast(intent)

}

fun Context.hideNotification(id: Int) {
    val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.cancel(id)
}

fun Context.hideTimerNotification() = hideNotification(TIMER_NOTIF_ID)

fun Context.updateWidgets() {
    val widgetsCnt = AppWidgetManager.getInstance(applicationContext).getAppWidgetIds(ComponentName(applicationContext, MyWidgetDateTimeProvider::class.java))
    if (widgetsCnt.isNotEmpty()) {
        val ids = intArrayOf(R.xml.widget_date_time_info)
        Intent(applicationContext, MyWidgetDateTimeProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            sendBroadcast(this)
        }
    }
}

@SuppressLint("NewApi")
fun Context.scheduleNextWidgetUpdate() {
    val widgetsCnt = AppWidgetManager.getInstance(applicationContext).getAppWidgetIds(ComponentName(applicationContext, MyWidgetDateTimeProvider::class.java))
    if (widgetsCnt.isEmpty()) {
        return
    }

    val intent = Intent(this, DateTimeWidgetUpdateReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(this, UPDATE_WIDGET_INTENT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val triggerAtMillis = System.currentTimeMillis() + getMSTillNextMinute()
    alarmManager.setExact(AlarmManager.RTC, triggerAtMillis, pendingIntent)
}

fun Context.getFormattedTime(passedSeconds: Int, showSeconds: Boolean, makeAmPmSmaller: Boolean): SpannableString {
    val use24HourFormat = config.use24HourFormat
    val hours = (passedSeconds / 3600) % 24
    val minutes = (passedSeconds / 60) % 60
    val seconds = passedSeconds % 60

    return if (!use24HourFormat) {
        val formattedTime = formatTo12HourFormat(showSeconds, hours, minutes, seconds)
        val spannableTime = SpannableString(formattedTime)
        val amPmMultiplier = if (makeAmPmSmaller) 0.4f else 1f
        spannableTime.setSpan(RelativeSizeSpan(amPmMultiplier), spannableTime.length - 5, spannableTime.length, 0)
        spannableTime
    } else {
        val formattedTime = formatTime(showSeconds, use24HourFormat, hours, minutes, seconds)
        SpannableString(formattedTime)
    }
}

fun Context.formatTo12HourFormat(showSeconds: Boolean, hours: Int, minutes: Int, seconds: Int): String {
    val appendable = getString(if (hours >= 12) R.string.p_m else R.string.a_m)
    val newHours = if (hours == 0 || hours == 12) 12 else hours % 12
    return "${formatTime(showSeconds, false, newHours, minutes, seconds)} $appendable"
}

fun Context.getNextAlarm(): String {
    val milliseconds = (getSystemService(Context.ALARM_SERVICE) as AlarmManager).nextAlarmClock?.triggerTime ?: return ""
    val calendar = Calendar.getInstance()
    val isDaylightSavingActive = TimeZone.getDefault().inDaylightTime(Date())
    var offset = calendar.timeZone.rawOffset
    if (isDaylightSavingActive) {
        offset += TimeZone.getDefault().dstSavings
    }

    calendar.timeInMillis = milliseconds
    val dayOfWeekIndex = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val dayOfWeek = resources.getStringArray(R.array.week_days_short)[dayOfWeekIndex]
    val formatted = getFormattedTime(((milliseconds + offset) / 1000L).toInt(), false, false)
    return "$dayOfWeek $formatted"
}

fun Context.rescheduleEnabledAlarms() {
    dbHelper.getEnabledAlarms().forEach {
        scheduleNextAlarm(it, false)
    }
}

fun Context.isScreenOn() = (getSystemService(Context.POWER_SERVICE) as PowerManager).isScreenOn

fun Context.showAlarmNotification(alarm: Alarm) {
    val pendingIntent = getOpenAlarmTabIntent()
    val notification = getAlarmNotification(pendingIntent, alarm)
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(alarm.id, notification)
    scheduleNextAlarm(alarm, false)
}

fun Context.showTimerNotification(addDeleteIntent: Boolean) {
    val pendingIntent = getOpenTimerTabIntent()
    val notification = getTimerNotification(pendingIntent, addDeleteIntent)
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(TIMER_NOTIF_ID, notification)
}

@SuppressLint("NewApi")
fun Context.getTimerNotification(pendingIntent: PendingIntent, addDeleteIntent: Boolean): Notification {
    var soundUri = config.timerSoundUri
    if (soundUri == SILENT) {
        soundUri = ""
    } else {
        grantReadUriPermission(soundUri)
    }

    val channelId = "simple_timer_channel_$soundUri"
    if (isOreoPlus()) {
        val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setLegacyStreamType(AudioManager.STREAM_ALARM)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val name = getString(R.string.timer)
        val importance = NotificationManager.IMPORTANCE_HIGH
        NotificationChannel(channelId, name, importance).apply {
            setBypassDnd(true)
            enableLights(true)
            lightColor = getAdjustedPrimaryColor()
            enableVibration(config.timerVibrate)
            setSound(Uri.parse(soundUri), audioAttributes)
            notificationManager.createNotificationChannel(this)
        }
    }

    val reminderActivityIntent = getReminderActivityIntent()
    val builder = NotificationCompat.Builder(this)
            .setContentTitle(getString(R.string.timer))
            .setContentText(getString(R.string.time_expired))
            .setSmallIcon(R.drawable.ic_timer)
            .setContentIntent(pendingIntent)
            .setPriority(Notification.PRIORITY_LOW)
            .setDefaults(Notification.DEFAULT_LIGHTS)
            .setAutoCancel(true)
            .setSound(Uri.parse(soundUri), AudioManager.STREAM_ALARM)
            .setChannelId(channelId)
            .addAction(R.drawable.ic_cross_vector, getString(R.string.dismiss), if (addDeleteIntent) reminderActivityIntent else getHideTimerPendingIntent())

    if (addDeleteIntent) {
        builder.setDeleteIntent(reminderActivityIntent)
    }

    builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

    if (config.timerVibrate) {
        val vibrateArray = LongArray(2) { 500 }
        builder.setVibrate(vibrateArray)
    }

    val notification = builder.build()
    notification.flags = notification.flags or Notification.FLAG_INSISTENT
    return notification
}

fun Context.getHideTimerPendingIntent(): PendingIntent {
    val intent = Intent(this, HideTimerReceiver::class.java)
    return PendingIntent.getBroadcast(this, TIMER_NOTIF_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}

fun Context.getHideAlarmPendingIntent(alarm: Alarm): PendingIntent {
    val intent = Intent(this, HideAlarmReceiver::class.java)
    intent.putExtra(ALARM_ID, alarm.id)
    return PendingIntent.getBroadcast(this, alarm.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}

@SuppressLint("NewApi")
fun Context.getAlarmNotification(pendingIntent: PendingIntent, alarm: Alarm): Notification {
    var soundUri = alarm.soundUri
    if (soundUri == SILENT) {
        soundUri = ""
    } else {
        grantReadUriPermission(soundUri)
    }

    val channelId = "simple_alarm_channel_$soundUri"
    val label = if (alarm.label.isNotEmpty()) alarm.label else getString(R.string.alarm)
    if (isOreoPlus()) {
        val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setLegacyStreamType(AudioManager.STREAM_ALARM)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val importance = NotificationManager.IMPORTANCE_HIGH
        NotificationChannel(channelId, label, importance).apply {
            setBypassDnd(true)
            enableLights(true)
            lightColor = getAdjustedPrimaryColor()
            enableVibration(alarm.vibrate)
            setSound(Uri.parse(soundUri), audioAttributes)
            notificationManager.createNotificationChannel(this)
        }
    }

    val builder = NotificationCompat.Builder(this)
            .setContentTitle(label)
            .setContentText(getFormattedTime(getPassedSeconds(), false, false))
            .setSmallIcon(R.drawable.ic_alarm_vector)
            .setContentIntent(pendingIntent)
            .setPriority(Notification.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_LIGHTS)
            .setAutoCancel(true)
            .setSound(Uri.parse(soundUri), AudioManager.STREAM_ALARM)
            .setChannelId(channelId)
            .addAction(R.drawable.ic_snooze_vector, getString(R.string.snooze), getSnoozePendingIntent(alarm))
            .addAction(R.drawable.ic_cross_vector, getString(R.string.dismiss), getHideAlarmPendingIntent(alarm))

    builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

    if (alarm.vibrate) {
        val vibrateArray = LongArray(2) { 500 }
        builder.setVibrate(vibrateArray)
    }

    val notification = builder.build()
    notification.flags = notification.flags or Notification.FLAG_INSISTENT
    return notification
}

fun Context.getSnoozePendingIntent(alarm: Alarm): PendingIntent {
    val snoozeClass = if (config.useSameSnooze) SnoozeService::class.java else SnoozeReminderActivity::class.java
    val intent = Intent(this, snoozeClass).setAction("Snooze")
    intent.putExtra(ALARM_ID, alarm.id)
    return if (config.useSameSnooze) {
        PendingIntent.getService(this, alarm.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    } else {
        PendingIntent.getActivity(this, alarm.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}

fun Context.getReminderActivityIntent(): PendingIntent {
    val intent = Intent(this, ReminderActivity::class.java)
    return PendingIntent.getActivity(this, REMINDER_ACTIVITY_INTENT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}

fun Context.checkAlarmsWithDeletedSoundUri(uri: String) {
    val defaultAlarmSound = getDefaultAlarmSound(ALARM_SOUND_TYPE_ALARM)
    dbHelper.getAlarmsWithUri(uri).forEach {
        it.soundTitle = defaultAlarmSound.title
        it.soundUri = defaultAlarmSound.uri
        dbHelper.updateAlarm(it)
    }
}
