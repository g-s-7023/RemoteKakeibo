package g_s_org.androidapp.com.remotekakeibo.view

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import android.widget.*
import g_s_org.androidapp.com.remotekakeibo.common.Constants
import g_s_org.androidapp.com.remotekakeibo.common.Constants.Companion.MAXDIGITS
import g_s_org.androidapp.com.remotekakeibo.common.Constants.Companion.sdf
import g_s_org.androidapp.com.remotekakeibo.common.KakeiboDate
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
    // 現在選択されている年月日を保持する変数
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
        // ten key
        class TenKeysNumClickListener(val clickedKey: Char) : View.OnClickListener {
            override fun onClick(v: View?) {
                if (priceStack.size < MAXDIGITS) {
                    if (priceStack.size == 1 && priceStack.getFirst() == '0') {
                        // delete 0 if stack contains only 0
                        priceStack.removeLast()
                    }
                    priceStack.addLast(clickedKey)
                    priceStack.printPrice()
                }
            }
        }
        (caller.findViewById(R.id.bt_nine) as Button).setOnClickListener(TenKeysNumClickListener('9'))
        (caller.findViewById(R.id.bt_eight) as Button).setOnClickListener(TenKeysNumClickListener('8'))
        (caller.findViewById(R.id.bt_seven) as Button).setOnClickListener(TenKeysNumClickListener('7'))
        (caller.findViewById(R.id.bt_six) as Button).setOnClickListener(TenKeysNumClickListener('6'))
        (caller.findViewById(R.id.bt_five) as Button).setOnClickListener(TenKeysNumClickListener('5'))
        (caller.findViewById(R.id.bt_four) as Button).setOnClickListener(TenKeysNumClickListener('4'))
        (caller.findViewById(R.id.bt_three) as Button).setOnClickListener(TenKeysNumClickListener('3'))
        (caller.findViewById(R.id.bt_two) as Button).setOnClickListener(TenKeysNumClickListener('2'))
        (caller.findViewById(R.id.bt_one) as Button).setOnClickListener(TenKeysNumClickListener('1'))
        (caller.findViewById(R.id.bt_zero) as Button).setOnClickListener(TenKeysNumClickListener('0'))
        // back key
        (caller.findViewById(R.id.bt_back) as Button).setOnClickListener {
            priceStack.removeLast()
            if (priceStack.size == 0) {
                // if priceStack becomes empty, add 0
                priceStack.addLast('0')
            }
            priceStack.printPrice()
        }
        // clear key
        (caller.findViewById(R.id.bt_clear) as Button).setOnClickListener {
            priceStack.clear()
            priceStack.addLast('0')
            priceStack.printPrice()
        }
        // list
        (caller.findViewById(R.id.lv_categoryAndDetail) as ListView).setOnItemClickListener { parent, view, position, id ->
            // get selected string
            val inputString = (parent as ListView).getItemAtPosition(position) as String
            // detail textbox
            val detailEditText: EditText = caller.findViewById(R.id.et_detail) as EditText
            // act depends on current input target
            when (inputTarget) {
                Constants.CATEGORY -> {
                    // update category textbox
                    (caller.findViewById(R.id.et_category) as EditText).setText(inputString)
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
        // category textbox
        (caller.findViewById(R.id.et_category) as EditText).setOnFocusChangeListener { view, hasFocus ->
            if (inputTarget != Constants.CATEGORY && hasFocus == true) run {
                // focus on category if current focus is on other than category
                inputTarget = Constants.CATEGORY
                setInputList(inputTarget)
            }
        }
        // detail textbox
        (caller.findViewById(R.id.et_detail) as EditText).setOnFocusChangeListener { view, hasFocus ->
            if (inputTarget != Constants.DETAIL && hasFocus == true) {
                // focus on detail if current focus is on other than detail
                inputTarget = Constants.DETAIL
                setInputList(inputTarget)
            }
        }
        // card button
        (caller.findViewById(R.id.tv_card) as TextView).setOnClickListener {
            if (termsOfPayment == Constants.CASH) {
                // select "card" if card is not selected
                termsOfPayment = Constants.CARD
                // change background color of card textview to "selected"
                it.setBackgroundResource(R.drawable.termsandtype_selected)
                // change background color of cash textview to "unselected"
                caller.findViewById(R.id.tv_cash).setBackgroundResource(R.drawable.termsandtype_noselected)
            }
        }
        // cash button
        (caller.findViewById(R.id.tv_cash) as TextView).setOnClickListener {
            if (termsOfPayment == Constants.CARD) {
                // select "cash" if cash is not selected
                termsOfPayment = Constants.CASH
                // change background color of cash textview to "selected"
                it.setBackgroundResource(R.drawable.termsandtype_selected)
                // change background color of card textview to "unselected"
                caller.findViewById(R.id.tv_card).setBackgroundResource(R.drawable.termsandtype_noselected)
            }
        }
        // income button
        (caller.findViewById(R.id.tv_income) as TextView).setOnClickListener {
            if (type == Constants.EXPENSE) {
                // select "income" if cash is not selected
                type = Constants.INCOME
                // change background color of income textview to "selected"
                it.setBackgroundResource(R.drawable.termsandtype_selected)
                // change background color of expense textview to "unselected"
                caller.findViewById(R.id.tv_expense).setBackgroundResource(R.drawable.termsandtype_noselected)
            }
        }
        // expense button
        (caller.findViewById(R.id.tv_expense) as TextView).setOnClickListener {
            if (type == Constants.INCOME) {
                // select "expense" if cash is not selected
                type = Constants.EXPENSE
                // change background color of expense textview to "selected"
                it.setBackgroundResource(R.drawable.termsandtype_selected)
                // change background color of income textview to "unselected"
                caller.findViewById(R.id.tv_income).setBackgroundResource(R.drawable.termsandtype_noselected)
            }
        }
        // date button
        (caller.findViewById(R.id.rl_date) as TextView).setOnClickListener {
            // fragment
            val calFragment = CalendarDialogFragment()
            // set year, month, day when "date" is tapped
            val args = Bundle()
            args.putInt("YEAR_BEFORE", selectedDate.year)
            args.putInt("MONTH_BEFORE", selectedDate.month)
            args.putInt("DAY_BEFORE", selectedDate.day)
            // pass arguments to fragment
            calFragment.setArguments(args)
            // prohibit cancel with "return" button
            calFragment.setCancelable(false)
            // show fragment(dialog)
            calFragment.show(caller.supportFragmentManager, "dialog")
        }
        super.onViewCreated(view, savedInstanceState)
    }

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
        // category
        (caller.caller.findViewById(R.id.et_category) as EditText).setText("")
        // set inputTarget to category
        inputTarget = Constants.CATEGORY
        // show inputlist
        setInputList(inputTarget)
        // detail
        (caller.findViewById(R.id.et_detail) as EditText).setText("")
        // income / expense
        type = Constants.EXPENSE
        caller.findViewById(R.id.tv_expense).setBackgroundResource(R.drawable.termsandtype_selected)
        caller.findViewById(R.id.tv_income).setBackgroundResource(R.drawable.termsandtype_noselected)
        // price
        priceStack.clear()
        priceStack.addLast('0')
        priceStack.printPrice()
        // set focus on price
        caller.findViewById(R.id.tv_priceValue).requestFocus()
        // cash / card
        termsOfPayment = Constants.CARD
        caller.findViewById(R.id.tv_card).setBackgroundResource(R.drawable.termsandtype_selected)
        caller.findViewById(R.id.tv_cash).setBackgroundResource(R.drawable.termsandtype_noselected)
    }
    //===
    //=== show value of priceStack
    //===

    // 4/3作り直し
    // 再帰を使って書き直したい

    fun Deque<Char>.printPrice(): String {
        // activity which calls this fragment
        val caller = activity
        // string builder
        val priceString = StringBuilder()
        // temporary deque 
        val tempStack = ArrayDeque(this)

        for (i in this.indices) {
            if (i != 0 && i % 3 == 0) {
                // insert ',' every three digits
                priceString.append(',')
            }
            priceString.append(tempStack.removeLast())
        }
        // add '￥' at the end
        priceString.append('￥')
        // reverse order
        priceString.reverse()
        return priceString.toString()
    }
/*
    fun printPriceStack() {
        // activity which calls this fragment
        val caller = activity
        // string builder
        val priceString = StringBuilder()
        // 表示用のDequeコピー
        val tempStack = ArrayDeque(priceStack)

        for (i in priceStack.indices) {
            if (i != 0 && i % 3 == 0) {
                // insert ',' every three digits
                priceString.append(',')
            }
            priceString.append(tempStack.removeLast())
        }
        // add '￥' at the end
        priceString.append('￥')
        // reverse order
        priceString.reverse()
        // set priceString to textView
        (caller.findViewById(R.id.tv_priceValue) as TextView).text = priceString.toString()
    }
*/
    //===
    //=== store price to stack
    //===

    // 4/3作り直し
    // 再帰を使って書き直したい

    fun Deque<Char>.setPrice(price: Int) {
        // change price to type array of char
        val priceArray = price.toString().toCharArray()
        // initialize deque
        this.clear()
        // push array to deque
        for (ch in priceArray) {
            this.addLast(ch)
        }
    }

    /*
        fun setPriceStack(price: Int) {
            // 金額を文字列に変換
            val priceString = Integer.toString(price)
            // 金額の文字列をchar型配列に変換
            val priceArray = priceString.toCharArray()
            // PriceStackを初期化
            priceStack.clear()
            // char型配列をpriceStackに格納
            for (ch in priceArray) {
                priceStack.addLast(ch)
            }
        }
    */
    //===
    //=== validation
    //===
    fun checkInput():Boolean{
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
