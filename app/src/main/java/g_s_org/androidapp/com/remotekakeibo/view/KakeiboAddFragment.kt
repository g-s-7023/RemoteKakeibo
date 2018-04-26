package g_s_org.androidapp.com.remotekakeibo.view

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
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


class KakeiboAddFragment : KakeiboInputFragment() {
    //===
    //=== mCallback from lifecycle
    //===
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initValues()
        setListeners()
    }

    //===
    //=== initialize view and values
    //===
    override fun initValues() {
        // date (set today)
        setToday()
        // set button name
        (mCaller.findViewById(R.id.bt_left) as Button).text = getString(R.string.bt_save)
        (mCaller.findViewById(R.id.bt_right) as Button).text = getString(R.string.bt_list)
        resetValues()
    }

    private fun resetValues() {
        // price (set 0)
        clearPrice()
        // clear text in category textbox
        (mCaller.findViewById(R.id.et_category) as EditText).setText("")
        // clear text in detail textbox
        (mCaller.findViewById(R.id.et_detail) as EditText).setText("")
        // select category
        setCategory()
        // select "cash"
        setCash()
        // select "expense"
        setExpense()
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
        val category = (mCaller.findViewById(R.id.et_category) as EditText).text.toString()
        val detail = (mCaller.findViewById(R.id.et_detail) as EditText).text.toString()
        // if category is empty, no data is saved
        if (category.isNotBlank()) {
            // contentValues to insert
            val cv = getContentValues(category, detail, selectedDate, priceStack, condition)
            // insert
            saveData(mCaller, cv, detail)
        }
    }

    // list button
    override fun onRightButtonClicked() {
        changePage(mCaller, selectedDate.year, selectedDate.month)
    }

    // hidden (no function)
    override fun onCenterButtonClicked() {}

    // enter key(insert)
    override fun onEnter(text: EditText, key: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && key == KeyEvent.KEYCODE_ENTER) {
            /*
            // contentValues to insert
            val cv = getContentValues(category, detail, selectedDate, priceStack, condition)
            // insert
            saveData(mCaller, cv, detail)
            */
            // close software keyboard
            (mCaller.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(text.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
            return true
        }
        return false
    }

    //===
    //=== business logic
    //===
    fun changePage(a: Activity, y: Int, m: Int) {
        // fragment to replace for
        val toFragment = KakeiboListFragment.newInstance(y, m)
        // change page
        if (a is FragmentToActivityInterection) {
            a.changeFragment(toFragment)
        } else {
            throw UnsupportedOperationException("Listener is not implemented")
        }
    }

    fun saveData(a: Activity, cv: ContentValues, d:String) {
        // insert to DB
        KakeiboDBAccess(a).insertKakeibo(cv)
        // save detail to preference
        if (d.isNotBlank()) {
            DetailHistoryAccess(a).savePreference(d)
        }
        // initialize values(except date)
        resetValues()
    }

}
