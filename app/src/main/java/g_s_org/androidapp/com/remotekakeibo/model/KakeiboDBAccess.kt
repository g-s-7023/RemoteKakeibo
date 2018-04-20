package g_s_org.androidapp.com.remotekakeibo.model

import android.app.Activity
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.widget.Toast
import g_s_org.androidapp.com.remotekakeibo.R
import g_s_org.androidapp.com.remotekakeibo.common.Constants

class KakeiboDBAccess {

    fun insertKakeibo(a: Activity, cv:ContentValues) {
        execWrite(a) { db: SQLiteDatabase ->
            db.insert(DBAccessHelper.TABLE_NAME, null, cv)
        }
    }

    fun deleteKakeibo(a: Activity, id:Int){
        execWrite(a) { db: SQLiteDatabase ->
            db.delete(DBAccessHelper.TABLE_NAME, "_id = ?", arrayOf(id.toString()))
        }
    }

    fun updateKakeibo(a: FragmentActivity, id:Int, cv: ContentValues) {
        execWrite(a) { db: SQLiteDatabase ->
            db.update(DBAccessHelper.TABLE_NAME, cv, "_id = ?", arrayOf(id.toString()))
        }
    }

    fun readKakeiboOfMonth(a: Activity, year: Int, month: Int): Cursor? {
        return execRead(a) { db: SQLiteDatabase ->
            db.query(DBAccessHelper.TABLE_NAME,
                    arrayOf("_id", "year", "month", "day", "dayOfWeek", "category", "type", "price", "detail", "termsOfPayment"),
                    "year = ? AND month = ?",
                    arrayOf(year.toString(), month.toString()),
                    null, null, "day ASC")
        }
    }

    fun readUnsynchronizedEntry(a: Activity): Cursor? {
        return execRead(a) { db: SQLiteDatabase ->
            db.query(DBAccessHelper.TABLE_NAME,
                    arrayOf("_id", "year", "month", "day", "dayOfWeek", "category", "type", "price", "detail", "termsOfPayment"),
                    "isSynchronized = ?",
                    arrayOf(Integer.toString(Constants.FALSE)),
                    null, null, null)
        }
    }

    // execute function passed
    private fun execWrite(a: Activity, f: (SQLiteDatabase) -> Unit) {
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

    private fun execRead(a: Activity, f: (SQLiteDatabase) -> Cursor?): Cursor? {
        // create helper
        val helper = DBAccessHelper(a)
        // DB
        var db: SQLiteDatabase? = null
        // cursor
        var cursor: Cursor? = null
        try {
            // データベースオブジェクト取得
            db = helper.readableDatabase
            // データ取得
            cursor = f(db)
        } catch (e: Exception) {
            throw e
        } finally {
            return cursor
        }
    }
}
