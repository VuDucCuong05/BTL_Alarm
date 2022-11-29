package com.simplemobiletools.clock.dialogs

import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_my_time_picker.view.*

class UpdateChildAlarmDialog(val activity: SimpleActivity, val callback: (result: Int) -> Unit) {

    init {
        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.yes, { dialog, which -> dialogConfirmed() })
                .setNegativeButton(R.string.no, null)
                .setTitle(R.string.update_child_alarm_title)
                .setMessage(R.string.update_child_alarm_info)
                .create().show()
    }

    private fun dialogConfirmed() {
            callback(1)
    }
}
