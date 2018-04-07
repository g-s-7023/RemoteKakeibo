package g_s_org.androidapp.com.remotekakeibo.model

import g_s_org.androidapp.com.remotekakeibo.common.Constants
import java.time.DayOfWeek
import java.util.*

/**
 * Created by C170044 on 2018/04/04.
 */

//===
//=== get priceString of priceStack
//===

// 4/3作り直し
// 再帰を使って書き直したい

fun Deque<Char>.getPrice(): String {
    // string builder
    val priceString = StringBuilder()
    // temporary deque
    val tempStack = ArrayDeque(this)

    for (i in this.indices) {
        if (i != 0 && i % 3 == 0) {
            // insert ',' every three digits
            priceString.append(',')
        }
        priceString.append(tempStack.removeLast())
    }
    // add '￥' at the end
    priceString.append('￥')
    // reverse order
    priceString.reverse()
    return priceString.toString()
}

//===
//=== store price to stack
//===

// 4/3作り直し
// 再帰を使って書き直したい

fun Deque<Char>.setPrice(price: Int) {
    // change price to type array of char
    val priceArray = price.toString().toCharArray()
    // initialize deque
    this.clear()
    // push array to deque
    for (ch in priceArray) {
        this.addLast(ch)
    }
}

//===
//=== get array filled with days
//===
fun getCalendarArray(y: Int, m: Int): Array<Int> {
    // get first day of the month
    val firstDay = Calendar.getInstance()
    firstDay.set(y, m - 1, 1)
    // get last day of the month
    val lastDay = firstDay.getActualMaximum(Calendar.DAY_OF_MONTH)
    // get array filled with date of the month
    return fillCalendarArray(firstDay.get(Calendar.DAY_OF_WEEK) - 1, 1, lastDay, Array(Constants.WEEKS_OF_MONTH * Constants.DAYS_OF_WEEK, { 0 }))
}

// copy value of date to array
tailrec fun fillCalendarArray(i: Int, d: Int, last: Int, cal: Array<Int>): Array<Int> {
    if (i >= cal.size) return cal
    cal[i] = d
    when (d) {
        last -> return cal
        else -> return fillCalendarArray(i + 1, d + 1, last, cal)
    }
}

fun getCalendarTable(y: Int, m: Int): Array<Array<Int>> {
    // get first day of the month
    val firstDay = Calendar.getInstance()
    firstDay.set(y, m - 1, 1)
    // get last day of the month
    val lastDay = firstDay.getActualMaximum(Calendar.DAY_OF_MONTH)
    // get array filled with date of the month
    return fillCalendarTable(0, firstDay.get(Calendar.DAY_OF_WEEK) - 1, 1, lastDay, Array(Constants.WEEKS_OF_MONTH, { Array(Constants.DAYS_OF_WEEK, { 0 }) }))
}

// copy value of date to table
tailrec fun fillCalendarTable(row: Int, col: Int, day: Int, last: Int, cal: Array<Array<Int>>): Array<Array<Int>> {
    // copy day to table
    cal[row][col] = day
    return when {
        day == last ->{
            cal
        }
        col == Constants.DAYS_OF_WEEK - 1 -> {
            // move to the next week
            fillCalendarTable(row + 1, 0, day + 1, last, cal)
        }
        else -> {
            fillCalendarTable(row + 1, col, day + 1, last, cal)
        }
    }
}