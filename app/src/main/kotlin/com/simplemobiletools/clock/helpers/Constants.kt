package com.simplemobiletools.clock.helpers

import com.simplemobiletools.clock.models.MyTimeZone
import java.util.*

// shared preferences
const val SHOW_SECONDS = "show_seconds"
const val SELECTED_TIME_ZONES = "selected_time_zones"
const val EDITED_TIME_ZONE_TITLES = "edited_time_zone_titles"
const val TIMER_SECONDS = "timer_seconds"
const val TIMER_VIBRATE = "timer_vibrate"
const val TIMER_SOUND_URI = "timer_sound_uri"
const val TIMER_SOUND_TITLE = "timer_sound_title"
const val TIMER_MAX_REMINDER_SECS = "timer_max_reminder_secs"
const val ALARM_MAX_REMINDER_SECS = "alarm_max_reminder_secs"
const val USE_TEXT_SHADOW = "use_text_shadow"
const val INCREASE_VOLUME_GRADUALLY = "increase_volume_gradually"

const val TABS_COUNT = 4
const val EDITED_TIME_ZONE_SEPARATOR = ":"
const val ALARM_ID = "alarm_id"
const val DEFAULT_ALARM_MINUTES = 480
const val DEFAULT_MAX_ALARM_REMINDER_SECS = 300
const val DEFAULT_MAX_TIMER_REMINDER_SECS = 60

const val PICK_AUDIO_FILE_INTENT_ID = 9994
const val REMINDER_ACTIVITY_INTENT_ID = 9995
const val OPEN_ALARMS_TAB_INTENT_ID = 9996
const val UPDATE_WIDGET_INTENT_ID = 9997
const val OPEN_APP_INTENT_ID = 9998
const val ALARM_NOTIF_ID = 9998
const val TIMER_NOTIF_ID = 9999

const val OPEN_TAB = "open_tab"
const val TAB_CLOCK = 0
const val TAB_ALARM = 1
const val TAB_STOPWATCH = 2
const val TAB_TIMER = 3

const val SORT_BY_LAP = 1
const val SORT_BY_LAP_TIME = 2
const val SORT_BY_TOTAL_TIME = 4

//Broadcast Intents

const val ALARM_GOING_TO_RING = "com.simplemobiletools.ALARM_GOING_TO_RING"
const val ALARM_DISABLED =      "com.simplemobiletools.ALARM_DISABLED"
const val ALARM_SNOOZED =       "com.simplemobiletools.ALARM_SNOOZED"
const val ALARM_IS_ACTIVE =     "com.simplemobiletools.ALARM_IS_ACTIVE"
const val NEW_ALARM =           "com.simplemobiletools.NEW_ALARM"
const val ALARM_CANCELED =       "com.simplemobiletools.ALARM_CANCELED"
fun getDefaultTimeZoneTitle(id: Int) = getAllTimeZones().firstOrNull { it.id == id }?.title ?: ""

fun getMSTillNextMinute(): Long {
    val calendar = Calendar.getInstance()
    return 60000L - calendar.get(Calendar.MILLISECOND) - calendar.get(Calendar.SECOND) * 1000
}

fun getPassedSeconds(): Int {
    val calendar = Calendar.getInstance()
    val isDaylightSavingActive = TimeZone.getDefault().inDaylightTime(Date())
    var offset = calendar.timeZone.rawOffset
    if (isDaylightSavingActive) {
        offset += TimeZone.getDefault().dstSavings
    }
    return ((calendar.timeInMillis + offset) / 1000).toInt()
}

fun formatTime(showSeconds: Boolean, use24HourFormat: Boolean, hours: Int, minutes: Int, seconds: Int): String {
    val hoursFormat = if (use24HourFormat) "%02d" else "%01d"
    var format = "$hoursFormat:%02d"

    return if (showSeconds) {
        format += ":%02d"
        String.format(format, hours, minutes, seconds)
    } else {
        String.format(format, hours, minutes)
    }
}

