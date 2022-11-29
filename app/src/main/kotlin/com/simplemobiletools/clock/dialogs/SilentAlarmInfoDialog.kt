package com.simplemobiletools.clock.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.simplemobiletools.clock.R

class SilentAlarmInfoDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.silent_alarm_dialog_title)
            builder.setMessage(R.string.silent_alarm_info)
                    .setNeutralButton(R.string.ok, DialogInterface.OnClickListener{ dialog: DialogInterface?, which: Int ->  })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
