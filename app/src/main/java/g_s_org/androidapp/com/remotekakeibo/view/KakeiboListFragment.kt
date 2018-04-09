package g_s_org.androidapp.com.remotekakeibo.view

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import g_s_org.androidapp.com.remotekakeibo.R
import g_s_org.androidapp.com.remotekakeibo.common.Constants
import g_s_org.androidapp.com.remotekakeibo.model.FragmentToActivityInterection
import g_s_org.androidapp.com.remotekakeibo.model.KakeiboListAdapter
import g_s_org.androidapp.com.remotekakeibo.model.KakeiboListItem
import g_s_org.androidapp.com.remotekakeibo.view.dummy.DummyContent
import g_s_org.androidapp.com.remotekakeibo.view.dummy.DummyContent.DummyItem
import java.util.*


// ページ遷移


class KakeiboListFragment : Fragment(), DatePickerDialogFragment.DatePickerCallback, KakeiboListAdapter.OnKakeiboListItemClickListener {
    private lateinit var mCaller: FragmentActivity
    private var yearToList: Int = Constants.DEFAULT_YEAR
    private var monthToList: Int = 1

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (mCaller is FragmentActivity) {
            mCaller = context as FragmentActivity
        } else {
            throw UnsupportedOperationException("caller should be Fragment Activity")
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_kakeibolist, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        // initialize values and views
        initValues()
        // set listeners
        setListeners(mCaller)
        // get KakeiboList
        val kakeiboList = getKakeiboList(y, m)
        // set recycler view
        val list: View = view?.findViewById(R.id.list) as View
        if (list is RecyclerView) {

        }
    }

    fun initValues() {
        if (arguments != null && arguments.containsKey("LISTED_YEAR") && arguments.containsKey("LISTED_MONTH")) {
            yearToList = arguments.getInt("YEAR_TOLIST")
            monthToList = arguments.getInt("MONTH_TOLIST")
        } else {
            val cal = Calendar.getInstance()
            yearToList = cal.get(Calendar.YEAR)
            monthToList = cal.get(Calendar.MONTH)
        }
    }

    fun setListeners(a: FragmentActivity) {

    }

    //===
    //=== listeners
    //===
    // year and month
    fun onYearOrMonthClicked(y: Int, d: Int) {
        // show DatePickerDialogFragment
        DatePickerDialogFragment.newInstance(y, d).show(childFragmentManager, "dialog")
    }
    // previous month

    // next month

    // row
    override fun onItemClicked(a: Activity, item: KakeiboListItem) {
        if (!item.isSummary) {
            // set fragment
            val fragment = KakeiboUpdateFragment.newInstance(item.id, item.date.year, item.date.month,
                    item.date.day, item.date.dayOfWeek, item.category, item.type, item.price, item.detail, item.termsOfPayment)
            // move to update fragment
            if (a is FragmentToActivityInterection) {
                a.changePage(fragment)
            } else {
                throw UnsupportedOperationException("Listener is not implemented")
            }
        }
    }

    //===
    //=== get KakeiboList
    //===
    fun getKakeiboList(y: Int, m: Int): List<KakeiboListItem> {
        val list: MutableList<KakeiboListItem> = mutableListOf()

        // 記載

        return list
    }

    //===
    //=== callback from calendar dialog
    //===
    override fun onDialogYearMonthSelected(y: Int, m: Int) {
        // activity which calls this fragment
        val a = activity
        // set year, month ,day
        yearToList = y
        monthToList = m
        // show
        (a.findViewById(R.id.tv_year_list) as TextView).text = y.toString()
        (a.findViewById(R.id.tv_month_list) as TextView).text = m.toString()
    }

    companion object {
        fun newInstance(y: Int, m: Int): KakeiboListFragment {
            // fragment
            val fragment = KakeiboListFragment()
            // set year and month when "date" is tapped
            val args = Bundle()
            args.putInt("YEAR_TOLIST", y)
            args.putInt("MONTH_TOLIST", m)
            // pass arguments to fragment
            fragment.arguments = args
            // return fragment
            return fragment
        }
    }
}
