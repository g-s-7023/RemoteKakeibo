package g_s_org.androidapp.com.remotekakeibo.dbaccess

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import g_s_org.androidapp.com.remotekakeibo.common.Constants

/**
 * Created by C170044 on 2018/04/03.
 */
class DetailHistoryAccess() {
    // preference name of detail history
    val DETAIL_HISTORY = "DETAIL_HISTORY"
    // keys of "detail" preference
    val prefKeys = arrayOf("1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th")

    // save preference with new value inserted
    fun savePreference(input: String, ctx: Context) {
        // loaded preferences
        val prefs = ctx.getSharedPreferences(DETAIL_HISTORY, Context.MODE_PRIVATE)
        // length of preferences
        val pLength = prefKeys.size
        // values to be stored (with new value inserted)
        val newPrefValues = insertPreference(prefs, Array(pLength, { "" }), pLength - 1, prefKeys, pLength - 1, input)
        // store new values to preference
        val editor = prefs.edit()
        for ((i, key) in prefKeys.withIndex()){
            editor.putString(key, newPrefValues[i])
        }
        editor.commit()
    }
    // insert new value to destination array
    tailrec fun insertPreference(src: SharedPreferences, dst: Array<String>, dstIndex: Int, keys: Array<String>, keyIndex: Int, input: String): Array<String> {
        when (dstIndex) {
            0 -> {
                // the head is replaced by input value
                dst[dstIndex] = input
                return dst
            }
            else -> {
                val s = src.getString(keys[keyIndex], "")
                if (s == input) {
                    // src is not copied if the list has the input value in the middle of it
                    return insertPreference(src, dst, dstIndex, keys, keyIndex - 1, input)
                }
                // copy src to dst
                dst[dstIndex] = s
                return insertPreference(src, dst, dstIndex - 1, keys, keyIndex - 1, input)
            }
        }
    }
    // get array of values stored in preference
    fun getPreference(ctx: Context):Array<String>{
        // loaded preferences
        val prefs = ctx.getSharedPreferences(DETAIL_HISTORY, Context.MODE_PRIVATE)
        // array to return
        val pStrings = Array(prefKeys.size, {""})
        for ((i, key) in prefKeys.withIndex()){
            pStrings[i] = prefs.getString(key, "")
        }
        return pStrings
    }
}