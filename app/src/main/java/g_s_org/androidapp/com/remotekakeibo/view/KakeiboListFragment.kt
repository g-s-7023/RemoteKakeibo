package g_s_org.androidapp.com.remotekakeibo.view

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import g_s_org.androidapp.com.remotekakeibo.R
import g_s_org.androidapp.com.remotekakeibo.view.dummy.DummyContent
import g_s_org.androidapp.com.remotekakeibo.view.dummy.DummyContent.DummyItem
import java.util.*


// 現在の年月をonSaveInstanceState()で保持させる
// ページ遷移




class KakeiboListFragment : Fragment() {
    private lateinit var mCaller: Activity
    private var listedYear:Int = 1900
    private var listedMonth:Int = 1

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mCaller = context as Activity
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_kakeibolist, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?){

    }

    fun initValues(savedInstanceState: Bundle?){
        if (savedInstanceState != null && savedInstanceState.containsKey("LISTED_YEAR") && savedInstanceState.containsKey("LISTED_MONTH")){
            listedYear = savedInstanceState.getInt("LISTED_YEAR")
            listedMonth = savedInstanceState.getInt("LISTED_MONTH")
        } else {
            val cal = Calendar.getInstance()
            listedYear = cal.get(Calendar.YEAR)
            listedMonth = cal.get(Calendar.MONTH)
        }
    }


    interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onListFragmentInteraction(item: DummyItem)
    }

    companion object {

        // TODO: Customize parameter argument names
        private val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        fun newInstance(columnCount: Int): KakeiboListFragment {
            val fragment = KakeiboListFragment()
            val args = Bundle()
            args.putInt(ARG_COLUMN_COUNT, columnCount)
            fragment.arguments = args
            return fragment
        }
    }
}
