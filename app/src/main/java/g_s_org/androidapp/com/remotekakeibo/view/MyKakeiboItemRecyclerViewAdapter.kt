package g_s_org.androidapp.com.remotekakeibo.view

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import g_s_org.androidapp.com.remotekakeibo.view.KakeiboListFragment.OnListFragmentInteractionListener
import g_s_org.androidapp.com.remotekakeibo.view.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class MyKakeiboItemRecyclerViewAdapter(private val mValues: List<DummyItem>, private val mListener: OnListFragmentInteractionListener?) : RecyclerView.Adapter<MyKakeiboItemRecyclerViewAdapter.ViewHolder>() {

    val itemCount: Int
        get() = mValues.size

    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_kakeiboitem, parent, false)
        return ViewHolder(view)
    }

    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        holder.mIdView.text = mValues[position].id
        holder.mContentView.text = mValues[position].content

        holder.mView.setOnClickListener {
            mListener?.onListFragmentInteraction(holder.mItem)
        }
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView
        val mContentView: TextView
        var mItem: DummyItem? = null

        init {
            mIdView = mView.findViewById(R.id.id) as TextView
            mContentView = mView.findViewById(R.id.content) as TextView
        }

        fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
