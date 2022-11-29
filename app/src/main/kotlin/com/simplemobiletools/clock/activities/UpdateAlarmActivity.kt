package com.simplemobiletools.clock.activities
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.simplemobiletools.clock.models.Alarm
import kotlin.math.abs
import android.content.Intent
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.DEFAULT_ALARM_MINUTES
import java.util.*
import kotlin.math.min
import kotlin.math.pow


// call using adb with
/*
adb shell am start -a android.intent.action.SEND -t text/plain  --ei ALARM_ID "1" --es LABEL "NEW_TEST_INTENT_LABEL_2" --ei "HOURS" 25 --ei "MINUTES" 65 com.simplemobiletools.clock.debug

 */
class UpdateAlarmActivity : SimpleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when {
            intent?.action == Intent.ACTION_SEND -> {
                var mode = intent.getStringExtra("MODE")
                when (mode){
                    "UPDATE" -> {
                        var alarmId = intent.getIntExtra("ALARM_ID", -1)
                        if (alarmId == -1){
                            Toast.makeText(applicationContext, "Error, please verify payload", Toast.LENGTH_SHORT).show()
                            finish()
                            return
                        }
                        var alarm: Alarm? = applicationContext.dbHelper?.getAlarmWithId(alarmId) ?: return

                        var cachedAlarmTime = alarm!!.timeInMinutes
                        var alarmChildren = applicationContext.dbHelper.getChildAlarms(alarmId)
                        var updateChildren = intent.getBooleanExtra("UPDATE_CHILDREN", false)
                        var label = intent.getStringExtra("LABEL")
                        var minutes = intent.getIntExtra("MINUTES", -1)
                        var hours = intent.getIntExtra("HOURS", -1)
                        var days = intent.getIntExtra("DAYS", -1)

                        if (!label.isNullOrBlank()) alarm!!.label = label

                        if(minutes > 60){
                            hours += (minutes / 60).toInt()
                            minutes %= 60
                        }

                        if (minutes != -1 && hours != -1) {
                            alarm!!.timeInMinutes = hours * 60 + minutes
                        } else if (hours != -1 && minutes == -1) {
                            alarm!!.timeInMinutes = hours * 60 + (alarm.timeInMinutes % 60)
                        } else if (hours == -1 && minutes != -1) {
                            alarm!!.timeInMinutes = (alarm.timeInMinutes / 60) + minutes
                        }

                        if (updateChildren) {
                            alarmChildren.forEach {
                                var diff = abs(cachedAlarmTime - alarm.timeInMinutes)
                                if (alarm.timeInMinutes > cachedAlarmTime)
                                    it.timeInMinutes = it.timeInMinutes + diff
                                else
                                    it.timeInMinutes = it.timeInMinutes - diff
                            }
                        }


                        if (days != -1) {
                            //todo
                        }

                        if (alarm != null) {
                            if (applicationContext.dbHelper.updateAlarm(alarm))
                                Toast.makeText(applicationContext, applicationContext.getString(com.simplemobiletools.clock.R.string.intent_alarm_updated, label), Toast.LENGTH_SHORT).show()
                            else
                                Toast.makeText(applicationContext, applicationContext.getString(com.simplemobiletools.clock.R.string.intent_alarm_updated_error, label), Toast.LENGTH_SHORT).show()
                        }
                        finish()
                        super.onBackPressed()
                    }

                    "INSERT" -> {
                        var label = intent.getStringExtra("LABEL")
                        var minutes = intent.getIntExtra("MINUTES", -1)
                        var hours = intent.getIntExtra("HOURS", -1)
                        var days = intent.getIntExtra("DAYS", -1)

                        if (label.isNullOrBlank() || minutes == -1 || hours == -1 ){//|| days == -1){
                            Toast.makeText(applicationContext, applicationContext.getString(R.string.must_specify_all_parameters), Toast.LENGTH_LONG).show()
                            finish()
                            return
                        }

                        var timeInMinutes = 0
                        if(minutes > 60){
                            hours += (minutes / 60).toInt()
                            minutes %= 60
                            timeInMinutes = hours*60+minutes
                        }
                        val newAlarm = applicationContext.createNewAlarm(timeInMinutes, 31)
                        newAlarm.isEnabled = true
                        applicationContext.dbHelper.insertAlarm(newAlarm)
                        applicationContext.scheduleNextAlarm(newAlarm, true)
                    }

                    "DELETE" ->{
                        //todo
                    }
                    "ENABLE" -> {
                        var alarmId = intent.getIntExtra("ALARM_ID", -1)
                        if (alarmId == -1){
                            Toast.makeText(applicationContext, "Error, please verify payload", Toast.LENGTH_SHORT).show()
                            finish()
                            return
                        }
                        var alarm: Alarm? = applicationContext.dbHelper?.getAlarmWithId(alarmId) ?: return
                        alarm!!.isEnabled = true
                        applicationContext.dbHelper.updateAlarmEnabledState(alarm.id, alarm.isEnabled)
                        applicationContext.cancelAlarmClock(alarm)
                    }
                    "DISABLE" -> {
                        var alarmId = intent.getIntExtra("ALARM_ID", -1)
                        if (alarmId == -1){
                            Toast.makeText(applicationContext, "Error, please verify payload", Toast.LENGTH_SHORT).show()
                            finish()
                            return
                        }

                        var alarm: Alarm? = applicationContext.dbHelper?.getAlarmWithId(alarmId) ?: return
                        alarm!!.isEnabled = false
                        applicationContext.dbHelper.updateAlarmEnabledState(alarm.id, alarm.isEnabled)
                        applicationContext.cancelAlarmClock(alarm)
                    }
                    else ->{
                        Toast.makeText(applicationContext, applicationContext.getString(R.string.must_specify_mode), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
        finish()
        super.onBackPressed()
    }
}
