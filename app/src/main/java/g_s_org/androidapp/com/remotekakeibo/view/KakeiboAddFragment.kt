package g_s_org.androidapp.com.remotekakeibo.view

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import g_s_org.androidapp.com.remotekakeibo.R
import g_s_org.androidapp.com.remotekakeibo.common.Constants
import g_s_org.androidapp.com.remotekakeibo.common.Constants.Companion.NO_ID
import g_s_org.androidapp.com.remotekakeibo.common.DBAccessHelper
import g_s_org.androidapp.com.remotekakeibo.dbaccess.DetailHistoryAccess
import g_s_org.androidapp.com.remotekakeibo.model.KakeiboDBAccess
import g_s_org.androidapp.com.remotekakeibo.model.getPrice
import java.util.*


class KakeiboAddFragment : KakeiboInputFragment() {

    //===
    //=== on view created
    //===
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        // activity which call this fragment
        this.initValues(mCaller)
        super.setListeners(mCaller)
        super.onViewCreated(view, savedInstanceState)
    }
    //===
    //=== initialize value of each view and field
    //===
    override fun initValues(a: FragmentActivity) {
        // clear text in category textbox
        (a.findViewById(R.id.et_category) as EditText).setText("")
        // clear text in detail textbox
        (a.findViewById(R.id.et_detail) as EditText).setText("")
        // select category
        onCategorySelected(a)
        // select "expense"
        onExpenseSelected(a.findViewById(R.id.tv_expense), a)
        // select "cash"
        onCashSelected(a.findViewById(R.id.tv_cash), a)
        // price (set 0)
        priceStack.clear()
        priceStack.addLast('0')
        (a.findViewById(R.id.tv_priceValue) as TextView).text = priceStack.getPrice()
        // date (set today)
        selectedDate.setDate(Calendar.getInstance())
        (a.findViewById(R.id.tv_year) as TextView).text = getString(R.string.show_year, selectedDate.year)
        (a.findViewById(R.id.tv_monthAndDay) as TextView).text = getString(R.string.show_monthday, selectedDate.month, selectedDate.day)
        (a.findViewById(R.id.tv_dayOfWeek) as TextView).text =  getString(R.string.show_dayofweek, Constants.WEEKNAME[selectedDate.dayOfWeek - 1])
        // set focus on price (not to show keyboard)
        (a.findViewById(R.id.tv_priceValue) as TextView).requestFocus()
        // set center button invisible
        (a.findViewById(R.id.bt_center) as Button).visibility = View.INVISIBLE
    }

    //===
    //=== functions run when each view is selected
    //===
    // save button
    override fun onLeftButtonClicked(a: FragmentActivity) {
        // contentValues to insert
        val cv = getContentValues(a)
        // insert to DB
        KakeiboDBAccess().exec(a){db:SQLiteDatabase->
            db.insert(DBAccessHelper.TABLE_NAME, null, cv)
        }
        // save detail to preference
        DetailHistoryAccess().savePreference((a.findViewById(R.id.et_detail) as EditText).text.toString(), a)
        // inititalize values(except date)
        initValues(a)
    }

    // list button
    override fun onRightButtonClicked(a: FragmentActivity) {
        // fragment to replace for
        val toFragment = KakeiboListFragment()
        // set argument(date)
        val args = Bundle()
        args.putInt("SELECTED_YEAR", selectedDate.year)
        args.putInt("SELECTED_MONTH", selectedDate.month)
        toFragment.arguments = args
        // change page
        if (a is OnFragmentInteractionListener){
            a.changePage(toFragment)
        } else {
            throw UnsupportedOperationException("Listener is not implemented")
        }
    }

    // hidden (no function)
    override fun onCenterButtonClicked(a: FragmentActivity) {}
}
