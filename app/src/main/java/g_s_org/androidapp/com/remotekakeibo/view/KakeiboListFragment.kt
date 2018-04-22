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
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

import g_s_org.androidapp.com.remotekakeibo.R
import g_s_org.androidapp.com.remotekakeibo.common.Constants
import g_s_org.androidapp.com.remotekakeibo.common.KakeiboDate
import g_s_org.androidapp.com.remotekakeibo.common.KakeiboListItem
import g_s_org.androidapp.com.remotekakeibo.model.*
import org.json.JSONArray
import java.util.*


class KakeiboListFragment : Fragment(), DatePickerDialogFragment.DatePickerCallback, KakeiboListAdapter.OnKakeiboListItemClickListener {
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
        (mCaller.findViewById(R.id.bt_sync) as Button).setOnClickListener { onSyncClicked() }
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
        val cursor = KakeiboDBAccess().readUnsynchronizedEntry(mCaller)
        // できればそれらのidを取り出したい

        // make list in json
        val jsonArray = getJsonArrayToSync(cursor)
        // upload json to server
        HttpPostKakeibo(mCaller.getString(R.string.kakeibo_url), jsonArray, object : HttpPostKakeibo.KakeiboSyncCallback {
            // callback after uploading
            override fun callback(result: JSONArray) {
                // get contentvalues and id from json
                val cvAndId = getContentValuesAndId(result, 0, SparseArray(result.length()))
                // get id which already exist in Client's DB
                val idForUpdate =


                onUploadFinished(result)
            }
        }).execute()

        // unsyncだったエントリをsyncedに更新する

    }

    // row
    override fun onItemClicked(item: KakeiboListItem) {
        openEntryForUpdate(mCaller, item)
    }

    // callback from dialog
    override fun onDialogYearMonthSelected(y: Int, m: Int) {
        setKakeiboListView(y, m)
    }

    //===
    //=== view and value setter
    //===
    fun setKakeiboListView(y: Int, m: Int) {
        // set value
        currentYearMonth[Constants.CURRENT_YEAR] = y
        currentYearMonth[Constants.CURRENT_MONTH] = m
        // read DB
        val cursor: Cursor? = KakeiboDBAccess().readKakeiboOfMonth(mCaller, y, m)
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
                l.add(KakeiboListItem(c.getInt(c.getColumnIndex("_id")), currentDate, c.getString(c.getColumnIndex("category")),
                        type, price, c.getString(c.getColumnIndex("detail")), c.getInt(c.getColumnIndex("termsOfPayment"))))
                return getKakeiboList(c, l, currentDate, si + income, se + expense, ti + income, te + expense, c.moveToNext(), false)
            }
        }
    }

    // new entry button
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

    fun getJsonArrayToSync(c: Cursor?): JSONArray {
        val array = JSONArray()
        if (c != null) {
            while (c.moveToNext()) {
                array.put(JsonKakeiboItem(c.getInt(c.getColumnIndex("_id")),
                        c.getInt(c.getColumnIndex("year")), c.getInt(c.getColumnIndex("month")),
                        c.getInt(c.getColumnIndex("day")), c.getInt(c.getColumnIndex("dayOfWeek")),
                        c.getString(c.getColumnIndex("category")), c.getInt(c.getColumnIndex("type")),
                        c.getInt(c.getColumnIndex("price")), c.getString(c.getColumnIndex("detail")),
                        c.getInt(c.getColumnIndex("termsOfPayment"))).toJson())
            }
        }
        return array
    }

    // idで新規か更新かを確認して、contentValuesに追加
    /*
    fun getContentValues(result:JSONArray):ContentValues{
        val valuesForInsert:MutableList<ContentValues>
        val valuesForUpdate:MutableList<ContentValues>

        val trainObject = result.getJSONObject(i)

    }
    */
    // jsonをパースしてcontentvaluesとidのペアを返す
    tailrec fun getContentValuesAndId(result: JSONArray, pos: Int, map:SparseArray<ContentValues>):SparseArray<ContentValues>{
        when(pos){
            result.length() ->{
                return map
            }
            else ->{
                val cv = ContentValues()
                val obj = result.getJSONObject(pos)
                // set values
                cv.put("category", obj?.getString("category")?: "")
                cv.put("detail", obj?.getString("detail")?: "")
                cv.put("kakeiboName", Constants.KAKEIBONAME_MINE)
                cv.put("year", obj?.getInt("year")?: Constants.DEFAULT_YEAR)
                cv.put("month", obj?.getInt("month")?: 1)
                cv.put("day", obj?.getInt("day")?: 1)
                cv.put("dayOfWeek", obj?.getInt("dayOfWeek")?: 1)
                cv.put("price", obj?.getInt("price")?: 0)
                cv.put("termsOfPayment", obj?.getInt("termsOfPayment")?: Constants.CASH)
                cv.put("type", obj?.getInt("type")?: Constants.EXPENSE)
                cv.put("isSynchronized", Constants.TRUE)
                // set id and contentvalues
                map.put(obj?.getInt("id")?: -1, cv)
                return getContentValuesAndId(result, pos + 1, map)
            }
        }
    }

    fun getIdForUpdate(cvAndId:SparseArray<ContentValues>):MutableList<Int>{

    }
    // 全てのidについて端末のDBの存在チェックをかける
    // 存在チェックの結果に基づき、挿入用のcontentValuesと更新用のcontentValuesに分ける

    fun getContentValues(result: JSONArray, pos: Int, keysForUpdate: MutableList<Int>, cv: MutableList<ContentValues>)
            : Pair<MutableList<Int>, MutableList<ContentValues>> {
        if (pos == result.length()) {
            return Pair(keysForUpdate, cv)
        }
        val obj = result.getJSONObject(pos)
        val id = obj?.getInt("id") ?: -1
        // check if the id exists in Client's DB
        when(KakeiboDBAccess().existId())
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
