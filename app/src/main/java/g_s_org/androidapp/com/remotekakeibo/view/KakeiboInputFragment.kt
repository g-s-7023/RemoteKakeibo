package g_s_org.androidapp.com.remotekakeibo.view

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import g_s_org.androidapp.com.remotekakeibo.common.Constants
import g_s_org.androidapp.com.remotekakeibo.common.Constants.Companion.MAXDIGITS
import g_s_org.androidapp.com.remotekakeibo.common.Constants.Companion.sdf
import g_s_org.androidapp.com.remotekakeibo.common.KakeiboDate
import g_s_org.androidapp.com.remotekakeibo.dbaccess.DetailHistoryAccess
import g_s_org.androidapp.com.remotekakeibo.model.getPrice
import g_s_org.androidapp.com.remotekakeibo.model.printPrice
import junit.runner.BaseTestRunner.getPreference
import kotlinx.android.synthetic.main.fragment_input.*
import java.text.SimpleDateFormat

import java.util.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [KakeiboInputFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [KakeiboInputFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
abstract class KakeiboInputFragment : Fragment() {
    // buffer of input price
    private val priceStack: Deque<Char> = ArrayDeque()
    // current input target(category or detail)
    private var inputTarget: Int = Constants.CATEGORY
    // current selected terms of payment(cash or card)
    private var termsOfPayment: Int = Constants.CASH
    // current selected type(income or expense)
    protected var type: Int = Constants.EXPENSE
    // selected year, month, and day
    protected var selectedDate: KakeiboDate = KakeiboDate()

    //===
    //=== on view created
    //===
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        // activity which calls this fragment
        val caller = activity

        //===
        //=== set listener
        //===
        // list
        (caller.findViewById(R.id.lv_categoryAndDetail) as ListView).setOnItemClickListener { parent, _, position, _ ->
            onListSelected(caller, parent, position)
        }
        // category textbox
        (caller.findViewById(R.id.et_category) as EditText).setOnFocusChangeListener { view, hasFocus ->
            // focus on category if current focus is on other than category
            if (inputTarget != Constants.CATEGORY && hasFocus == true) onCategorySelected(caller)
        }
        // detail textbox
        (caller.findViewById(R.id.et_detail) as EditText).setOnFocusChangeListener { view, hasFocus ->
            // focus on detail if current focus is on other than detail
            if (inputTarget != Constants.DETAIL && hasFocus == true) onDetailSelected(caller)
        }
        // card button
        (caller.findViewById(R.id.tv_card) as TextView).setOnClickListener {
            if (termsOfPayment == Constants.CASH) onCardSelected(it, caller)
        }
        // cash button
        (caller.findViewById(R.id.tv_cash) as TextView).setOnClickListener {
            if (termsOfPayment == Constants.CARD) onCashSelected(it, caller)
        }
        // income button
        (caller.findViewById(R.id.tv_income) as TextView).setOnClickListener {
            if (type == Constants.EXPENSE) onIncomeSelected(it, caller)
        }
        // expense button
        (caller.findViewById(R.id.tv_expense) as TextView).setOnClickListener {
            if (type == Constants.INCOME) onExpenseSelected(it, caller)
        }
        // date button
        (caller.findViewById(R.id.rl_date) as TextView).setOnClickListener { onDateSelected(caller) }
        // ten key
        (caller.findViewById(R.id.bt_nine) as Button).setOnClickListener { onTenKeyClicked(caller, '9') }
        (caller.findViewById(R.id.bt_eight) as Button).setOnClickListener { onTenKeyClicked(caller, '8') }
        (caller.findViewById(R.id.bt_seven) as Button).setOnClickListener { onTenKeyClicked(caller, '7') }
        (caller.findViewById(R.id.bt_six) as Button).setOnClickListener { onTenKeyClicked(caller, '6') }
        (caller.findViewById(R.id.bt_five) as Button).setOnClickListener { onTenKeyClicked(caller, '5') }
        (caller.findViewById(R.id.bt_four) as Button).setOnClickListener { onTenKeyClicked(caller, '4') }
        (caller.findViewById(R.id.bt_three) as Button).setOnClickListener { onTenKeyClicked(caller, '3') }
        (caller.findViewById(R.id.bt_two) as Button).setOnClickListener { onTenKeyClicked(caller, '2') }
        (caller.findViewById(R.id.bt_one) as Button).setOnClickListener { onTenKeyClicked(caller, '1') }
        (caller.findViewById(R.id.bt_zero) as Button).setOnClickListener { onTenKeyClicked(caller, '0') }
        // back key
        (caller.findViewById(R.id.bt_back) as Button).setOnClickListener { onBackKeyClicked(caller) }
        // clear key
        (caller.findViewById(R.id.bt_clear) as Button).setOnClickListener { onClearKeyClicked(caller) }
        super.onViewCreated(view, savedInstanceState)
    }

    //===
    //=== functions run when selected
    //===
    // on select an item of list
    fun onListSelected(a: Activity, parent: AdapterView<*>, pos: Int) {
        // get selected string
        val inputString = (parent as ListView).getItemAtPosition(pos) as String
        // detail textbox
        val detailEditText: EditText = a.findViewById(R.id.et_detail) as EditText
        // act depends on current input target
        when (inputTarget) {
            Constants.CATEGORY -> {
                // update category textbox
                (a.findViewById(R.id.et_category) as EditText).setText(inputString)
                // move focus on detail textbox
                detailEditText.requestFocus()
            }
            Constants.DETAIL -> {
                // update detail textbox(stay focus)
                detailEditText.setText(inputString)
            }
        }
        // get string in detail textbox
        val detailString = detailEditText.text.toString()
        // move cursor to the end of string
        detailEditText.setSelection(detailString.length)
    }

    // on select category
    fun onCategorySelected(a: Activity) {
        // change input target
        inputTarget = Constants.CATEGORY
        // set back ground of category textbox "selected"
        (a.findViewById(R.id.et_category) as EditText).setBackgroundResource(R.drawable.categoryanddetail_selected)
        // set back ground of detail textbox "unselected"
        (a.findViewById(R.id.et_detail) as EditText).setBackgroundResource(R.drawable.categoryanddetail_noselected)
        // get category list
        val valList = resources.getStringArray(R.array.lv_category_and_detail)
        // create adapter and show list
        val adapter = ArrayAdapter<String>(caller, android.R.layout.simple_list_item_1, valList)
        (a.findViewById(R.id.lv_categoryAndDetail) as ListView).adapter = adapter
    }

    // on select detail
    fun onDetailSelected(a: Activity) {
        // change input target
        inputTarget = Constants.DETAIL
        // set back ground of category textbox "selected"
        (a.findViewById(R.id.et_category) as EditText).setBackgroundResource(R.drawable.categoryanddetail_noselected)
        // set back ground of detail textbox "unselected"
        (a.findViewById(R.id.et_detail) as EditText).setBackgroundResource(R.drawable.categoryanddetail_selected)
        // get detail list
        val valList = DetailHistoryAccess().getPreference(a)
        // create adapter and show list
        val adapter = ArrayAdapter<String>(a, android.R.layout.simple_list_item_1, valList)
        (a.findViewById(R.id.lv_categoryAndDetail) as ListView).adapter = adapter
    }

    fun onCardSelected(v: View, a: Activity) {
        // select "card" if card is not selected
        termsOfPayment = Constants.CARD
        // change background color of card textview to "selected"
        v.setBackgroundResource(R.drawable.termsandtype_selected)
        // change background color of cash textview to "unselected"
        a.findViewById(R.id.tv_cash).setBackgroundResource(R.drawable.termsandtype_noselected)
    }

    fun onCashSelected(v: View, a: Activity) {
        // select "cash" if cash is not selected
        termsOfPayment = Constants.CASH
        // change background color of cash textview to "selected"
        v.setBackgroundResource(R.drawable.termsandtype_selected)
        // change background color of card textview to "unselected"
        a.findViewById(R.id.tv_card).setBackgroundResource(R.drawable.termsandtype_noselected)
    }

    fun onIncomeSelected(v: View, a: Activity) {
        // select "income" if cash is not selected
        type = Constants.INCOME
        // change background color of income textview to "selected"
        v.setBackgroundResource(R.drawable.termsandtype_selected)
        // change background color of expense textview to "unselected"
        a.findViewById(R.id.tv_expense).setBackgroundResource(R.drawable.termsandtype_noselected)
    }

    fun onExpenseSelected(v: View, a: Activity) {
        // select "expense" if cash is not selected
        type = Constants.EXPENSE
        // change background color of expense textview to "selected"
        v.setBackgroundResource(R.drawable.termsandtype_selected)
        // change background color of income textview to "unselected"
        a.findViewById(R.id.tv_income).setBackgroundResource(R.drawable.termsandtype_noselected)
    }

    // on date selected, show calendar dialog
    fun onDateSelected(a: FragmentActivity) {
        // fragment
        val calFragment = CalendarDialogFragment()
        // set year, month, day when "date" is tapped
        val args = Bundle()
        args.putInt("YEAR_BEFORE", selectedDate.year)
        args.putInt("MONTH_BEFORE", selectedDate.month)
        args.putInt("DAY_BEFORE", selectedDate.day)
        // pass arguments to fragment
        calFragment.arguments = args
        // prohibit cancel with "return" button
        calFragment.isCancelable = false
        // show fragment(dialog)
        calFragment.show(a.supportFragmentManager, "dialog")
    }

    fun onTenKeyClicked(a: Activity, k: Char) {
        if (priceStack.size < MAXDIGITS) {
            if (priceStack.size == 1 && priceStack.first == '0') {
                // delete 0 if stack contains only 0
                priceStack.removeLast()
            }
            priceStack.addLast(k)
            (a.findViewById(R.id.tv_priceValue) as TextView).setText(priceStack.getPrice())
        }
    }

    fun onBackKeyClicked(a: Activity) {
        priceStack.removeLast()
        if (priceStack.size == 0) {
            // if priceStack becomes empty, add 0
            priceStack.addLast('0')
        }
        (a.findViewById(R.id.tv_priceValue) as TextView).setText(priceStack.getPrice())
    }

    fun onClearKeyClicked(a: Activity) {
        priceStack.clear()
        priceStack.addLast('0')
        (a.findViewById(R.id.tv_priceValue) as TextView).setText(priceStack.getPrice())
    }

    abstract fun leftButtonClicked()
    abstract fun rightButtonClicked()
    abstract fun centerButtonClicked()

    //===
    //=== callback from calendar dialog
    //===
    override fun onDialogDateSelected(y: Int, m: Int, d: Int) {
        // activity which calls this fragment
        val caller = activity
        // set year, month ,day
        selectedDate.year = y
        selectedDate.month = m
        selectedDate.day = d
        selectedDate.setDayOfWeek(y, m, d)
        // show
        (caller.findViewById(R.id.tv_year) as TextView).setText(y.toString() + "年")
        (caller.findViewById(R.id.tv_monthAndDay) as TextView).setText(m.toString() + "/" + d.toString())
        (caller.findViewById(R.id.tv_dayOfWeek) as TextView).setText("(" + Constants.WEEKNAME[selectedDate.dayOfWeek - 1] + ")")
    }

    //===
    //=== initialize textview
    //===
    fun initInputData() {
        // activity which calls this fragment
        val caller = activity
        // clear text in category textbox
        (caller.findViewById(R.id.et_category) as EditText).setText("")
        // clear text in detail textbox
        (caller.findViewById(R.id.et_detail) as EditText).setText("")
        // select category
        onCategorySelected(caller)
        // select "expense"
        onExpenseSelected(caller.findViewById(R.id.tv_expense), caller)
        // select "cash"
        onCashSelected(caller.findViewById(R.id.tv_cash), caller)
        // price
        priceStack.clear()
        priceStack.addLast('0')
        (caller.findViewById(R.id.tv_priceValue) as TextView).setText(priceStack.getPrice())
        // set focus on price (not to show keyboard)
        (caller.findViewById(R.id.tv_priceValue) as TextView).requestFocus()
    }

    //===
    //=== validation
    //===
    fun checkInput(): Boolean {
        // activity which calls this fragment
        val caller = activity
        // check value of category
        var category = (caller.findViewById(R.id.et_category) as EditText).text.toString()
        return category.isNotBlank()
    }
    //===
    //=== set contentsValue to store
    //===


    // 4/3修正
    // 入力値チェックは別出しにする


    fun setContentValues(cv: ContentValues) {
        // activity which calls this fragment
        val caller = activity
        // set value of category
        var category = (caller.findViewById(R.id.et_category) as EditText).text.toString()
        cv.put("category", category)
        // set value of detail
        val detail = (caller.findViewById(R.id.et_detail) as EditText).text.toString()
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
    }




    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }


}// Required empty public constructor
