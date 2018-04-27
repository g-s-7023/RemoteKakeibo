package g_s_org.androidapp.com.remotekakeibo.view

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

import g_s_org.androidapp.com.remotekakeibo.R
import g_s_org.androidapp.com.remotekakeibo.common.Constants
import g_s_org.androidapp.com.remotekakeibo.common.KakeiboDate
import g_s_org.androidapp.com.remotekakeibo.common.KakeiboItemForSync
import g_s_org.androidapp.com.remotekakeibo.common.KakeiboListItem
import g_s_org.androidapp.com.remotekakeibo.model.*
import org.json.JSONArray
import java.util.*


class KakeiboListFragment : Fragment(), DatePickerDialogFragment.DatePickerCallback, KakeiboListAdapter.OnKakeiboListItemClickListener, HttpPostKakeibo.KakeiboSyncCallback {
    private lateinit var mCaller: FragmentActivity
    private var currentYearMonth = arrayOf(Constants.DEFAULT_YEAR, 1)

    //===
    //=== life cycle
    //===
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is FragmentActivity) {
            mCaller = context
        } else {
            throw UnsupportedOperationException("caller should be Fragment Activity")
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_kakeibolist, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        // initialize values and views
        initValues()
        // set listeners of buttons
        setListeners()
    }

    //===
    //=== initialize values
    //===
    private fun initValues() {
        if (arguments != null && arguments.containsKey("YEAR_TOLIST") && arguments.containsKey("MONTH_TOLIST")) {
            setKakeiboListView(arguments.getInt("YEAR_TOLIST"), arguments.getInt("MONTH_TOLIST"))
        } else {
            val cal = Calendar.getInstance()
            setKakeiboListView(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
        }
    }

    //===
    //=== set listeners of each view
    //===
    private fun setListeners() {
        // year and month
        (mCaller.findViewById(R.id.ll_yearAndMonth) as LinearLayout).setOnClickListener { onYearOrMonthClicked() }
        // previous month
        (mCaller.findViewById(R.id.bt_previousmonth) as Button).setOnClickListener { onPreviousMonthClicked() }
        // next month
        (mCaller.findViewById(R.id.bt_nextmonth) as Button).setOnClickListener { onNextMonthClicked() }
        // new entry
        (mCaller.findViewById(R.id.bt_new) as Button).setOnClickListener { onNewEntryClicked() }
        // synchronize
        //(mCaller.findViewById(R.id.bt_sync) as Button).setOnClickListener { onSyncClicked() }
    }

    //===
    //=== listeners and callback
    //===
    // year and month label
    private fun onYearOrMonthClicked() {
        // show DatePickerDialogFragment
        DatePickerDialogFragment.newInstance(currentYearMonth[Constants.CURRENT_YEAR], currentYearMonth[Constants.CURRENT_MONTH])
                .show(childFragmentManager, "dialog")
    }

    // previous month button
    private fun onPreviousMonthClicked() {
        when (currentYearMonth[Constants.CURRENT_MONTH]) {
            1 -> setKakeiboListView(currentYearMonth[Constants.CURRENT_YEAR] - 1, 12)
            else -> setKakeiboListView(currentYearMonth[Constants.CURRENT_YEAR], currentYearMonth[Constants.CURRENT_MONTH] - 1)
        }
    }

    // next month button
    private fun onNextMonthClicked() {
        when (currentYearMonth[Constants.CURRENT_MONTH]) {
            12 -> setKakeiboListView(currentYearMonth[Constants.CURRENT_YEAR + 1], 1)
            else -> setKakeiboListView(currentYearMonth[Constants.CURRENT_YEAR], currentYearMonth[Constants.CURRENT_MONTH] + 1)
        }
    }

    // new entry button
    private fun onNewEntryClicked() {
        openNewEntry(mCaller)
    }

    // sync button
    private fun onSyncClicked() {
        // read entries yet to be synchronized
        val cursor = KakeiboDBAccess(mCaller).readUnsynchronizedEntry()
        // make list of entries to be synchronized in json format
        val (jsonArray, ids) = getJsonArrayToSync(cursor)
        // upload json to server
        HttpPostKakeibo(mCaller.getString(R.string.kakeibo_url), jsonArray, ids, this).execute()
    }

    // row
    override fun onItemClicked(item: KakeiboListItem) {
        openEntryForUpdate(mCaller, item)
    }

    // callback from dialog
    override fun onDialogYearMonthSelected(y: Int, m: Int) {
        setKakeiboListView(y, m)
    }

    // callback after uploading
    override fun callback(result: JSONArray, ids:MutableList<Int>) {
        // get contentValues for insert and update
        val (cvForInsert, cvForUpdate) = getContentValuesFromServer(result, mutableListOf(), mutableListOf(), 0)
        // insert and update
        val k = KakeiboDBAccess(mCaller)
        k.syncInsert(cvForInsert)
        k.syncUpdate(cvForUpdate)
        // update isSynchronized
        k.setSynchronized(ids)
    }

    //===
    //=== view and value setter
    //===
    fun setKakeiboListView(y: Int, m: Int) {
        // set value
        currentYearMonth[Constants.CURRENT_YEAR] = y
        currentYearMonth[Constants.CURRENT_MONTH] = m
        // read DB
        val cursor: Cursor? = KakeiboDBAccess(mCaller).readKakeiboOfMonth(y, m)
        // getKakeiboList may throw SQLiteException
        cursor?.use {
            // get kakeibo list
            val (kList, totalIncome, totalExpense) = getKakeiboList(cursor, mutableListOf(), KakeiboDate(), 0, 0, 0, 0, cursor.moveToNext(), true)
            // set recycler view
            (mCaller.findViewById(R.id.list) as RecyclerView).adapter = KakeiboListAdapter(kList, this, mCaller)
            // set other views
            // total income / total expense
            (mCaller.findViewById(R.id.tv_totalIncomeValue) as TextView).text = totalIncome.toString()
            (mCaller.findViewById(R.id.tv_totalExpenseValue) as TextView).text = totalExpense.toString()
            // year and month
            (mCaller.findViewById(R.id.tv_year_list) as TextView).text = y.toString()
            (mCaller.findViewById(R.id.tv_month_list) as TextView).text = m.toString()
        }
    }

    //===
    //=== business logic
    //===
    // get list of kakeibo from DB
    @Throws(SQLiteException::class)
    private tailrec fun getKakeiboList(c: Cursor, l: MutableList<KakeiboListItem>, previousDate: KakeiboDate, si: Int, se: Int, ti: Int, te: Int,
                                       existNext: Boolean, isFirst: Boolean): Triple<MutableList<KakeiboListItem>, Int, Int> {
        if (!existNext) {
            // after the last entry
            if (!isFirst) {
                // if list is not empty, set subtotal(income, expense)
                l.add(KakeiboListItem(true, previousDate, si, se))
                // change the order to Descendant
                l.reverse()
            }
            return Triple(l, ti, te)
        }
        // set current date
        val currentDate = KakeiboDate(c.getInt(c.getColumnIndex("year")), c.getInt(c.getColumnIndex("month")),
                c.getInt(c.getColumnIndex("day")), c.getInt(c.getColumnIndex("dayOfWeek")))
        when (currentDate.isSameDate(previousDate) || isFirst) {
            false -> {
                // if the date changes, set subtotal
                l.add(KakeiboListItem(true, previousDate, si, se))
                return getKakeiboList(c, l, currentDate, 0, 0, ti, te, true, false)
            }
            true -> {
                // if the same date, set data row and move to next entry
                val type = c.getInt(c.getColumnIndex("type"))
                val price = c.getInt(c.getColumnIndex("price"))
                val income = if (type == Constants.INCOME) price else 0
                val expense = if (type == Constants.EXPENSE) price else 0
                l.add(KakeiboListItem(c.getInt(c.getColumnIndex("_id")), currentDate, c.getString(c.getColumnIndex("category")), type, price,
                        c.getString(c.getColumnIndex("detail")), c.getInt(c.getColumnIndex("termsOfPayment")), c.getInt(c.getColumnIndex("isSynchronized"))))
                return getKakeiboList(c, l, currentDate, si + income, se + expense, ti + income, te + expense, c.moveToNext(), false)
            }
        }
    }

    // open new entry
    fun openNewEntry(a: Activity) {
        if (a is FragmentToActivityInterection) {
            a.backFragment()
        } else {
            throw UnsupportedOperationException("Listener is not implemented")
        }
    }

    // row
    fun openEntryForUpdate(a: Activity, item: KakeiboListItem) {
        // set fragment
        val fragment = KakeiboUpdateFragment.newInstance(item.id, item.date.year, item.date.month,
                item.date.day, item.date.dayOfWeek, item.category, item.type, item.price, item.detail, item.termsOfPayment)
        // move to UpdateFragment
        if (a is FragmentToActivityInterection) {
            (a as FragmentToActivityInterection).changeFragment(fragment)
        } else {
            throw UnsupportedOperationException("Listener is not implemented")
        }
    }

    // read DB and make JSON array for send
    fun getJsonArrayToSync(c: Cursor?): Pair<JSONArray, MutableList<Int>> {
        val array = JSONArray()
        val ids = mutableListOf<Int>()
        if (c != null) {
            while (c.moveToNext()) {
                array.put(JsonKakeiboItem(c.getInt(c.getColumnIndex("_id")),
                        c.getInt(c.getColumnIndex("year")), c.getInt(c.getColumnIndex("month")),
                        c.getInt(c.getColumnIndex("day")), c.getInt(c.getColumnIndex("dayOfWeek")),
                        c.getString(c.getColumnIndex("category")), c.getInt(c.getColumnIndex("type")),
                        c.getInt(c.getColumnIndex("price")), c.getString(c.getColumnIndex("detail")),
                        c.getInt(c.getColumnIndex("termsOfPayment")), c.getInt(c.getColumnIndex("isDeleted"))).toJson())
                ids.add(c.getInt(c.getColumnIndex("_id")))
            }
        }
        return Pair(array, ids)
    }

    // make contentvalue from received JSON
    tailrec fun getContentValuesFromServer(result: JSONArray, cvForInsert: MutableList<ContentValues>, cvForUpdate: MutableList<KakeiboItemForSync>, pos: Int)
            : Pair<MutableList<ContentValues>, MutableList<KakeiboItemForSync>> {
        if (pos >= result.length()) {
            return Pair(cvForInsert, cvForUpdate)
        }
        val obj = result.getJSONObject(pos)
        if (obj != null) {
            // set values
            val cv = ContentValues()
            cv.put("category", obj.getString("category") ?: "")
            cv.put("detail", obj.getString("detail") ?: "")
            cv.put("kakeiboName", Constants.KAKEIBONAME_MINE)
            cv.put("year", obj.getInt("year"))
            cv.put("month", obj.getInt("month"))
            cv.put("day", obj.getInt("day"))
            cv.put("dayOfWeek", obj.getInt("dayOfWeek"))
            cv.put("price", obj.getInt("price"))
            cv.put("termsOfPayment", obj.getInt("termsOfPayment"))
            cv.put("type", obj.getInt("type"))
            cv.put("isSynchronized", Constants.TRUE)
            // set id and contentvalues
            val id = obj.getInt("id")
            when (id) {
                // no id means data created in server
                Constants.NO_ID -> cvForInsert.add(cv)
                // positive id means data created in client and updated in server
                else -> cvForUpdate.add(KakeiboItemForSync(id, cv))
            }
        }
        return getContentValuesFromServer(result, cvForInsert, cvForUpdate, pos + 1)
    }

    //===
    //=== factory method
    //===
    companion object {
        fun newInstance(y: Int, m: Int): KakeiboListFragment {
            // fragment
            val fragment = KakeiboListFragment()
            // set year and month when "date" is tapped
            val args = Bundle()
            args.putInt("YEAR_TOLIST", y)
            args.putInt("MONTH_TOLIST", m)
            // pass arguments to fragment
            fragment.arguments = args
            // return fragment
            return fragment
        }
    }
}
