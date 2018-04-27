package g_s_org.androidapp.com.remotekakeibo.view


import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.NumberPicker

import g_s_org.androidapp.com.remotekakeibo.R
import g_s_org.androidapp.com.remotekakeibo.common.Constants
import java.util.*


class DatePickerDialogFragment : DialogFragment() {
    lateinit var mCaller: Activity

    //===
    //=== life cycle
    //===
    override fun onAttach(context: Context?) {
        mCaller = context as Activity
        super.onAttach(context as Context)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // create view to display dialog
        val pickerView = View.inflate(mCaller, R.layout.fragment_yearmonth_dialog, null)
        // set picker
        val (year, month) = setPicker(pickerView, arguments.getInt("ORIGINAL_YEAR", Constants.DEFAULT_YEAR), arguments.getInt("ORIGINAL_MONTH", 1))
        // build dialog
        val builder = AlertDialog.Builder(mCaller)
                // set picker
                .setView(pickerView)
                .setTitle(R.string.title_yearandmonth)
                .setPositiveButton(R.string.bt_ok) { _, _ ->
                    // callback
                    val parent = parentFragment
                    if (parent is DatePickerCallback) {
                        parent.onDialogYearMonthSelected(year.value, month.value)
                    }
                    // close dialog
                    dismiss()
                }
                // set cancel button
                .setNegativeButton(getText(R.string.bt_cancel), null)
        // return alert dialog
        return builder.create()
    }
    //===
    //=== view setter
    //===
    fun setPicker(parent:View, year:Int, month:Int): Pair<NumberPicker, NumberPicker>{
        // set year picker
        val yearPicker = parent.findViewById(R.id.pk_year) as NumberPicker
        yearPicker.maxValue = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = Constants.DEFAULT_YEAR
        yearPicker.value = year
        // set month picker
        val monthPicker = parent.findViewById(R.id.pk_month) as NumberPicker
        monthPicker.maxValue = 12
        monthPicker.minValue = 1
        monthPicker.value = month
        return Pair(yearPicker, monthPicker)
    }
    //===
    //=== callback
    //===
    interface DatePickerCallback {
        fun onDialogYearMonthSelected(year: Int, month: Int)
    }
    //===
    //=== factory method
    //===
    companion object {
        fun newInstance(y: Int, m: Int): DatePickerDialogFragment {
            // fragment
            val dpFragment = DatePickerDialogFragment()
            // value to fragment
            val args = Bundle()
            // set year and month when clicked
            args.putInt("ORIGINAL_YEAR", y)
            args.putInt("ORIGINAL_MONTH", m)
            // set argument to fragment
            dpFragment.arguments = args
            // prohibit cancel with "return" button
            dpFragment.isCancelable = false
            // return fragment
            return dpFragment
        }
    }
}
