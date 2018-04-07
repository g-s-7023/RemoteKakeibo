package g_s_org.androidapp.com.remotekakeibo.view

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.View
import android.widget.Button

import g_s_org.androidapp.com.remotekakeibo.R
import g_s_org.androidapp.com.remotekakeibo.model.KakeiboCalendar
import g_s_org.androidapp.com.remotekakeibo.model.getCalendarArray
import g_s_org.androidapp.com.remotekakeibo.model.getCalendarArray_
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import g_s_org.androidapp.com.remotekakeibo.common.Constants


class CalendarDialogFragment : DialogFragment() {
    // listener
    lateinit var mListener: OnDialogInteractionListener
    // date when dialog is shown
    var originalYear = 1900
    var originalMonth = 1
    var originalDay = 1
    // current selected month
    var currentYear = 1900
    var currentMonth = 1


    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
        // activity which call this fragment
        val caller = activity
        // view to display in dialog
        val calendarView = View.inflate(caller, R.layout.fragment_calendar_dialog, null)
        // fill value and listener of calendar

        // create dialog
        val dialog = AlertDialog.Builder(caller)
                .setView(calendarView)
                .setNegativeButton(R.string.bt_cancel, null)
                .create()
        // display no title
        dialog.requestWindowFeature(DialogFragment.STYLE_NO_FRAME)
        return dialog
    }

    fun initValues() {
        // get arguments
        originalYear = arguments.getInt("YEAR_BEFORE")
        originalMonth = arguments.getInt("YEAR_BEFORE")
        originalDay = arguments.getInt("YEAR_BEFORE")
        // set current selected month
        currentYear = originalYear
        currentMonth = originalMonth
    }

    fun setListeners(view: View) {
        // previous month button
        (view.findViewById(R.id.bt_previous_calendar) as Button).setOnClickListener { onPreviousMonthClicked(view) }
        // next month button
        (view.findViewById(R.id.bt_next_calendar) as Button).setOnClickListener { onNextMonthClicked(view) }
    }

    fun setCalendarView(view: View, year: Int, month: Int) {
        // get days
        val days = getCalendarArray(year, month)
        // calendar view to display
        val tl = view.findViewById(R.id.tl_days) as TableLayout
        // set year and month text
        (view.findViewById(R.id.tv_yearAndMonth_calendar) as TextView).text = getString(R.string.show_yearandmonth, year, month)
        // fill rows with title of the day of week


    }
    fun fillCalendarView(t: TableLayout, a: Array<Array<Int>>, row:Int, col:Int) {
        when (a[row][col]){
            0 -> setEmpty((t.getChildAt(row) as TableRow).getChildAt(col) as TextView)
            else -> setDay((t.getChildAt(row) as TableRow).getChildAt(col) as TextView, a[row][col])
        }
        return when{
            row == Constants.WEEKS_OF_MONTH -1 && col == Constants.DAYS_OF_WEEK - 1 -> { }
            row == Constants.WEEKS_OF_MONTH -1 -> {
                fillCalendarView(t, a, row + 1, 0)
            }
            else -> fillCalendarView(t, a, row, col + 1)
        }
    }
    // set empty string and no listener to textView
    fun setEmpty(t:TextView){
        t.setOnClickListener(null)
        t.text = ""
    }
    // set day and the corresponding listener to text view
    fun setDay(t:TextView, d:Int){
        t.setOnClickListener{onDateClicked(d)}
        t.text = d.toString()
    }
/*
    fun fillCalendarView(t: TableLayout, a: Array<Int>, r, c, i: Int): Int {
        when (i) {
            a.size -> return i
            else -> {
                (t.getChildAt(i/Constants.DAYS_OF_WEEK) as TableRow).getChildAt(i% Constants.DAYS_OF_WEEK)
            }
        }
    }
*/

    //===
    //=== listener
    //===
    fun onPreviousMonthClicked(view: View) {
        if (currentMonth == 1) {
            // back to December in the previous year
            currentYear--
            currentMonth = 12
        } else {
            // back to the previous month
            currentMonth--
        }
        // set the previous month to calendar
        setCalendarView(view, currentYear, currentMonth)
    }

    fun onNextMonthClicked(view: View) {
        // proceed to January in the next year
        if (currentMonth == 12) {
            // 現在が12月の場合、年を+1する
            currentYear++
            currentMonth = 1
        } else {
            // proceed to the next month
            currentMonth++
        }
        // set the next month to calendar
        setCalendarView(view, currentYear, currentMonth)
    }

    fun onDateClicked(day: Int) {
        // get parent fragment
        val parent = parentFragment
        if (parent is OnDialogInteractionListener) {
            // callback
            parent.onDialogDateSelected(currentYear, currentMonth, day)
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
            args.putInt("YEAR_BEFORE", y)
            args.putInt("MONTH_BEFORE", m)
            args.putInt("DAY_BEFORE", d)
            // pass arguments to fragment
            fragment.arguments = args
            // prohibit cancel with "return" button
            fragment.isCancelable = false
            return fragment
        }
    }
}
