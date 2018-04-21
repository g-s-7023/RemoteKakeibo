package g_s_org.androidapp.com.remotekakeibo.view

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
import g_s_org.androidapp.com.remotekakeibo.common.KakeiboDate
import g_s_org.androidapp.com.remotekakeibo.dbaccess.DetailHistoryAccess
import g_s_org.androidapp.com.remotekakeibo.model.getPrice
import g_s_org.androidapp.com.remotekakeibo.R
import g_s_org.androidapp.com.remotekakeibo.model.setPrice

import java.util.*

abstract class KakeiboInputFragment : Fragment(), CalendarDialogFragment.OnDialogInteractionListener {
    // caller of this activity
    protected lateinit var mCaller: FragmentActivity
    // buffer of input price
    protected val priceStack: Deque<Char> = ArrayDeque()
    // selected year, month, and day
    protected var selectedDate: KakeiboDate = KakeiboDate()
    // condition
    protected var condition = Array<Int>(3, { -1 })

    //===
    //=== mCallback from lifecycle
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
    //=== set listener of each view
    //===
    open fun setListeners() {
        // list
        (mCaller.findViewById(R.id.lv_categoryAndDetail) as ListView).setOnItemClickListener { parent, _, position, _ -> onListSelected(parent, position) }
        // category textbox
        (mCaller.findViewById(R.id.et_category) as EditText).setOnFocusChangeListener { _, hasFocus -> onCategoryFocused(hasFocus) }
        // detail textbox
        (mCaller.findViewById(R.id.et_detail) as EditText).setOnFocusChangeListener { _, hasFocus -> onDetailFocused(hasFocus) }
        // card button
        (mCaller.findViewById(R.id.tv_card) as TextView).setOnClickListener { onCardSelected() }
        // cash button
        (mCaller.findViewById(R.id.tv_cash) as TextView).setOnClickListener { onCashSelected() }
        // income button
        (mCaller.findViewById(R.id.tv_income) as TextView).setOnClickListener { onIncomeSelected() }
        // expense button
        (mCaller.findViewById(R.id.tv_expense) as TextView).setOnClickListener { onExpenseSelected() }
        // date button
        (mCaller.findViewById(R.id.rl_date) as RelativeLayout).setOnClickListener { onDateSelected() }
        // left button
        (mCaller.findViewById(R.id.bt_left) as Button).setOnClickListener { onLeftButtonClicked() }
        // right button
        (mCaller.findViewById(R.id.bt_right) as Button).setOnClickListener { onRightButtonClicked() }
        // center button
        (mCaller.findViewById(R.id.bt_center) as Button).setOnClickListener { onCenterButtonClicked() }
        // ten key
        (mCaller.findViewById(R.id.bt_nine) as Button).setOnClickListener { onTenKeyClicked('9') }
        (mCaller.findViewById(R.id.bt_eight) as Button).setOnClickListener { onTenKeyClicked('8') }
        (mCaller.findViewById(R.id.bt_seven) as Button).setOnClickListener { onTenKeyClicked('7') }
        (mCaller.findViewById(R.id.bt_six) as Button).setOnClickListener { onTenKeyClicked('6') }
        (mCaller.findViewById(R.id.bt_five) as Button).setOnClickListener { onTenKeyClicked('5') }
        (mCaller.findViewById(R.id.bt_four) as Button).setOnClickListener { onTenKeyClicked('4') }
        (mCaller.findViewById(R.id.bt_three) as Button).setOnClickListener { onTenKeyClicked('3') }
        (mCaller.findViewById(R.id.bt_two) as Button).setOnClickListener { onTenKeyClicked('2') }
        (mCaller.findViewById(R.id.bt_one) as Button).setOnClickListener { onTenKeyClicked('1') }
        (mCaller.findViewById(R.id.bt_zero) as Button).setOnClickListener { onTenKeyClicked('0') }
        // back key
        (mCaller.findViewById(R.id.bt_back) as Button).setOnClickListener { onBackKeyClicked() }
        // clear key
        (mCaller.findViewById(R.id.bt_clear) as Button).setOnClickListener { onClearKeyClicked() }
    }

    //===
    //=== listeners and mCallback
    //===
    // on select an item of list
    fun onListSelected(parent: AdapterView<*>, pos: Int) {
        // act depends on current input target
        when (condition[Constants.INPUT_TARGET]) {
            Constants.CATEGORY -> {
                setCategoryFromList((parent as ListView).getItemAtPosition(pos) as String)
                // onDetailFocused is called, succeeded by requestFocus() in setCategoryFromList()
            }
            Constants.DETAIL -> {
                setDetailFromList((parent as ListView).getItemAtPosition(pos) as String)
            }
        }
    }

