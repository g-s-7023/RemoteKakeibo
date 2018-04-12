package g_s_org.androidapp.com.remotekakeibo.view

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.View
import android.widget.EditText
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
        this.setViews(mCaller)
        super.setListeners(mCaller)
        super.onViewCreated(view, savedInstanceState)
    }
    //===
    //=== initialize values
    //===
    override fun initValues() {
        priceStack.setPrice(arguments.getInt("SELECTED_PRICE"))
        selectedDate.setDate(
                arguments.getInt("SELECTED_YEAR"),
                arguments.getInt("SELECTED_MONTH"),
                arguments.getInt("SELECTED_DAY"),
                arguments.getInt("SELECTED_DAYOFWEEK"))
        inputTarget = Constants.CATEGORY
        termsOfPayment = arguments.getInt("SELECTED_TERMSOFPAYMENT")
        type = arguments.getInt("SELECTED_TYPE")
        selectedId = arguments.getInt("SELECTED_ID")
    }

    //===
    //=== initialize value of each view and field
    //===
    override fun setViews(a: FragmentActivity) {
        // set text in category textbox
        (a.findViewById(R.id.et_category) as EditText).setText(arguments.getString("SELECTED_CATEGORY"))
        // set text in detail textbox
        (a.findViewById(R.id.et_detail) as EditText).setText(arguments.getString("SELECTED_DETAIL"))
        // select category
        setCategory()
        // select income or expense
        when (type) {
            Constants.INCOME -> setIncome()
            Constants.EXPENSE -> setExpense()
        }
        // select cash or card
        when (termsOfPayment) {
            Constants.CASH -> setCash()
            Constants.CARD -> setCard()
        }
        // price
        (a.findViewById(R.id.tv_priceValue) as TextView).text = priceStack.getPrice()
        // date
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
        KakeiboDBAccess().execWrite(a) { db: SQLiteDatabase ->
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
        KakeiboDBAccess().execWrite(a){ db:SQLiteDatabase ->
            db.update(DBAccessHelper.TABLE_NAME, cv, "_id = ?", arrayOf(selectedId.toString()))
        }
        // back to list
        pageBack(a)
    }

    //===
    //=== back to list
    //===
    fun pageBack(a: FragmentActivity) {
        if (a is FragmentToActivityInterection){
            a.backPage()
        } else {
            throw UnsupportedOperationException("Listener is not implemented")
        }
    }

    companion object {
        fun newInstance(id:Int, y:Int, m:Int, day:Int, dOfW:Int, c:String, t:Int, p:Int, det:String, tOfP:Int):KakeiboUpdateFragment{
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
