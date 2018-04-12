package g_s_org.androidapp.com.remotekakeibo.model

import android.support.v4.app.Fragment


interface FragmentToActivityInterection {
    fun changeFragment(to: Fragment)
    fun backFragment()
}