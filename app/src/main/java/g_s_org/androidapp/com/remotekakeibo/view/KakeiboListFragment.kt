package g_s_org.androidapp.com.remotekakeibo.view

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.database.SQLException
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
import g_s_org.androidapp.com.remotekakeibo.model.*
import java.util.*


// ページ遷移


class KakeiboListFragment : Fragment(), DatePickerDialogFragment.DatePickerCallback, KakeiboListAdapter.OnKakeiboListItemClickListener {
    private lateinit var mCaller: FragmentActivity
    private var yearToList: Int = Constants.DEFAULT_YEAR
    private var monthToList: Int = 1

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (mCaller is FragmentActivity) {
            mCaller = context as FragmentActivity
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
        // show KakeiboList
        setViews()
    }

    fun initValues() {
        if (arguments != null && arguments.containsKey("LISTED_YEAR") && arguments.containsKey("LISTED_MONTH")) {
            yearToList = arguments.getInt("YEAR_TOLIST")
            monthToList = arguments.getInt("MONTH_TOLIST")
        } else {
            val cal = Calendar.getInstance()
            yearToList = cal.get(Calendar.YEAR)
            monthToList = cal.get(Calendar.MONTH)
        }
    }

    fun setListeners() {
        // year and month
        (mCaller.findViewById(R.id.ll_yearAndMonth) as LinearLayout).setOnClickListener { onYearOrMonthClicked(yearToList, monthToList) }
        // previous month
        (mCaller.findViewById(R.id.bt_previousmonth) as Button).setOnClickListener { onPreviousMonthClicked() }
        // next month
        (mCaller.findViewById(R.id.bt_nextmonth) as Button).setOnClickListener { onNextMonthClicked() }
        // new entry
        (mCaller.findViewById(R.id.bt_new) as Button).setOnClickListener { onNewEntryClicked(mCaller) }
    }

    fun setViews() {
        // list
        setKakeiboListView(yearToList, monthToList)
    }

    //===
    //=== listeners
    //===
    // year and month label
    fun onYearOrMonthClicked(y: Int, d: Int) {
        // show DatePickerDialogFragment
        DatePickerDialogFragment.newInstance(y, d).show(childFragmentManager, "dialog")
    }

    // previous month button
    fun onPreviousMonthClicked() {
        when (monthToList) {
            1 -> setKakeiboListView(yearToList - 1, 12)
            else -> setKakeiboListView(yearToList, monthToList - 1)
        }
    }

    // next month button
    fun onNextMonthClicked() {
        when (monthToList) {
            12 -> setKakeiboListView(yearToList + 1, 1)
            else -> setKakeiboListView(yearToList, monthToList + 1)
        }
    }

    // new entry button
    fun onNewEntryClicked(a: Activity) {
        if (a is FragmentToActivityInterection) {
            a.backPage()
        } else {
            throw UnsupportedOperationException("Listener is not implemented")
        }
    }

    // row
    override fun onItemClicked(item: KakeiboListItem) {
        // set fragment
        val fragment = KakeiboUpdateFragment.newInstance(item.id, item.date.year, item.date.month,
                item.date.day, item.date.dayOfWeek, item.category, item.type, item.price, item.detail, item.termsOfPayment)
        // move to UpdateFragment
        if (mCaller is FragmentToActivityInterection) {
            (mCaller as FragmentToActivityInterection).changePage(fragment)
        } else {
            throw UnsupportedOperationException("Listener is not implemented")
        }
    }

    //===
    //=== view and value setters
    //===
    fun setKakeiboListView(y: Int, m: Int) {
        // set value
        yearToList = y
        monthToList = m
        // read DB
        val cursor: Cursor? = KakeiboDBAccess().readAllKakeibo(mCaller, y, m)
        // getKakeiboList may throw SQLiteException
        cursor?.use {
            // get kakeibo list
            val (kList, totalIncome, totalExpence) = getKakeiboList(cursor, mutableListOf(), KakeiboDate(), 0, 0, 0, 0, cursor.moveToNext(), true)
            // set recycler view
            val listView: View = mCaller.findViewById(R.id.list) as View
            if (listView is RecyclerView) {
                listView.adapter = KakeiboListAdapter(kList, this)
            }
            // set other views
            // total income / total expense
            (mCaller.findViewById(R.id.tv_totalIncomeValue) as TextView).text = totalIncome.toString()
            (mCaller.findViewById(R.id.tv_totalExpenseValue) as TextView).text = totalExpence.toString()
            // year and month
            (mCaller.findViewById(R.id.tv_year_list) as TextView).text = y.toString()
            (mCaller.findViewById(R.id.tv_month_list) as TextView).text = m.toString()
        }
    }

    //===
    //=== callback from calendar dialog
    //===
    override fun onDialogYearMonthSelected(y: Int, m: Int) {
        setKakeiboListView(y, m)
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

    //===
    //=== business logic
    //===
    @Throws(SQLiteException::class)
    private tailrec fun getKakeiboList(c: Cursor, l: MutableList<KakeiboListItem>, previousDate: KakeiboDate, si: Int, se: Int, ti: Int, te: Int,
                                       isContinue: Boolean, isFirst: Boolean): Triple<MutableList<KakeiboListItem>, Int, Int> {
        if (!isContinue) {
            // after the last entry
            if (!isFirst) {
                // if list is not empty, set subtotal(income, expense)
                l.add(KakeiboListItem(true, previousDate, si, se))
            }
            // change the order to Descendant
            return Triple(l.reversed() as MutableList<KakeiboListItem>, ti, te)
        }
        // set current date
        val currentDate = KakeiboDate(c.getInt(c.getColumnIndex("year")),
                c.getInt(c.getColumnIndex("month")),
                c.getInt(c.getColumnIndex("day")),
                c.getInt(c.getColumnIndex("dayOfWeek")))
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
                l.add(KakeiboListItem(c.getInt(c.getColumnIndex("id")),
                        currentDate,
                        c.getString(c.getColumnIndex("category")),
                        type,
                        price,
                        c.getString(c.getColumnIndex("detail")),
                        c.getInt(c.getColumnIndex("termsOfPayment"))))
                return getKakeiboList(c, l, currentDate, si + income, se + expense, ti + income, te + expense, c.moveToNext(), false)
            }
        }
    }
}
