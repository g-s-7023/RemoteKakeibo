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

import g_s_org.androidapp.com.remotekakeibo.R
import g_s_org.androidapp.com.remotekakeibo.common.Constants


class DatePickerDialogFragment : DialogFragment() {
    lateinit var mCaller: Activity

    override fun onAttach(context: Context?) {
        mCaller = context as Activity
        super.onAttach(context as Context)
    }

    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
        // year and month when dialog is shown first
        val originalYear = arguments.getInt("ORIGINAL_YEAR", Constants.DEFAULT_YEAR)
        val originalMonth = arguments.getInt("ORIGINAL_MONTH", 1)

        // datePicker in dialog
        val picker = DatePicker(mCaller)
        // not show "day"
        val dayId = Resources.getSystem().getIdentifier("day", "id", "android")
        (picker.findViewById(dayId) as View).visibility = View.GONE
        // set initial value
        picker.updateDate(originalYear, originalMonth - 1, 1)
        // only show picker (deprecated)
        /*
        picker.spinnersShown = true
        picker.calendarViewShown = false
        */
        // build dialog
        val builder = AlertDialog.Builder(mCaller)
                // set picker
                .setView(picker)
                .setTitle(R.string.title_yearandmonth)
                .setPositiveButton(R.string.bt_ok) { dialog, which ->
                    val parent = parentFragment
                    // set callback
                    if (parent is DatePickerCallback) {
                        parent.onDialogYearMonthSelected(picker.year, picker.month + 1)
                    }
                    // close dialog
                    dismiss()
                }
                // set cancel button
                .setNegativeButton(getText(R.string.bt_cancel), null)
        // return alert dialog
        return builder.create()
    }

    // callback
    interface DatePickerCallback {
        fun onDialogYearMonthSelected(year: Int, month: Int)
    }

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
