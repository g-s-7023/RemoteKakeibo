package g_s_org.androidapp.com.remotekakeibo.common

/**
 * Created by C170044 on 2018/04/02.
 */
class Constants{
    companion object {
        //===
        //=== max digits allowed to input
        //===
        const val MAXDIGITS = 8
        //===
        //=== row number of kakeibo
        //===
        const val KAKEIBO_NAME = 1
        const val KAKEIBO_YEAR = 2
        const val KAKEIBO_MONTH = 3
        const val KAKEIBO_DAY = 4
        const val KAKEIBO_DAYOFWEEK = 5
        const val KAKEIBO_CATEGORY = 6
        const val KAKEIBO_TYPE = 7
        const val KAKEIBO_PRICE = 8
        const val KAKEIBO_TERMSOFPAYMENT = 9
        const val KAKEIBO_DETAIL = 10
        const val KAKEIBO_ISDELETED = 11
        const val KAKEIBO_ISSYNCHRONIZED = 12
        const val KAKEIBO_LASTUPDATEDDATE = 13

        //===
        //=== index of type
        //===
        const val INCOME = 0
        const val EXPENSE = 1
        //===
        //=== index of termsOfPayment
        //===
        const val CASH = 0
        const val CARD = 1
        //===
        //=== index of inputTarget
        //===
        const val CATEGORY = 0
        const val DETAIL = 1
        //===
        //=== flag (isSynchronized、isDeleted)
        //===
        const val FALSE = 0
        const val TRUE = 1
        //===
        //=== name of DB
        //===
        const val KAKEIBONAME_MINE = "MyKakeibo"
        //===
        //=== default position of cursor
        //===
        const val DEFAULTPOSITION = -1
        //===
        //=== reset date or not
        //===
        const val DATE_RESET = true
        const val NO_DATE_RESET = false
        //===
        //=== return value of setContentValues
        //===
        const val SUCCESS = 0
        // 未入力項目あり
        const val NOT_FILLED = 1
        //===
        //=== day of week
        //===
        val WEEKNAME = arrayOf("日", "月", "火", "水", "木", "金", "土")
        //===
        //=== max number of weeks in a month
        //===
        const val DAYS_OF_WEEK = 7
        //===
        //=== max number of weeks in a month
        //===
        const val WEEKS_OF_MONTH = 6
        //===
        //=== format of date
        //===
        const val sdf = "yyyy/MM/dd/kk:mm:ss"
        //===
        //=== no id
        //===
        const val NO_ID = -1

    }
}