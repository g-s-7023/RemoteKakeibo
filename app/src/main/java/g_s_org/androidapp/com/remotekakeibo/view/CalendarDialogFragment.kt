package g_s_org.androidapp.com.remotekakeibo.view

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TextView

import g_s_org.androidapp.com.remotekakeibo.R
import g_s_org.androidapp.com.remotekakeibo.common.Constants


class CalendarDialogFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {
    lateinit var mCaller: Activity

    override fun onAttach(context: Context?) {
        mCaller = context as Activity
        super.onAttach(context as Context)
    }

    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
        // year and month when dialog is shown first
        val originalYear = arguments.getInt("ORIGINAL_YEAR", Constants.DEFAULT_YEAR)
        val originalMonth = arguments.getInt("ORIGINAL_MONTH", 1)
        val originalDay = arguments.getInt("ORIGINAL_DAY", 1)
        // create dialog
        val dialog = DatePickerDialog(mCaller, this, originalYear, originalMonth, originalDay)
        // set positive button invisible
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).visibility = GONE
        return dialog
    }

    // when data is set
    override fun onDateSet(dp: DatePicker?, y: Int, m: Int, d: Int) {
        // get parent fragment
        val parent = parentFragment
        if (parent is OnDialogInteractionListener) {
            // callback
            parent.onDialogDateSelected(y, m, d)
            // close dialog
            dismiss()
        } else {
            throw UnsupportedOperationException("listener is not implemented")
        }
    }

    interface OnDialogInteractionListener {
        fun onDialogDateSelected(year: Int, month: Int, day: Int)
    }

    companion object {
        fun newInstance(y: Int, m: Int, d: Int): CalendarDialogFragment {
            // fragment
            val fragment = CalendarDialogFragment()
            // set year, month, day when "date" is tapped
            val args = Bundle()
            args.putInt("ORIGINAL_YEAR", y)
            args.putInt("ORIGINAL_MONTH", m)
            args.putInt("ORIGINAL_DAY", d)
            // pass arguments to fragment
            fragment.arguments = args
            // prohibit cancel with "return" button
            fragment.isCancelable = false
            // return fragment
            return fragment
        }
    }

}
