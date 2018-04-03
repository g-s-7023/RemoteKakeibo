package g_s_org.androidapp.com.remotekakeibo.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import g_s_org.androidapp.com.remotekakeibo.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // set fragment
        val fragment = supportFragmentManager.findFragmentById(R.id.container)
        supportFragmentManager.beginTransaction().replace(R.id.container, )






    }
}
