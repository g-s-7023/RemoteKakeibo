package g_s_org.androidapp.com.remotekakeibo.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.KeyEvent
import g_s_org.androidapp.com.remotekakeibo.R
import g_s_org.androidapp.com.remotekakeibo.common.Constants.Companion.RC_GET_TOKEN
import g_s_org.androidapp.com.remotekakeibo.model.FragmentToActivityInterection
import g_s_org.androidapp.com.remotekakeibo.model.hundleSignInResult

class MainActivity : AppCompatActivity(), FragmentToActivityInterection {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // set view
        setContentView(R.layout.activity_main)
        // set fragment
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, KakeiboAddFragment())
                .commit()
    }

    override fun changeFragment(to: Fragment) {
        // replace current fragment for "to"
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, to).addToBackStack(null).commit()
    }

    override fun backFragment() {
        // remove current fragment and pop backstack
        val manager = supportFragmentManager
        manager.popBackStack()
    }

    /**
     * get result from activity(called after google sign in)
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_GET_TOKEN -> hundleSignInResult(data)
        }
    }

}
