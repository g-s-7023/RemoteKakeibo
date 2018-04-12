package g_s_org.androidapp.com.remotekakeibo.view

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import g_s_org.androidapp.com.remotekakeibo.common.Constants
import g_s_org.androidapp.com.remotekakeibo.common.Constants.Companion.MAXDIGITS
import g_s_org.androidapp.com.remotekakeibo.common.Constants.Companion.sdf
import g_s_org.androidapp.com.remotekakeibo.model.KakeiboDate
import g_s_org.androidapp.com.remotekakeibo.dbaccess.DetailHistoryAccess
import g_s_org.androidapp.com.remotekakeibo.model.getPrice
import g_s_org.androidapp.com.remotekakeibo.R
import java.text.SimpleDateFormat

import java.util.*

abstract class KakeiboInputFragment : Fragment(), _CalendarDialogFragment.OnDialogInteractionListener {
    // caller of this activity
    protected lateinit var mCaller: FragmentActivity
    // buffer of input price
    protected val priceStack: Deque<Char> = ArrayDeque()
    // selected year, month, and day
    protected var selectedDate: KakeiboDate = KakeiboDate()
    // condition
    protected var condition: Array<Int> = arrayOf(3)

    override fun onAttach(context: Context?) {
        if (context is FragmentActivity) mCaller = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_input, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    //===
    //=== initialize values
    //===
    abstract fun initValues()

    //===
    //=== set values to each view
    //===
    abstract fun setViews(a: FragmentActivity)

    //===
    //=== set listener of each view
    //===
    open fun setListeners(a: FragmentActivity) {
        // list
        (a.findViewById(R.id.lv_categoryAndDetail) as ListView).setOnItemClickListener { parent, _, position, _ -> onListSelected(parent, position) }
        // category textbox
        (a.findViewById(R.id.et_category) as EditText).setOnFocusChangeListener { _, hasFocus -> onCategoryFocused(hasFocus) }
        // detail textbox
        (a.findViewById(R.id.et_detail) as EditText).setOnFocusChangeListener { _, hasFocus -> onDetailFocused(hasFocus) }
        // card button
        (a.findViewById(R.id.tv_card) as TextView).setOnClickListener { onCardSelected() }
        // cash button
        (a.findViewById(R.id.tv_cash) as TextView).setOnClickListener { onCashSelected() }
        // income button
        (a.findViewById(R.id.tv_income) as TextView).setOnClickListener { onIncomeSelected() }
        // expense button
        (a.findViewById(R.id.tv_expense) as TextView).setOnClickListener { onExpenseSelected() }
        // date button
        (a.findViewById(R.id.rl_date) as RelativeLayout).setOnClickListener { onDateSelected() }
        // left button
        (a.findViewById(R.id.bt_left) as Button).setOnClickListener { onLeftButtonClicked() }
        // right button
        (a.findViewById(R.id.bt_right) as Button).setOnClickListener { onRightButtonClicked() }
        // center button
        (a.findViewById(R.id.bt_center) as Button).setOnClickListener { onCenterButtonClicked() }
        // ten key
        (a.findViewById(R.id.bt_nine) as Button).setOnClickListener { onTenKeyClicked('9') }
        (a.findViewById(R.id.bt_eight) as Button).setOnClickListener { onTenKeyClicked('8') }
        (a.findViewById(R.id.bt_seven) as Button).setOnClickListener { onTenKeyClicked('7') }
        (a.findViewById(R.id.bt_six) as Button).setOnClickListener { onTenKeyClicked('6') }
        (a.findViewById(R.id.bt_five) as Button).setOnClickListener { onTenKeyClicked('5') }
        (a.findViewById(R.id.bt_four) as Button).setOnClickListener { onTenKeyClicked('4') }
        (a.findViewById(R.id.bt_three) as Button).setOnClickListener { onTenKeyClicked('3') }
        (a.findViewById(R.id.bt_two) as Button).setOnClickListener { onTenKeyClicked('2') }
        (a.findViewById(R.id.bt_one) as Button).setOnClickListener { onTenKeyClicked('1') }
        (a.findViewById(R.id.bt_zero) as Button).setOnClickListener { onTenKeyClicked('0') }
        // back key
        (a.findViewById(R.id.bt_back) as Button).setOnClickListener { onBackKeyClicked() }
        // clear key
        (a.findViewById(R.id.bt_clear) as Button).setOnClickListener { onClearKeyClicked() }
    }

    //===
    //=== listeners and callback
    //===
    // on select an item of list
    fun onListSelected(parent: AdapterView<*>, pos: Int) {
        // act depends on current input target
        when (condition[Constants.INPUT_TARGET]) {
            Constants.CATEGORY -> {
                setCategoryFromList(mCaller.findViewById(R.id.et_category) as EditText,
                        mCaller.findViewById(R.id.et_category) as EditText,
                        (parent as ListView).getItemAtPosition(pos) as String)
                // onDetailFocused is called, succeeded by requestFocus() in setCategoryFromList()
            }
            Constants.DETAIL -> {
                setDetailFromList(mCaller.findViewById(R.id.et_category) as EditText,
                        (parent as ListView).getItemAtPosition(pos) as String)
            }
        }
    }

    fun onCategoryFocused(hasFocus: Boolean) {
        if (condition[Constants.INPUT_TARGET] != Constants.CATEGORY && hasFocus) {
            // change input target to category
            setCategoryAndDetail(mCaller.findViewById(R.id.et_detail) as EditText,
                    mCaller.findViewById(R.id.et_category) as EditText,
                    mCaller.findViewById(R.id.lv_categoryAndDetail) as ListView,
                    resources.getStringArray(R.array.lv_category_and_detail),
                    condition, Constants.CATEGORY, mCaller)
        }
    }

    fun onDetailFocused(hasFocus: Boolean) {
        if (condition[Constants.INPUT_TARGET] != Constants.DETAIL && hasFocus) {
            // change input target to detail
            setCategoryAndDetail(mCaller.findViewById(R.id.et_category) as EditText,
                    mCaller.findViewById(R.id.et_detail) as EditText,
                    mCaller.findViewById(R.id.lv_categoryAndDetail) as ListView,
                    DetailHistoryAccess().getPreference(mCaller),
                    condition, Constants.DETAIL, mCaller)
        }
    }

    fun onCardSelected() {
        if (condition[Constants.TERMS_OF_PAYMENT] == Constants.CASH) {
            // select "card" if card is not selected
            setCardAndCash(mCaller.findViewById(R.id.tv_cash) as TextView,
                    mCaller.findViewById(R.id.tv_card) as TextView,
                    condition, Constants.CARD)
        }
    }

    fun onCashSelected() {
        if (condition[Constants.TERMS_OF_PAYMENT] == Constants.CARD) {
            // select "cash" if cash is not selected
            setCardAndCash(mCaller.findViewById(R.id.tv_card) as TextView,
                    mCaller.findViewById(R.id.tv_cash) as TextView,
                    condition, Constants.CASH)
        }
    }

    fun onIncomeSelected() {
        // select "income" if cash is not selected
        if (condition[Constants.TYPE] == Constants.EXPENSE) {
            setIncomeAndExpense(mCaller.findViewById(R.id.tv_expense) as TextView,
                    mCaller.findViewById(R.id.tv_income) as TextView,
                    condition, Constants.INCOME)
        }
    }

    fun onExpenseSelected() {
        // select "expense" if expense is not selected
        if (condition[Constants.TYPE] == Constants.INCOME) {
            setIncomeAndExpense(mCaller.findViewById(R.id.tv_income) as TextView,
                    mCaller.findViewById(R.id.tv_expense) as TextView,
                    condition, Constants.EXPENSE)
        }
    }

    fun onDateSelected() {
        // show fragment(dialog)
        CalendarDialogFragment.newInstance(selectedDate.year, selectedDate.month, selectedDate.day)
                .show(childFragmentManager, "dialog")
    }

    fun onTenKeyClicked(k: Char) {
        addPrice((mCaller.findViewById(R.id.tv_priceValue) as TextView), priceStack, k)
    }

    fun onBackKeyClicked() {
        removePrice((mCaller.findViewById(R.id.tv_priceValue) as TextView), priceStack)
    }

    fun onClearKeyClicked() {
        clearPrice((mCaller.findViewById(R.id.tv_priceValue) as TextView), priceStack)
    }

    abstract fun onLeftButtonClicked()
    abstract fun onRightButtonClicked()
    abstract fun onCenterButtonClicked()

    // callback from calendar dialog
    override fun onDialogDateSelected(y: Int, m: Int, d: Int) {
        setDate(y, m, d,
                mCaller.findViewById(R.id.tv_year) as TextView,
                mCaller.findViewById(R.id.tv_monthAndDay) as TextView,
                mCaller.findViewById(R.id.tv_dayOfWeek) as TextView,
                selectedDate)
    }

    //===
    //=== business logic
    //===
    fun addPrice(v: TextView, p: Deque<Char>, k: Char) {
        if (p.size < MAXDIGITS) {
            if (p.size == 1 && p.first == '0') {
                // delete 0 if stack contains only 0
                p.removeLast()
            }
            p.addLast(k)
            v.text = p.getPrice()
        }
    }

    fun clearPrice(v: TextView, p: Deque<Char>) {
        p.clear()
        p.addLast('0')
        v.text = p.getPrice()
    }

    fun removePrice(v: TextView, p: Deque<Char>) {
        p.removeLast()
        if (p.size == 0) {
            // if priceStack becomes empty, add 0
            p.addLast('0')
        }
        v.text = p.getPrice()
    }

    fun setDate(y: Int, m: Int, d: Int, yv: TextView, mv: TextView, dv: TextView, date: KakeiboDate) {
        // set year, month ,day
        date.setDate(y, m, d)
        // show
        yv.text = getString(R.string.show_year, y)
        mv.text = getString(R.string.show_monthday, m, d)
        dv.text = getString(R.string.show_dayofweek, Constants.WEEKNAME[date.dayOfWeek - 1])
    }

    // set category's value to which is selected from list
    fun setCategoryFromList(cv: EditText, dv: EditText, c: String) {
        cv.setText(c)
        // move focus on detail textbox
        dv.requestFocus()
        // move cursor to the end of string
        dv.setSelection(dv.text.toString().length)
    }

    // set detail's value to which is selected from list
    fun setDetailFromList(dv: EditText, d: String) {
        // update detail textbox(stay focus)
        dv.setText(d)
        // move cursor to the end of string
        dv.setSelection(dv.text.toString().length)
    }

    fun setCategoryAndDetail(from: EditText, to: EditText, lv: ListView, valArr: Array<String>, con: Array<Int>, target: Int, a: Activity) {
        // set back ground of detail textbox "unselected"
        from.setBackgroundResource(R.drawable.categoryanddetail_noselected)
        // set back ground of category textbox "selected"
        to.setBackgroundResource(R.drawable.categoryanddetail_selected)
        // set adapter to ListView
        lv.adapter = ArrayAdapter<String>(a, android.R.layout.simple_list_item_1, valArr)
        // set input target value
        con[Constants.INPUT_TARGET] = target
    }

    fun setCardAndCash(from: TextView, to: TextView, con: Array<Int>, terms: Int) {
        // change background color of card textview to "unselected"
        from.setBackgroundResource(R.drawable.termsandtype_noselected)
        // change background color of cash textview to "selected"
        to.setBackgroundResource(R.drawable.termsandtype_selected)
        // set termsOfPayment
        con[Constants.TERMS_OF_PAYMENT] = terms
    }

    fun setIncomeAndExpense(from: TextView, to: TextView, con: Array<Int>, type: Int) {
        // change background color of card textview to "unselected"
        from.setBackgroundResource(R.drawable.termsandtype_noselected)
        // change background color of cash textview to "selected"
        to.setBackgroundResource(R.drawable.termsandtype_selected)
        // set termsOfPayment
        con[Constants.TYPE] = type
    }

    //===
    //=== validation
    //===
    fun checkInput(): Boolean {
        // activity which calls this fragment
        val a = activity
        // check value of category
        var category = (a.findViewById(R.id.et_category) as EditText).text.toString()
        return category.isNotBlank()
    }

    //===
    //=== set contentsValue to store
    //===
    fun getContentValues(a: Activity): ContentValues {
        // contentValues to return
        val cv = ContentValues()
        // set value of category
        var category = (a.findViewById(R.id.et_category) as EditText).text.toString()
        cv.put("category", category)
        // set value of detail
        val detail = (a.findViewById(R.id.et_detail) as EditText).text.toString()
        cv.put("detail", detail)
        // set kakeibo name
        cv.put("kakeiboName", Constants.KAKEIBONAME_MINE)
        // set date
        cv.put("year", selectedDate.year)
        cv.put("month", selectedDate.month)
        cv.put("day", selectedDate.day)
        cv.put("dayOfWeek", selectedDate.dayOfWeek)
        // set income / expense
        cv.put("type", type)
        // set price
        val priceString = StringBuilder()
        while (priceStack.size > 0) {
            priceString.append(priceStack.removeFirst())
        }
        cv.put("price", Integer.parseInt(priceString.toString()))
        // set terms of payment
        cv.put("termsOfPayment", termsOfPayment)
        // set delete flag
        cv.put("isDeleted", Constants.FALSE)
        // set synchronized flag
        cv.put("isSynchronized", Constants.FALSE)
        // set last update date
        val ludString = StringBuilder(SimpleDateFormat(sdf).format(Calendar.getInstance().time))
        cv.put("lastUpdatedDate", ludString.toString())
        return cv
    }


}