fun getAllTimeZones() = arrayListOf(
        MyTimeZone(1, "GMT-11:00 Midway", "Pacific/Midway"),
        MyTimeZone(2, "GMT-10:00 Honolulu", "Pacific/Honolulu"),
        MyTimeZone(3, "GMT-09:00 Anchorage", "America/Anchorage"),
        MyTimeZone(4, "GMT-08:00 Los Angeles", "America/Los_Angeles"),
        MyTimeZone(5, "GMT-08:00 Tijuana", "America/Tijuana"),
        MyTimeZone(6, "GMT-07:00 Phoenix", "America/Phoenix"),
        MyTimeZone(7, "GMT-07:00 Chihuahua", "America/Chihuahua"),
        MyTimeZone(8, "GMT-07:00 Denver", "America/Denver"),
        MyTimeZone(9, "GMT-06:00 Costa Rica", "America/Costa_Rica"),
        MyTimeZone(10, "GMT-06:00 Chicago", "America/Chicago"),
        MyTimeZone(11, "GMT-06:00 Mexico City", "America/Mexico_City"),
        MyTimeZone(12, "GMT-06:00 Regina", "America/Regina"),
        MyTimeZone(13, "GMT-05:00 Bogota", "America/Bogota"),
        MyTimeZone(14, "GMT-05:00 New York", "America/New_York"),
        MyTimeZone(15, "GMT-04:30 Caracas", "America/Caracas"),
        MyTimeZone(16, "GMT-04:00 Barbados", "America/Barbados"),
        MyTimeZone(17, "GMT-04:00 Halifax", "America/Halifax"),
        MyTimeZone(18, "GMT-04:00 Manaus", "America/Manaus"),
        MyTimeZone(19, "GMT-03:30 St. John's", "America/St_Johns"),
        MyTimeZone(20, "GMT-03:00 Santiago", "America/Santiago"),
        MyTimeZone(21, "GMT-03:00 Recife", "America/Recife"),
        MyTimeZone(22, "GMT-03:00 Sao Paulo", "America/Sao_Paulo"),
        MyTimeZone(23, "GMT-03:00 Buenos Aires", "America/Buenos_Aires"),
        MyTimeZone(24, "GMT-03:00 Nuuk", "America/Godthab"),
        MyTimeZone(25, "GMT-03:00 Montevideo", "America/Montevideo"),
        MyTimeZone(26, "GMT-02:00 South Georgia", "Atlantic/South_Georgia"),
        MyTimeZone(27, "GMT-01:00 Azores", "Atlantic/Azores"),
        MyTimeZone(28, "GMT-01:00 Cape Verde", "Atlantic/Cape_Verde"),
        MyTimeZone(29, "GMT+00:00 Casablanca", "Africa/Casablanca"),
        MyTimeZone(30, "GMT+00:00 Greenwich Mean Time", "Etc/Greenwich"),
        MyTimeZone(31, "GMT+01:00 Amsterdam", "Europe/Amsterdam"),
        MyTimeZone(32, "GMT+01:00 Belgrade", "Europe/Belgrade"),
        MyTimeZone(33, "GMT+01:00 Brussels", "Europe/Brussels"),
        MyTimeZone(34, "GMT+01:00 Madrid", "Europe/Madrid"),
        MyTimeZone(35, "GMT+01:00 Sarajevo", "Europe/Sarajevo"),
        MyTimeZone(36, "GMT+01:00 Brazzaville", "Africa/Brazzaville"),
        MyTimeZone(37, "GMT+02:00 Windhoek", "Africa/Windhoek"),
        MyTimeZone(38, "GMT+02:00 Amman", "Asia/Amman"),
        MyTimeZone(39, "GMT+02:00 Athens", "Europe/Athens"),
        MyTimeZone(40, "GMT+02:00 Istanbul", "Europe/Istanbul"),
        MyTimeZone(41, "GMT+02:00 Beirut", "Asia/Beirut"),
        MyTimeZone(42, "GMT+02:00 Cairo", "Africa/Cairo"),
        MyTimeZone(43, "GMT+02:00 Helsinki", "Europe/Helsinki"),
        MyTimeZone(44, "GMT+02:00 Jerusalem", "Asia/Jerusalem"),
        MyTimeZone(45, "GMT+02:00 Harare", "Africa/Harare"),
        MyTimeZone(46, "GMT+03:00 Minsk", "Europe/Minsk"),
        MyTimeZone(47, "GMT+03:00 Baghdad", "Asia/Baghdad"),
        MyTimeZone(48, "GMT+03:00 Moscow", "Europe/Moscow"),
        MyTimeZone(49, "GMT+03:00 Kuwait", "Asia/Kuwait"),
        MyTimeZone(50, "GMT+03:00 Nairobi", "Africa/Nairobi"),
        MyTimeZone(51, "GMT+03:30 Tehran", "Asia/Tehran"),
        MyTimeZone(52, "GMT+04:00 Baku", "Asia/Baku"),
        MyTimeZone(53, "GMT+04:00 Tbilisi", "Asia/Tbilisi"),
        MyTimeZone(54, "GMT+04:00 Yerevan", "Asia/Yerevan"),
        MyTimeZone(55, "GMT+04:00 Dubai", "Asia/Dubai"),
        MyTimeZone(56, "GMT+04:30 Kabul", "Asia/Kabul"),
        MyTimeZone(57, "GMT+05:00 Karachi", "Asia/Karachi"),
        MyTimeZone(58, "GMT+05:00 Oral", "Asia/Oral"),
        MyTimeZone(59, "GMT+05:00 Yekaterinburg", "Asia/Yekaterinburg"),
        MyTimeZone(60, "GMT+05:30 Kolkata", "Asia/Kolkata"),
        MyTimeZone(61, "GMT+05:30 Colombo", "Asia/Colombo"),
        MyTimeZone(62, "GMT+05:45 Kathmandu", "Asia/Kathmandu"),
        MyTimeZone(63, "GMT+06:00 Almaty", "Asia/Almaty"),
        MyTimeZone(64, "GMT+06:30 Rangoon", "Asia/Rangoon"),
        MyTimeZone(65, "GMT+07:00 Krasnoyarsk", "Asia/Krasnoyarsk"),
        MyTimeZone(66, "GMT+07:00 Bangkok", "Asia/Bangkok"),
        MyTimeZone(67, "GMT+07:00 Jakarta", "Asia/Jakarta"),
        MyTimeZone(68, "GMT+08:00 Shanghai", "Asia/Shanghai"),
        MyTimeZone(69, "GMT+08:00 Hong Kong", "Asia/Hong_Kong"),
        MyTimeZone(70, "GMT+08:00 Irkutsk", "Asia/Irkutsk"),
        MyTimeZone(71, "GMT+08:00 Kuala Lumpur", "Asia/Kuala_Lumpur"),
        MyTimeZone(72, "GMT+08:00 Perth", "Australia/Perth"),
        MyTimeZone(73, "GMT+08:00 Taipei", "Asia/Taipei"),
        MyTimeZone(74, "GMT+09:00 Seoul", "Asia/Seoul"),
        MyTimeZone(75, "GMT+09:00 Tokyo", "Asia/Tokyo"),
        MyTimeZone(76, "GMT+09:00 Yakutsk", "Asia/Yakutsk"),
        MyTimeZone(77, "GMT+09:30 Darwin", "Australia/Darwin"),
        MyTimeZone(78, "GMT+10:00 Brisbane", "Australia/Brisbane"),
        MyTimeZone(79, "GMT+10:00 Vladivostok", "Asia/Vladivostok"),
        MyTimeZone(80, "GMT+10:00 Guam", "Pacific/Guam"),
        MyTimeZone(81, "GMT+10:00 Magadan", "Asia/Magadan"),
        MyTimeZone(82, "GMT+10:30 Adelaide", "Australia/Adelaide"),
        MyTimeZone(83, "GMT+11:00 Hobart", "Australia/Hobart"),
        MyTimeZone(84, "GMT+11:00 Sydney", "Australia/Sydney"),
        MyTimeZone(85, "GMT+11:00 Noumea", "Pacific/Noumea"),
        MyTimeZone(86, "GMT+12:00 Majuro", "Pacific/Majuro"),
        MyTimeZone(87, "GMT+12:00 Fiji", "Pacific/Fiji"),
        MyTimeZone(88, "GMT+13:00 Auckland", "Pacific/Auckland"),
        MyTimeZone(89, "GMT+13:00 Tongatapu", "Pacific/Tongatapu")
)