    fun onCategoryFocused(hasFocus: Boolean) {
        if (condition[Constants.INPUT_TARGET] != Constants.CATEGORY && hasFocus) {
            // change input target to category
            setCategory()
        }
    }

    fun onDetailFocused(hasFocus: Boolean) {
        if (condition[Constants.INPUT_TARGET] != Constants.DETAIL && hasFocus) {
            // change input target to detail
            setDetail()
        }
    }

    fun onCardSelected() {
        if (condition[Constants.TERMS_OF_PAYMENT] == Constants.CASH) {
            // select "card" if card is not selected
            setCard()
        }
    }

    fun onCashSelected() {
        if (condition[Constants.TERMS_OF_PAYMENT] == Constants.CARD) {
            // select "cash" if cash is not selected
            setCash()
        }
    }

    fun onIncomeSelected() {
        // select "income" if cash is not selected
        if (condition[Constants.TYPE] == Constants.EXPENSE) {
            setIncome()
        }
    }

    fun onExpenseSelected() {
        // select "expense" if expense is not selected
        if (condition[Constants.TYPE] == Constants.INCOME) {
            setExpense()
        }
    }

    fun onDateSelected() {
        // show fragment(dialog)
        CalendarDialogFragment.newInstance(selectedDate.year, selectedDate.month, selectedDate.day)
                .show(childFragmentManager, "dialog")
    }

    fun onTenKeyClicked(k: Char) {
        addPrice(k)
    }

    fun onBackKeyClicked() {
        removePrice()
    }

    fun onClearKeyClicked() {
        clearPrice()
    }

    abstract fun onLeftButtonClicked()
    abstract fun onRightButtonClicked()
    abstract fun onCenterButtonClicked()

    // mCallback from calendar dialog
    override fun onDialogDateSelected(y: Int, m: Int, d: Int) {
        setDate(y, m, d)
    }

    //===
    //=== view and value setter
    //===
    fun addPrice(k: Char) {
        if (priceStack.size < MAXDIGITS) {
            if (priceStack.size == 1 && priceStack.first == '0') {
                // delete 0 if stack contains only 0
                priceStack.removeLast()
            }
            priceStack.addLast(k)
            (mCaller.findViewById(R.id.tv_priceValue) as TextView).text = priceStack.getPrice()
        }
    }

    fun clearPrice() {
        priceStack.clear()
        priceStack.addLast('0')
        (mCaller.findViewById(R.id.tv_priceValue) as TextView).text = priceStack.getPrice()
    }

    fun removePrice() {
        priceStack.removeLast()
        if (priceStack.size == 0) {
            // if priceStack becomes empty, add 0
            priceStack.addLast('0')
        }
        (mCaller.findViewById(R.id.tv_priceValue) as TextView).text = priceStack.getPrice()
    }

    fun setPrice(p: Int) {
        priceStack.setPrice(p)
        (mCaller.findViewById(R.id.tv_priceValue) as TextView).text = priceStack.getPrice()
    }

    fun setDate(y: Int, m: Int, d: Int) {
        // set year, month ,day
        selectedDate.setDate(y, m, d)
        // show
        (mCaller.findViewById(R.id.tv_year) as TextView).text = y.toString()
        (mCaller.findViewById(R.id.tv_monthAndDay) as TextView).text = getString(R.string.show_monthday, m, d)
        (mCaller.findViewById(R.id.tv_dayOfWeek) as TextView).text = getString(R.string.show_dayofweek, Constants.WEEKNAME[selectedDate.dayOfWeek - 1])
    }

    fun setToday() {
        // set year, month, day of today
        selectedDate.setDate(Calendar.getInstance())
        // show
        (mCaller.findViewById(R.id.tv_year) as TextView).text = selectedDate.year.toString()
        (mCaller.findViewById(R.id.tv_monthAndDay) as TextView).text = getString(R.string.show_monthday, selectedDate.month, selectedDate.day)
        (mCaller.findViewById(R.id.tv_dayOfWeek) as TextView).text = getString(R.string.show_dayofweek, Constants.WEEKNAME[selectedDate.dayOfWeek - 1])
    }

    // set category's value to which is selected from list
    fun setCategoryFromList(c: String) {
        (mCaller.findViewById(R.id.et_category) as EditText).setText(c)
        // move focus on detail textbox
        (mCaller.findViewById(R.id.et_detail) as EditText).requestFocus()
        // move cursor to the end of string
        (mCaller.findViewById(R.id.et_detail) as EditText)
                .setSelection((mCaller.findViewById(R.id.et_detail) as EditText).text.toString().length)
    }

