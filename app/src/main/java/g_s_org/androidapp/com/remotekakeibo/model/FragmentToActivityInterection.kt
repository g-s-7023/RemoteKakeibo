package g_s_org.androidapp.com.remotekakeibo.model

import android.support.v4.app.Fragment


interface FragmentToActivityInterection {
    fun changePage(to: Fragment)
    fun backPage()
}