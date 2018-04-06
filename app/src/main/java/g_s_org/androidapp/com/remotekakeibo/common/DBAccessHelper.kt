package g_s_org.androidapp.com.remotekakeibo.common

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * Created by C170044 on 2018/04/05.
 */
class DBAccessHelper(ctx: Context) : SQLiteOpenHelper(ctx, DB_NAME, null, DB_VERSION) {
    companion object {
        // version of database
        const val DB_VERSION: Int = 1
        // name of database
        const val DB_NAME: String = "MyKakeiboDB"
        // table of database
        const val TABLE_NAME: String = "MyKakeibo"
        // for output log
        const val TAG = "ContactDbOpenHelper"
        // when re-define DB
        const val LOG_DBUPGRADE = "KakeiboAccessHelper.onUpgradeが呼ばれました"
    }

    // for create DB
    override fun onCreate(db: SQLiteDatabase) {
        // rid : id in table on the Web
        // kakeiboName : mykakeibo or family_kakeibo(not divide table)
        // isSynchronized : has already been synchronized to table on the Web
        val sql = "create table " + TABLE_NAME + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "rid INTEGER," +
                "kakeiboName TEXT not null," +
                "year INTEGER not null," +
                "month INTEGER not null," +
                "day INTEGER not null," +
                "dayOfWeek INTEGER not null," +
                "category TEXT not null," +
                "type INTEGER not null," +
                "price INTEGER not null," +
                "detail TEXT," +
                "termsOfPayment INTEGER," +
                "isSynchronized INTEGER)"
        db.execSQL(sql)
    }

    // for upgrade table
    override fun onUpgrade(db: SQLiteDatabase, oldversion: Int, newversion: Int) {
        // ログ出力
        Log.d(TAG, LOG_DBUPGRADE)
        try {
            // delete old table
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            // create new table
            onCreate(db)
        } catch (e: Exception) {
            Log.e("ERROR", e.toString())
        }
    }
}