    // set detail's value to which is selected from list
    fun setDetailFromList(d: String) {
        // update detail textbox(stay focus)
        (mCaller.findViewById(R.id.et_detail) as EditText).setText(d)
        // move cursor to the end of string
        (mCaller.findViewById(R.id.et_detail) as EditText)
                .setSelection((mCaller.findViewById(R.id.et_detail) as EditText).text.toString().length)
    }

    fun setCategory() {
        // set back ground of detail textbox "unselected"
        (mCaller.findViewById(R.id.et_detail) as EditText).setBackgroundResource(R.drawable.categoryanddetail_noselected)
        // set back ground of category textbox "selected"
        (mCaller.findViewById(R.id.et_category) as EditText).setBackgroundResource(R.drawable.categoryanddetail_selected)
        // set adapter to ListView
        (mCaller.findViewById(R.id.lv_categoryAndDetail) as ListView).adapter =
                ArrayAdapter<String>(mCaller, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.lv_category_and_detail))
        // set input target value
        condition[Constants.INPUT_TARGET] = Constants.CATEGORY
    }

    fun setDetail() {
        // set back ground of detail textbox "selected"
        (mCaller.findViewById(R.id.et_detail) as EditText).setBackgroundResource(R.drawable.categoryanddetail_selected)
        // set back ground of category textbox "unselected"
        (mCaller.findViewById(R.id.et_category) as EditText).setBackgroundResource(R.drawable.categoryanddetail_noselected)
        // set adapter to ListView
        (mCaller.findViewById(R.id.lv_categoryAndDetail) as ListView).adapter =
                ArrayAdapter<String>(mCaller, android.R.layout.simple_list_item_1, DetailHistoryAccess().getPreference(mCaller))
        // set input target value
        condition[Constants.INPUT_TARGET] = Constants.DETAIL
    }

    fun setCard() {
        // change background color of card textview to "unselected"
        (mCaller.findViewById(R.id.tv_cash) as TextView).setBackgroundResource(R.drawable.termsandtype_noselected)
        // change background color of cash textview to "selected"
        (mCaller.findViewById(R.id.tv_card) as TextView).setBackgroundResource(R.drawable.termsandtype_selected)
        // set termsOfPayment
        condition[Constants.TERMS_OF_PAYMENT] = Constants.CARD
    }

    fun setCash() {
        // change background color of card textview to "selected"
        (mCaller.findViewById(R.id.tv_cash) as TextView).setBackgroundResource(R.drawable.termsandtype_selected)
        // change background color of cash textview to "unselected"
        (mCaller.findViewById(R.id.tv_card) as TextView).setBackgroundResource(R.drawable.termsandtype_noselected)
        // set termsOfPayment
        condition[Constants.TERMS_OF_PAYMENT] = Constants.CASH
    }

    fun setIncome() {
        // change background color of card textview to "unselected"
        (mCaller.findViewById(R.id.tv_expense) as TextView).setBackgroundResource(R.drawable.termsandtype_noselected)
        // change background color of cash textview to "selected"
        (mCaller.findViewById(R.id.tv_income) as TextView).setBackgroundResource(R.drawable.termsandtype_selected)
        // set termsOfPayment
        condition[Constants.TYPE] = Constants.INCOME
    }

    fun setExpense() {
        // change background color of card textview to "selected"
        (mCaller.findViewById(R.id.tv_expense) as TextView).setBackgroundResource(R.drawable.termsandtype_selected)
        // change background color of cash textview to "unselected"
        (mCaller.findViewById(R.id.tv_income) as TextView).setBackgroundResource(R.drawable.termsandtype_noselected)
        // set termsOfPayment
        condition[Constants.TYPE] = Constants.EXPENSE
    }

    //===
    //=== business logic
    //===
    // get contentValues to save
    fun getContentValues(cat: String, det: String, date: KakeiboDate, price: Deque<Char>, con: Array<Int>): ContentValues {
        // contentValues to return
        val cv = ContentValues()
        // set value of category
        cv.put("category", cat)
        // set value of detail
        cv.put("detail", det)
        // set kakeibo name
        cv.put("kakeiboName", Constants.KAKEIBONAME_MINE)
        // set date
        cv.put("year", date.year)
        cv.put("month", date.month)
        cv.put("day", date.day)
        cv.put("dayOfWeek", date.dayOfWeek)
        // set price
        val priceString = StringBuilder()
        while (price.size > 0) {
            priceString.append(price.removeFirst())
        }
        cv.put("price", Integer.parseInt(priceString.toString()))
        // set terms of payment
        cv.put("termsOfPayment", con[Constants.TERMS_OF_PAYMENT])
        // set income / expense
        cv.put("type", con[Constants.TYPE])
        // set synchronized flag
        cv.put("isSynchronized", Constants.FALSE)
        return cv
    }
}


