package g_s_org.androidapp.com.remotekakeibo.view

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView

import g_s_org.androidapp.com.remotekakeibo.R
import g_s_org.androidapp.com.remotekakeibo.common.Constants
import g_s_org.androidapp.com.remotekakeibo.model.DBAccessHelper
import g_s_org.androidapp.com.remotekakeibo.model.FragmentToActivityInterection
import g_s_org.androidapp.com.remotekakeibo.model.KakeiboDBAccess
import g_s_org.androidapp.com.remotekakeibo.model.getPrice
import g_s_org.androidapp.com.remotekakeibo.model.setPrice

class KakeiboUpdateFragment : KakeiboInputFragment() {
    // ID of selected entry
    var selectedId: Int = -1
    //===
    //=== on view created
    //===
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        this.initValues()
        super.setListeners()
        super.onViewCreated(view, savedInstanceState)
    }
    //===
    //=== initialize value of each view and field
    //===
    override fun initValues() {
        // set button name
        (mCaller.findViewById(R.id.bt_left) as Button).text = getString(R.string.bt_delete)
        (mCaller.findViewById(R.id.bt_right) as Button).text = getString(R.string.bt_cancel)
        (mCaller.findViewById(R.id.bt_center) as Button).text = getString(R.string.bt_update)
        resetValues()
    }

    private fun resetValues(){
        // ID
        selectedId = arguments.getInt("SELECTED_ID")
        // date
        setDate(arguments.getInt("SELECTED_YEAR"),
                arguments.getInt("SELECTED_MONTH"),
                arguments.getInt("SELECTED_DAY"),
                mCaller.findViewById(R.id.tv_year) as TextView,
                mCaller.findViewById(R.id.tv_monthAndDay) as TextView,
                mCaller.findViewById(R.id.tv_dayOfWeek) as TextView,
                selectedDate)
        // price
        priceStack.setPrice(arguments.getInt("SELECTED_PRICE"))
        // category
        (mCaller.findViewById(R.id.et_category) as EditText).setText(arguments.getString("SELECTED_CATEGORY"))
        // detail
        (mCaller.findViewById(R.id.et_detail) as EditText).setText(arguments.getString("SELECTED_DETAIL"))
        // select category
        setCategoryAndDetail(mCaller.findViewById(R.id.et_detail) as EditText,
                mCaller.findViewById(R.id.et_category) as EditText,
                mCaller.findViewById(R.id.lv_categoryAndDetail) as ListView,
                resources.getStringArray(R.array.lv_category_and_detail),
                condition, Constants.CATEGORY, mCaller)
        // select card or cash
        when (arguments.get("SELECTED_TYPE")) {
            Constants.INCOME -> {
                setIncomeAndExpense(mCaller.findViewById(R.id.tv_expense) as TextView,
                        mCaller.findViewById(R.id.tv_income) as TextView,
                        condition, Constants.INCOME)
            }
            Constants.EXPENSE -> {
                setIncomeAndExpense(mCaller.findViewById(R.id.tv_income) as TextView,
                        mCaller.findViewById(R.id.tv_expense) as TextView,
                        condition, Constants.EXPENSE)
            }
        }
        // select income or expense
        when (arguments.get("SELECTED_TERMSOFPAYMENT")) {
            Constants.CASH -> {
                setCardAndCash(mCaller.findViewById(R.id.tv_card) as TextView,
                        mCaller.findViewById(R.id.tv_cash) as TextView,
                        condition, Constants.CASH)
            }
            Constants.CARD -> {
                setCardAndCash(mCaller.findViewById(R.id.tv_cash) as TextView,
                        mCaller.findViewById(R.id.tv_card) as TextView,
                        condition, Constants.CARD)
            }
        }
        // set focus on price (not to show keyboard)
        (mCaller.findViewById(R.id.tv_priceValue) as TextView).requestFocus()
    }

    //===
    //=== listeners
    //===
    // delete button
    override fun onLeftButtonClicked() {
        // delete from DB
        deleteData(mCaller, selectedId)
        // back to list
        pageBack(mCaller)
    }

    // cancel button
    override fun onRightButtonClicked() {
        // contentValues to update
        val cv = getContentValues((mCaller.findViewById(R.id.et_category) as EditText).text.toString(),
                (mCaller.findViewById(R.id.et_detail) as EditText).text.toString(),
                selectedDate, priceStack, condition)
        // update
        updateData(mCaller, selectedId, cv)
        // back to list
        pageBack(mCaller)
    }

    // update button
    override fun onCenterButtonClicked() {
        // back to list
        pageBack(mCaller)
    }
    //===
    //=== business logic
    //===
    private fun deleteData(a: FragmentActivity, id:Int) {
        KakeiboDBAccess().execWrite(a) { db: SQLiteDatabase ->
            db.delete(DBAccessHelper.TABLE_NAME, "_id = ?", arrayOf(id.toString()))
        }
    }

    private fun updateData(a: FragmentActivity, id:Int, cv: ContentValues) {
        // update DB
        KakeiboDBAccess().execWrite(a) { db: SQLiteDatabase ->
            db.update(DBAccessHelper.TABLE_NAME, cv, "_id = ?", arrayOf(id.toString()))
        }
    }

    private fun pageBack(a: FragmentActivity) {
        if (a is FragmentToActivityInterection) {
            a.backFragment()
        } else {
            throw UnsupportedOperationException("Listener is not implemented")
        }
    }
    //===
    //=== factory method
    //===
    companion object {
        fun newInstance(id: Int, y: Int, m: Int, day: Int, dOfW: Int, c: String, t: Int, p: Int, det: String, tOfP: Int): KakeiboUpdateFragment {
            // set fragment
            val fragment = KakeiboUpdateFragment()
            // set arguments
            val args = Bundle()
            args.putInt("SELECTED_ID", id)
            args.putInt("SELECTED_YEAR", y)
            args.putInt("SELECTED_MONTH", m)
            args.putInt("SELECTED_DAY", day)
            args.putInt("SELECTED_DAYOFWEEK", dOfW)
            args.putString("SELECTED_CATEGORY", c)
            args.putInt("SELECTED_TYPE", t)
            args.putInt("SELECTED_PRICE", p)
            args.putString("SELECTED_DETAIL", det)
            args.putInt("SELECTED_TERMSOFPAYMENT", tOfP)
            fragment.arguments = args
            return fragment
        }
    }

}
