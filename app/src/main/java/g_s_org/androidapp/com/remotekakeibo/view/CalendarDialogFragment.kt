package g_s_org.androidapp.com.remotekakeibo.view

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import g_s_org.androidapp.com.remotekakeibo.R
import g_s_org.androidapp.com.remotekakeibo.common.Constants.Companion.DAYS_OF_WEEK
import g_s_org.androidapp.com.remotekakeibo.common.Constants.Companion.WEEKS_OF_MONTH

fun newCalendarDialogFragment(bundle:Bundle , fragment:Fragment){
    val instance: CalendarDialogFragment  = CalendarDialogFragment()

}

class CalendarDialogFragment : DialogFragment() {
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
        val calendarView = (caller.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                .inflate(R.layout.fragment_calendar_dialog, null)
        // fill value and listener ot calendar

        // create dialog
        val dialog = AlertDialog.Builder(caller)
                .setView(calendarView)
                .setNegativeButton(R.string.bt_cancel, null)
                .create()
        // display no title
        dialog.requestWindowFeature(DialogFragment.STYLE_NO_FRAME)

        return dialog
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        setTargetFragment()
    }

    fun initValues(){
        // get arguments
        originalYear = arguments.getInt("YEAR_BEFORE")
        originalMonth = arguments.getInt("YEAR_BEFORE")
        originalDay = arguments.getInt("YEAR_BEFORE")
        // set current selected month
        currentYear = originalYear
        currentMonth = originalMonth
    }

    fun setListeners(view:View){
        // previous month button
        (view.findViewById(R.id.bt_previous_calendar) as Button).setOnClickListener { onPreviousMonthClicked(view) }
        // next month button
        (view.findViewById(R.id.bt_next_calendar) as Button).setOnClickListener { onNextMonthClicked(view) }
    }

    fun setCalendarView(view:View, year:Int, month:Int){
        // set button

    }

    //===
    //=== listener
    //===
    fun onPreviousMonthClicked(view:View){
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
    fun onNextMonthClicked(view:View){
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
    fun onDateClicked(view:View, day:int){
        // callback
        (activity as Callback).onDialogDateSelected(currentYear, currentMonth, clickedDay)
        // ダイアログを閉じる
        dismiss()
    }

    interface OnDialogInteractionListener{
        fun onDialogDateSelected(year:Int, month:Int, day:Int)
    }
}
