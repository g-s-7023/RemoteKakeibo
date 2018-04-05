package g_s_org.androidapp.com.remotekakeibo.model

import android.app.Activity
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import g_s_org.androidapp.com.remotekakeibo.R
import g_s_org.androidapp.com.remotekakeibo.common.DBAccessHelper


/**
 * Created by C170044 on 2018/04/05.
 */
class KakeiboDBAccess {
    fun kakeiboInsert(a: Activity, cv:ContentValues){
        // create helper
        val helper = DBAccessHelper(a)
        // db
        var db: SQLiteDatabase? = null
        try {
            // open db
            db = helper.getWritableDatabase()
            // begin transaction
            db.beginTransaction()
            // exec insert
            db.insert(DBAccessHelper.TABLE_NAME, null, cv)
            // commit
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            // display toast if error
            Toast.makeText(a, R.string.msg_dberror, Toast.LENGTH_SHORT).show()
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