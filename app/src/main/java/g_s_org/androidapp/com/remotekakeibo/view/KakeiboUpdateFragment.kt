package g_s_org.androidapp.com.remotekakeibo.view

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import g_s_org.androidapp.com.remotekakeibo.R
import g_s_org.androidapp.com.remotekakeibo.common.Constants
import g_s_org.androidapp.com.remotekakeibo.common.DBAccessHelper
import g_s_org.androidapp.com.remotekakeibo.common.KakeiboDate
import g_s_org.androidapp.com.remotekakeibo.dbaccess.DetailHistoryAccess
import g_s_org.androidapp.com.remotekakeibo.model.KakeiboDBAccess
import g_s_org.androidapp.com.remotekakeibo.model.getPrice
import g_s_org.androidapp.com.remotekakeibo.model.setPrice
import org.w3c.dom.Text
import java.util.*

class KakeiboUpdateFragment : KakeiboInputFragment() {
    // ID of selected entry
    var selectedId: Int = -1

    //===
    //=== on view created
    //===
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        this.initValues(mCaller)
        super.setListeners(mCaller)
        super.onViewCreated(view, savedInstanceState)
    }

    //===
    //=== initialize value of each view and field
    //===
    override fun initValues(a: FragmentActivity) {
        // get parameters from list item
        selectedId = arguments.getInt("SELECTED_ID")
        selectedDate.setDate(
                arguments.getInt("SELECTED_YEAR"),
                arguments.getInt("SELECTED_MONTH"),
                arguments.getInt("SELECTED_DAY"),
                arguments.getInt("SELECTED_DAYOFWEEK"))
        priceStack.setPrice(arguments.getInt("SELECTED_PRICE"))
        val category = arguments.getString("SELECTED_CATEGORY")
        val type = arguments.getInt("SELECTED_TYPE")
        val detail = arguments.getString("SELECTED_DETAIL")
        val termsOfPayment = arguments.getInt("SELECTED_TERMSOFPAYMENT")
        // set text in category textbox
        (a.findViewById(R.id.et_category) as EditText).setText(category)
        // set text in detail textbox
        (a.findViewById(R.id.et_detail) as EditText).setText(detail)
        // select category
        onCategorySelected(a)
        // select income or expense
        when (type) {
            Constants.INCOME -> onIncomeSelected(a.findViewById(R.id.tv_income) as TextView, a)
            Constants.EXPENSE -> onExpenseSelected(a.findViewById(R.id.tv_expense) as TextView, a)
        }
        // select cash or card
        when (termsOfPayment) {
            Constants.CASH -> onCashSelected(a.findViewById(R.id.tv_cash) as TextView, a)
            Constants.CARD -> onCardSelected(a.findViewById(R.id.tv_card) as TextView, a)
        }
        // price (set 0)
        (a.findViewById(R.id.tv_priceValue) as TextView).text = priceStack.getPrice()
        // date (set today)
        selectedDate.setDate(Calendar.getInstance())
        (a.findViewById(R.id.tv_year) as TextView).text = getString(R.string.show_year, selectedDate.year)
        (a.findViewById(R.id.tv_monthAndDay) as TextView).text = getString(R.string.show_monthday, selectedDate.month, selectedDate.day)
        (a.findViewById(R.id.tv_dayOfWeek) as TextView).text = getString(R.string.show_dayofweek, Constants.WEEKNAME[selectedDate.dayOfWeek - 1])
        // set focus on price (not to show keyboard)
        (a.findViewById(R.id.tv_priceValue) as TextView).requestFocus()
    }

    //===
    //=== functions run when each view is selected
    //===
    // delete button
    override fun onLeftButtonClicked(a: FragmentActivity) {
        // delete from DB
        KakeiboDBAccess().exec(a) { db: SQLiteDatabase ->
            db.delete(DBAccessHelper.TABLE_NAME, "_id = ?", arrayOf(selectedId.toString()))
        }
        // back to list
        pageBack(a)
    }

    // cancel button
    override fun onRightButtonClicked(a: FragmentActivity) {
        // back to list
        pageBack(a)
    }

    // update button
    override fun onCenterButtonClicked(a: FragmentActivity) {
        // contentValues to insert
        val cv = getContentValues(a)
        // update DB
        KakeiboDBAccess().exec(a){db:SQLiteDatabase ->
            db.update(DBAccessHelper.TABLE_NAME, cv, "_id = ?", arrayOf(selectedId.toString()))
        }
        // back to list
        pageBack(a)
    }

    //===
    //=== back to list
    //===
    fun pageBack(a: FragmentActivity) {
        if (a is OnFragmentInteractionListener){
            a.backPage()
        } else {
            throw UnsupportedOperationException("Listener is not implemented")
        }
    }

}
