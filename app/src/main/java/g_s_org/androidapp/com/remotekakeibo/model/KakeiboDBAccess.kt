package g_s_org.androidapp.com.remotekakeibo.model

import android.app.Activity
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import g_s_org.androidapp.com.remotekakeibo.R
import g_s_org.androidapp.com.remotekakeibo.common.DBAccessHelper

class KakeiboDBAccess {
    // execute function passed
    fun exec(a:Activity, f:(SQLiteDatabase) -> Unit){
        // create helper
        val helper = DBAccessHelper(a)
        // db
        var db: SQLiteDatabase? = null
        try {
            // open db
            db = helper.writableDatabase
            // begin transaction
            db.beginTransaction()
            // execution(insert, update, delete)
            f(db)
            // commit
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            // display toast if error
            Toast.makeText(a, R.string.msg_dbaccesserror, Toast.LENGTH_SHORT).show()
            // out put exception log
            Log.e("ERROR", e.toString())
        } finally {
            // end transaction
            db?.endTransaction()
            // close DB
            db?.close()
        }
    }
}
