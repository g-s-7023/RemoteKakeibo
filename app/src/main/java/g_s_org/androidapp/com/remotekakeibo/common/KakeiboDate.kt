package g_s_org.androidapp.com.remotekakeibo.common

import java.util.*

// Date object
class KakeiboDate(var year:Int = Constants.DEFAULT_YEAR, var month:Int = 1, var day:Int = 1, var dayOfWeek: Int = 1) :Cloneable{
    // set value from calendar
    fun setDate(cal:Calendar){
        year = cal.get(Calendar.YEAR)
        month = cal.get(Calendar.MONTH) + 1
        day = cal.get(Calendar.DAY_OF_MONTH)
        dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    }
    // set values and calculate dayOfWeek from date
    fun setDate(y: Int, m: Int, d: Int){
        year = y
        month = m
        day = d
        val cal = Calendar.getInstance()
        cal.set(y, m - 1, d)
        dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    }
    // set values
    fun setDate(y: Int, m: Int, d: Int, w: Int) {
        year = y
        month = m
        day = d
        dayOfWeek = w
    }
    // compare date
    fun isSameDate(date: KakeiboDate): Boolean {
        return (date.year == year && date.month == this.month && date.day == this.day)
    }
    // clone
    @Throws(CloneNotSupportedException::class)
    public override fun clone(): KakeiboDate {
        return super.clone() as KakeiboDate
    }
    // toString
    override fun toString(): String {
        return month.toString() + "/" + day.toString() + "(" + Constants.WEEKNAME[dayOfWeek - 1] + ")"
    }
}