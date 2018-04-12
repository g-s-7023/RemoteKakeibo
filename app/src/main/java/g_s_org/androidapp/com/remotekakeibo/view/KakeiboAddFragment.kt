package g_s_org.androidapp.com.remotekakeibo.view

import android.app.Activity
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import g_s_org.androidapp.com.remotekakeibo.R
import g_s_org.androidapp.com.remotekakeibo.common.Constants
import g_s_org.androidapp.com.remotekakeibo.model.DBAccessHelper
import g_s_org.androidapp.com.remotekakeibo.dbaccess.DetailHistoryAccess
import g_s_org.androidapp.com.remotekakeibo.model.FragmentToActivityInterection
import g_s_org.androidapp.com.remotekakeibo.model.KakeiboDBAccess
import java.text.SimpleDateFormat
import java.util.*


class KakeiboAddFragment : KakeiboInputFragment() {
    //===
    //=== on view created
    //===
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        // activity which call this fragment
        initValues()
        setListeners()
        super.onViewCreated(view, savedInstanceState)
    }

    //===
    //=== initialize view and values
    //===
    override fun initValues() {
        // date (set today)
        setToday(mCaller.findViewById(R.id.tv_year) as TextView,
                mCaller.findViewById(R.id.tv_monthAndDay) as TextView,
                mCaller.findViewById(R.id.tv_dayOfWeek) as TextView,
                selectedDate)
        resetValues()
    }

    private fun resetValues(){
        // price (set 0)
        clearPrice(mCaller.findViewById(R.id.tv_priceValue) as TextView, priceStack)
        // clear text in category textbox
        (mCaller.findViewById(R.id.et_category) as EditText).setText("")
        // clear text in detail textbox
        (mCaller.findViewById(R.id.et_detail) as EditText).setText("")
        // select category
        setCategoryAndDetail(mCaller.findViewById(R.id.et_detail) as EditText,
                mCaller.findViewById(R.id.et_category) as EditText,
                mCaller.findViewById(R.id.lv_categoryAndDetail) as ListView,
                resources.getStringArray(R.array.lv_category_and_detail),
                condition, Constants.CATEGORY, mCaller)
        // select "cash"
        setCardAndCash(mCaller.findViewById(R.id.tv_card) as TextView,
                mCaller.findViewById(R.id.tv_cash) as TextView,
                condition, Constants.CASH)
        // select "expense"
        setIncomeAndExpense(mCaller.findViewById(R.id.tv_income) as TextView,
                mCaller.findViewById(R.id.tv_expense) as TextView,
                condition, Constants.EXPENSE)
        // set focus on price (not to show keyboard)
        (mCaller.findViewById(R.id.tv_priceValue) as TextView).requestFocus()
        // set center button invisible
        (mCaller.findViewById(R.id.bt_center) as Button).visibility = View.INVISIBLE
    }

    //===
    //=== listeners
    //===
    // save button
    override fun onLeftButtonClicked() {
        saveData(mCaller, mCaller.findViewById(R.id.et_detail) as EditText)
    }

    // list button
    override fun onRightButtonClicked() {
        changePage(mCaller, selectedDate.year, selectedDate.month)
    }

    // hidden (no function)
    override fun onCenterButtonClicked() {}

    //===
    //=== business logic
    //===
    private fun changePage(a: Activity, y:Int, m:Int){
        // fragment to replace for
        val toFragment = KakeiboListFragment.newInstance(y, m)
        // change page
        if (a is FragmentToActivityInterection){
            a.changeFragment(toFragment)
        } else {
            throw UnsupportedOperationException("Listener is not implemented")
        }
    }

    private fun saveData(a:Activity, dv:EditText){
        // contentValues to insert
        val cv = getContentValues((a.findViewById(R.id.et_category) as EditText).text.toString(),
                (a.findViewById(R.id.et_detail) as EditText).text.toString(),
                selectedDate, priceStack, condition)
        // insert to DB
        KakeiboDBAccess().execWrite(a){ db:SQLiteDatabase->
            db.insert(DBAccessHelper.TABLE_NAME, null, cv)
        }
        // save detail to preference
        DetailHistoryAccess().savePreference(dv.text.toString(), a)
        // initialize values(except date)
        resetValues()
    }

}
