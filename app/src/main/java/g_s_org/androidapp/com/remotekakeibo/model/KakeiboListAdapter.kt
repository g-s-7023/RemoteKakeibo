package g_s_org.androidapp.com.remotekakeibo.model

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import g_s_org.androidapp.com.remotekakeibo.R
import g_s_org.androidapp.com.remotekakeibo.common.Constants


class KakeiboListAdapter(private val mValues: MutableList<KakeiboListItem>, private val mListener: OnKakeiboListItemClickListener) : RecyclerView.Adapter<KakeiboListAdapter.ViewHolder>() {
    // create view holder with layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_kakeiboitem, parent, false)
        return ViewHolder(view)
    }

    // bind values and listeners to view holder
    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        when(mValues[pos].isSummary) {
            true -> {
                // summary of a day
                setHeader(holder, mValues[pos])
            }
            false -> {
                // data row
                setData(holder, mValues[pos])
            }
        }
    }
    
    // set values to view holder as header
    private fun setHeader(holder:ViewHolder, item:KakeiboListItem) {
        // display header layout
        holder.headerLayout.visibility=View.VISIBLE
        // hide data layout
        holder.imageLayout.visibility=View.GONE
        holder.categoryAndDetailLayout.visibility=View.GONE
        holder.priceLayout.visibility=View.GONE
        // set date
        holder.date.text = item.date.toString()
        // set income of the day
        holder.subtotalIncome.text = item.subtotalIncome.toString()
        // set expense of the day
        holder.subtotalExpense.text = item.subtotalExpense.toString()
    }

    // set values and listener to view holder as data row
    private fun setData(holder:ViewHolder, item:KakeiboListItem){
        // hide header
        holder.headerLayout.visibility = View.GONE
        // display data
        holder.imageLayout.visibility = View.VISIBLE
        holder.categoryAndDetailLayout.visibility = View.VISIBLE
        holder.priceLayout.visibility = View.VISIBLE
        // set image corresponding to type
        holder.type.setImageResource(when (item.type) {
            Constants.INCOME -> R.drawable.`in`
            else -> R.drawable.out
        })
        // set category
        holder.category.text = item.category
        // set detail
        holder.detail.text = item.detail
        // set price
        holder.price.text = item.price.toString()
        // set listener
        holder.mView.setOnClickListener {
            mListener.onItemClicked(item)
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        // income / expense
        val type = mView.findViewById(R.id.iv_listType) as ImageView
        // category
        val category = mView.findViewById(R.id.tv_listCategory) as TextView
        // price
        val price = mView.findViewById(R.id.tv_listPrice) as TextView
        // detail
        val detail = mView.findViewById(R.id.tv_listDetail) as TextView
        // date (for summary)
        val date = mView.findViewById(R.id.tv_listDate) as TextView
        // subtotal of income (for summary)
        val subtotalIncome = mView.findViewById(R.id.tv_listSubtotalIncome) as TextView
        // subtotal of expense (for summary)
        val subtotalExpense = mView.findViewById(R.id.tv_listSubtotalExpense) as TextView
        // layout of header (for summary)
        val headerLayout = mView.findViewById(R.id.rl_header) as RelativeLayout
        // layout of image
        val imageLayout = mView.findViewById(R.id.ll_image) as LinearLayout
        // layout of category and detail
        val categoryAndDetailLayout = mView.findViewById(R.id.ll_categoryAndDetails) as LinearLayout
        // layout of price
        val priceLayout = mView.findViewById(R.id.ll_listPrice) as LinearLayout
    }

    interface OnKakeiboListItemClickListener {
        fun onItemClicked(item: KakeiboListItem)
    }

}
