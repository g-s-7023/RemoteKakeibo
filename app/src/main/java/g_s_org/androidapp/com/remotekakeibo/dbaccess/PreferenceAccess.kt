package g_s_org.androidapp.com.remotekakeibo.dbaccess

import android.content.Context
import android.content.SharedPreferences
import g_s_org.androidapp.com.remotekakeibo.common.Constants
import g_s_org.androidapp.com.remotekakeibo.common.Constants.Companion.DETAIL_HISTORY

/**
 * Created by C170044 on 2018/04/03.
 */
class DetailHistoryAccess{

    //ここから


    fun savePreference(input: String, ctx: Context) {
        // プリファレンスオブジェクト
        val preference = ctx.getSharedPreferences(DETAIL_HISTORY, Context.MODE_PRIVATE)
        // プリファレンスの編集用オブジェクト
        val editor = preference.edit()
        // 既に登録されていた場合の履歴の順位
        var orderOfInputDetail = Constants.PREF_DETAIL.length - 2
        // プリファレンスを一時的に格納する配列
        val tempPrefArray = arrayOfNulls<String>(Constants.PREF_DETAIL.length)

        if (!input.isEmpty()) {
            // 詳細が空の場合、登録はしない
            // 入力された値がプリファレンスの中に存在するかチェック
            for (i in 0 until Constants.PREF_DETAIL.length - 1) {
                // 次の処理のため、プリファレンスの値を一時的に保存
                tempPrefArray[i] = preference.getString(Constants.PREF_DETAIL[i], "")
                if (tempPrefArray[i] == input) {
                    // 既に登録されている場合、その履歴の順位を記録
                    orderOfInputDetail = i
                    break
                }
            }
            // orderOfInputDetailの位置まで、プリファレンスの値を詰める
            for (i in orderOfInputDetail downTo 1) {
                editor.putString(Constants.PREF_DETAIL[i], tempPrefArray[i - 1])
            }
            // preferenceの先頭に今回入力された詳細を挿入
            editor.putString(Constants.PREF_DETAIL[0], input)
            // 変更をコミット
            editor.commit()
        }
    }
}