package g_s_org.androidapp.com.remotekakeibo.model

import android.app.Activity
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.widget.Toast
import g_s_org.androidapp.com.remotekakeibo.R

class KakeiboDBAccess {
    // execute function passed
    fun execWrite(a: Activity, f: (SQLiteDatabase) -> Unit) {
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

    fun readAllKakeibo(a: Activity, year: Int, month: Int): Cursor? {
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
            cursor = db.query(DBAccessHelper.TABLE_NAME,
                    arrayOf("_id", "year", "month", "day", "dayOfWeek", "category", "type", "price", "detail", "termsOfPayment"),
                    "year = ? AND month = ?",
                    arrayOf(Integer.toString(year), Integer.toString(month)),
                    null, null, "day ASC")
        } catch (e: Exception) {
            throw e
        } finally {
            return cursor
        }
    }
}
