package g_s_org.androidapp.com.remotekakeibo.model

import android.app.Activity
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.util.SparseArray
import android.widget.Toast
import g_s_org.androidapp.com.remotekakeibo.R
import g_s_org.androidapp.com.remotekakeibo.common.Constants
import g_s_org.androidapp.com.remotekakeibo.common.KakeiboItemForSync

class KakeiboDBAccess(val a:Activity) {

    fun insertKakeibo(cv: ContentValues) {
        execWrite { db: SQLiteDatabase ->
            db.insert(DBAccessHelper.TABLE_NAME, null, cv)
        }
    }

    // delete entry (logically)
    fun deleteKakeibo(id: Int) {
        val cv = ContentValues()
        cv.put("isDeleted", Constants.TRUE)
        execWrite { db: SQLiteDatabase ->
            db.update(DBAccessHelper.TABLE_NAME, cv, "_id = ?", arrayOf(id.toString()))
        }
    }

    fun updateKakeibo(id: Int, cv: ContentValues) {
        execWrite{ db: SQLiteDatabase ->
            db.update(DBAccessHelper.TABLE_NAME, cv, "_id = ?", arrayOf(id.toString()))
        }
    }

    fun syncInsert(items:MutableList<ContentValues>){
        execWrite { db: SQLiteDatabase ->
            for (item in items) {
                db.insert(DBAccessHelper.TABLE_NAME, null, item)
            }
        }
    }

    fun syncUpdate(items:MutableList<KakeiboItemForSync>){
        execWrite{ db: SQLiteDatabase ->
            for (item in items) {
                db.update(DBAccessHelper.TABLE_NAME, item.cv, "_id = ?", arrayOf(item.id.toString()))
            }
        }
    }

    fun syncDelete(items:MutableList<KakeiboItemForSync>){
        execWrite{ db: SQLiteDatabase ->
            for (item in items) {
                db.update(DBAccessHelper.TABLE_NAME, item.cv, "_id = ?", arrayOf(item.id.toString()))
            }
        }
    }

    fun setSynchronized(ids:MutableList<Int>){
        val cv = ContentValues()
        cv.put("isSynchronized", Constants.TRUE)
        execWrite { db: SQLiteDatabase ->
            for (id in ids){
                db.update(DBAccessHelper.TABLE_NAME, cv, "_id = ?", arrayOf(id.toString()))
            }
        }
    }

    fun readKakeiboOfMonth(year: Int, month: Int): Cursor? {
        return execRead { db: SQLiteDatabase ->
            db.query(DBAccessHelper.TABLE_NAME,
                    arrayOf("_id", "year", "month", "day", "dayOfWeek", "category", "type", "price", "detail", "termsOfPayment", "isSynchronized"),
                    "year = ? AND month = ? AND isDeleted = ?",
                    arrayOf(year.toString(), month.toString(), Constants.FALSE.toString()),
                    null, null, "day ASC")
        }
    }

    fun readUnsynchronizedEntry(): Cursor? {
        return execRead { db: SQLiteDatabase ->
            db.query(DBAccessHelper.TABLE_NAME,
                    arrayOf("_id", "year", "month", "day", "dayOfWeek", "category", "type", "price", "detail", "termsOfPayment"),
                    "isSynchronized = ?",
                    arrayOf(Integer.toString(Constants.FALSE)),
                    null, null, null)
        }
    }

    // execute function passed
    private fun execWrite(f: (SQLiteDatabase) -> Unit) {
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

    private fun execRead(f: (SQLiteDatabase) -> Cursor?): Cursor? {
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
