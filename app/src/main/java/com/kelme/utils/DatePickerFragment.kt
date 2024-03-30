package com.kelme.utils

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.kelme.event.DateEvent
import com.kelme.event.DateEventTo
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * Created by Amit Gupta on 12-07-2021.
 */
class DatePickerFragment(var dateIdentifier: String) : DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(requireContext(), this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        val newMonth = manageMonth(month)
        val newDate =  manageDate(day)
        if(dateIdentifier=="datePickerFrom") {
            EventBus.getDefault().post(DateEvent("$newDate-$newMonth-$year"))
        }else{
            EventBus.getDefault().post(DateEventTo("$newDate-$newMonth-$year"))
        }
    }

    private fun manageDate(date:Int): String{
        if (date<10){
            return "0$date"
        }else{
            return "$date"
        }

    }

    private fun manageMonth(month:Int): String{
        val month1 = month +1
        if (month1<10){
            return "0$month1"
        }else{
            return "$month1"
        }
    }
}