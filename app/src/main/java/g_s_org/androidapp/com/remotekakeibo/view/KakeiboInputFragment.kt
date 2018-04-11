package g_s_org.androidapp.com.remotekakeibo.view

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.View
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
    // current input target(category or detail)
    protected var inputTarget: Int = Constants.CATEGORY
    // current selected terms of payment(cash or card)
    protected var termsOfPayment: Int = Constants.CASH
    // current selected type(income or expense)
    protected var type: Int = Constants.EXPENSE

    override fun onAttach(context: Context?) {
        if (context is FragmentActivity) mCaller = context
        super.onAttach(context)
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
        (a.findViewById(R.id.lv_categoryAndDetail) as ListView).setOnItemClickListener { parent, _, position, _ -> onListSelected(a, parent, position) }
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
        (a.findViewById(R.id.bt_left) as Button).setOnClickListener { onLeftButtonClicked(a) }
        // right button
        (a.findViewById(R.id.bt_right) as Button).setOnClickListener { onRightButtonClicked(a) }
        // center button
        (a.findViewById(R.id.bt_center) as Button).setOnClickListener { onCenterButtonClicked(a) }
        // ten key
        (a.findViewById(R.id.bt_nine) as Button).setOnClickListener { onTenKeyClicked(a, '9') }
        (a.findViewById(R.id.bt_eight) as Button).setOnClickListener { onTenKeyClicked(a, '8') }
        (a.findViewById(R.id.bt_seven) as Button).setOnClickListener { onTenKeyClicked(a, '7') }
        (a.findViewById(R.id.bt_six) as Button).setOnClickListener { onTenKeyClicked(a, '6') }
        (a.findViewById(R.id.bt_five) as Button).setOnClickListener { onTenKeyClicked(a, '5') }
        (a.findViewById(R.id.bt_four) as Button).setOnClickListener { onTenKeyClicked(a, '4') }
        (a.findViewById(R.id.bt_three) as Button).setOnClickListener { onTenKeyClicked(a, '3') }
        (a.findViewById(R.id.bt_two) as Button).setOnClickListener { onTenKeyClicked(a, '2') }
        (a.findViewById(R.id.bt_one) as Button).setOnClickListener { onTenKeyClicked(a, '1') }
        (a.findViewById(R.id.bt_zero) as Button).setOnClickListener { onTenKeyClicked(a, '0') }
        // back key
        (a.findViewById(R.id.bt_back) as Button).setOnClickListener { onBackKeyClicked(a) }
        // clear key
        (a.findViewById(R.id.bt_clear) as Button).setOnClickListener { onClearKeyClicked(a) }
    }
    //===
    //=== functions run when each view is selected
    //===
    // on select an item of list
    fun onListSelected(a: Activity, parent: AdapterView<*>, pos: Int) {
        // act depends on current input target
        when (inputTarget) {
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
        if (inputTarget != Constants.CATEGORY && hasFocus) {
            // change input target
            inputTarget = Constants.CATEGORY
            setCategoryView()
        }
    }

    fun onDetailFocused(hasFocus: Boolean) {
        if (inputTarget != Constants.DETAIL && hasFocus) {
            // change input target
            inputTarget = Constants.DETAIL
            setDetailView()
        }
    }

    fun onCardSelected() {
        if (termsOfPayment == Constants.CASH) {
            // select "card" if card is not selected
            termsOfPayment = Constants.CARD
            setCardView()
        }
    }

    fun onCashSelected() {
        if (termsOfPayment == Constants.CARD) {
            // select "cash" if cash is not selected
            termsOfPayment = Constants.CASH
            setCashView()
        }
    }

    fun onIncomeSelected() {
        // select "income" if cash is not selected
        if (type == Constants.EXPENSE) {
            type = Constants.INCOME
            setIncomeView()
        }
    }

    fun onExpenseSelected() {
        // select "expense" if expense is not selected
        if (type == Constants.INCOME) {
            type = Constants.EXPENSE
            setExpenseView()
        }
    }

    fun onDateSelected() {
        // show fragment(dialog)
        CalendarDialogFragment.newInstance(selectedDate.year, selectedDate.month, selectedDate.day)
                .show(childFragmentManager, "dialog")
    }

    fun onTenKeyClicked(a: Activity, k: Char) {
        if (priceStack.size < MAXDIGITS) {
            if (priceStack.size == 1 && priceStack.first == '0') {
                // delete 0 if stack contains only 0
                priceStack.removeLast()
            }
            priceStack.addLast(k)
            setPriceView(priceStack.getPrice())
        }
    }

    fun onBackKeyClicked(a: Activity) {
        priceStack.removeLast()
        if (priceStack.size == 0) {
            // if priceStack becomes empty, add 0
            priceStack.addLast('0')
        }
        setPriceView(priceStack.getPrice())
    }

    fun onClearKeyClicked(a: Activity) {
        priceStack.clear()
        priceStack.addLast('0')
        setPriceView(priceStack.getPrice())
    }

    abstract fun onLeftButtonClicked(a: FragmentActivity)
    abstract fun onRightButtonClicked(a: FragmentActivity)
    abstract fun onCenterButtonClicked(a: FragmentActivity)

    //===
    //=== view setters
    //===
    fun setPriceView(p: String) {
        (mCaller.findViewById(R.id.tv_priceValue) as TextView).text = p
    }

    fun setCategoryFromList(c:String){
        // update category textbox
        (mCaller.findViewById(R.id.et_category) as EditText).setText(c)
        val detailEditText = (mCaller.findViewById(R.id.et_category) as EditText)
        // move focus on detail textbox
        detailEditText.requestFocus()
        // move cursor to the end of string
        detailEditText.setSelection(detailEditText.text.toString().length)
    }

    fun setDetailFromList(d:String){
        val detailEditText = (mCaller.findViewById(R.id.et_category) as EditText)
        // update detail textbox(stay focus)
        detailEditText.setText(d)
        // move cursor to the end of string
        detailEditText.setSelection(detailEditText.text.toString().length)
    }

    fun setCategoryView() {
        // set back ground of category textbox "selected"
        (mCaller.findViewById(R.id.et_category) as EditText).setBackgroundResource(R.drawable.categoryanddetail_selected)
        // set back ground of detail textbox "unselected"
        (mCaller.findViewById(R.id.et_detail) as EditText).setBackgroundResource(R.drawable.categoryanddetail_noselected)
        // get category list
        val valList = resources.getStringArray(R.array.lv_category_and_detail)
        // set adapter to ListView
        (mCaller.findViewById(R.id.lv_categoryAndDetail) as ListView).adapter =
                ArrayAdapter<String>(mCaller, android.R.layout.simple_list_item_1, valList)
    }

    fun setDetailView() {
        // set back ground of category textbox "selected"
        (mCaller.findViewById(R.id.et_category) as EditText).setBackgroundResource(R.drawable.categoryanddetail_noselected)
        // set back ground of detail textbox "unselected"
        (mCaller.findViewById(R.id.et_detail) as EditText).setBackgroundResource(R.drawable.categoryanddetail_selected)
        // get detail list
        val valList = DetailHistoryAccess().getPreference(mCaller)
        // set list to ListView
        (mCaller.findViewById(R.id.lv_categoryAndDetail) as ListView).adapter =
                ArrayAdapter<String>(mCaller, android.R.layout.simple_list_item_1, valList)
    }

    fun setCardView() {
        // change background color of cash textview to "selected"
        (mCaller.findViewById(R.id.tv_card) as TextView).setBackgroundResource(R.drawable.termsandtype_selected)
        // change background color of card textview to "unselected"
        (mCaller.findViewById(R.id.tv_cash) as TextView).setBackgroundResource(R.drawable.termsandtype_noselected)
    }

    fun setCashView() {
        // change background color of cash textview to "selected"
        (mCaller.findViewById(R.id.tv_cash) as TextView).setBackgroundResource(R.drawable.termsandtype_selected)
        // change background color of card textview to "unselected"
        (mCaller.findViewById(R.id.tv_card) as TextView).setBackgroundResource(R.drawable.termsandtype_noselected)
    }

    fun setIncomeView() {
        // change background color of income textview to "selected"
        (mCaller.findViewById(R.id.tv_income) as TextView).setBackgroundResource(R.drawable.termsandtype_selected)
        // change background color of expense textview to "unselected"
        (mCaller.findViewById(R.id.tv_expense) as TextView).setBackgroundResource(R.drawable.termsandtype_noselected)
    }

    fun setExpenseView() {
        // change background color of expense textview to "selected"
        (mCaller.findViewById(R.id.tv_expense) as TextView).setBackgroundResource(R.drawable.termsandtype_selected)
        // change background color of income textview to "unselected"
        (mCaller.findViewById(R.id.tv_income) as TextView).setBackgroundResource(R.drawable.termsandtype_noselected)
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

    //===
    //=== callback from calendar dialog
    //===
    override fun onDialogDateSelected(y: Int, m: Int, d: Int) {
        // activity which calls this fragment
        val a = activity
        // set year, month ,day
        selectedDate.setDate(y, m, d)
        // show
        (a.findViewById(R.id.tv_year) as TextView).text = getString(R.string.show_year, y)
        (a.findViewById(R.id.tv_monthAndDay) as TextView).text = getString(R.string.show_monthday, m, d)
        (a.findViewById(R.id.tv_dayOfWeek) as TextView).text = getString(R.string.show_dayofweek, Constants.WEEKNAME[selectedDate.dayOfWeek - 1])
    }
}


