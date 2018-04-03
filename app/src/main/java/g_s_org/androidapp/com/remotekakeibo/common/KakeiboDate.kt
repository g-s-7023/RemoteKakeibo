package g_s_org.androidapp.com.remotekakeibo.common

import java.time.DayOfWeek
import java.util.*

/**
 * Created by C170044 on 2018/04/03.
 */
class KakeiboDate(var year:Int = 1900, var month:Int = 1, var day:Int = 1, var dayOfWeek: Int = 1) :Cloneable{
    constructor(cal: Calendar):
            this(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.DAY_OF_WEEK))

    // calculate dayOfWeek from date
    fun setDayOfWeek(y: Int, m: Int, d: Int){
        val cal = Calendar.getInstance()
        cal.set(y, m, d)
        dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    }
    // compare date
    fun compareDate(date: KakeiboDate): Boolean {
        return (date.year == year && date.month == this.month && date.day == this.day)
    }
    // clone
    @Throws(CloneNotSupportedException::class)
    public override fun clone(): KakeiboDate {
        return super.clone() as KakeiboDate
    }
    // toString
    override fun toString(): String {
        return year.toString() + "/" + month.toString() + "/" + day.toString() + "(" + Constants.WEEKNAME[dayOfWeek - 1] + ")"
    }
}