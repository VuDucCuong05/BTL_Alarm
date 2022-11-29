package com.simplemobiletools.clock.adapters

import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.getFormattedTime
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.commons.extensions.getDialogTheme
import kotlinx.android.synthetic.main.item_child_alarm.view.*

class ChildAlarmsAdapter (var items: List<Alarm>, var shownItems: List<Int>, val parentAlarm: Alarm, val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var alarmsToDelete = ArrayList<Alarm>()

    override fun getItemCount(): Int {
        return shownItems.size
    }

    fun updateItems(newItems: List<Alarm>, newShownItems: List<Int>) {
        items = newItems
        shownItems = newShownItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder (LayoutInflater.from(context).inflate(R.layout.item_child_alarm, parent, false))
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var childAlarm : Alarm = items.get(shownItems.get(position))
        var time = context.getFormattedTime(childAlarm.timeInMinutes *60, false, true)
        holder.itemView.tv_child_alarm_info.setTextColor(context!!.config.textColor)
        holder.itemView.tv_child_alarm_info.setText(time)
        holder.itemView.switch_toggle_silent_alarm.isChecked = childAlarm.isEnabled
        if(childAlarm.id != 0)
            holder.itemView.tv_child_alarm_id.text="ID. "+childAlarm.id
        else
            holder.itemView.tv_child_alarm_id.text= context.resources.getString(R.string.save_for_id)
        val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            childAlarm.timeInMinutes = hourOfDay * 60 + minute
            var time = context.getFormattedTime(childAlarm.timeInMinutes *60, false, true)
            holder.itemView.tv_child_alarm_info.setText(time)
        }

        holder.itemView.tv_child_alarm_info.setOnClickListener {
            if(parentAlarm.timeInMinutes != childAlarm.timeInMinutes)
                TimePickerDialog(context, context.getDialogTheme(), timeSetListener, childAlarm.timeInMinutes / 60, childAlarm.timeInMinutes % 60, context.config.use24HourFormat).show()
            else
                TimePickerDialog(context, context.getDialogTheme(), timeSetListener, parentAlarm.timeInMinutes / 60, parentAlarm.timeInMinutes % 60, context.config.use24HourFormat).show()
        }

        holder.itemView.ib_delete_child_alarm.setOnClickListener {
            items.get(shownItems.get(position)).isEnabled = false
            alarmsToDelete.add(items.get(shownItems.get(position)))
            updateItems(items, shownItems.minus(position))
            notifyDataSetChanged()
        }


        holder.itemView.switch_toggle_silent_alarm.setOnClickListener {
            items[position].isEnabled  = !items[position].isEnabled
        }


    }

    fun addItem(alarm: Alarm) {
        updateItems(items.plus(alarm), shownItems.plus(shownItems.lastIndex+1))
        //updateItems(items.plus(alarm), items.plus(alarm).indices.toList())
    }

}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view)