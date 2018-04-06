package g_s_org.androidapp.com.remotekakeibo.model

import g_s_org.androidapp.com.remotekakeibo.common.Constants
import java.util.*

class KakeiboCalendar {

    fun getCalendarArray_(y: Int, m: Int): Array<Int> {
        // get first day of the month
        val firstDay = Calendar.getInstance()
        firstDay.set(y, m - 1, 1)
        // get last day of the month
        val lastDay = firstDay.getActualMaximum(Calendar.DAY_OF_MONTH)
        // get array filled with date of the month
        return getCalendarArray(firstDay.get(Calendar.DAY_OF_WEEK) - 1, 1, lastDay,Array(Constants.WEEKS_OF_MONTH * Constants.DAYS_OF_WEEK, { 0 }))
    }

    // copy value of date to array
    tailrec fun getCalendarArray(i: Int, d: Int, last:Int, cal: Array<Int>): Array<Int> {
        if (i >= cal.size) return cal
        cal[i] = d
        when(d){
            last-> return cal
            else-> return getCalendarArray(i + 1, d + 1, last, cal)
        }
    }
}