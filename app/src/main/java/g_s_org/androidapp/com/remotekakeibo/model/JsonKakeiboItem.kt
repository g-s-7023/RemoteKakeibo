package g_s_org.androidapp.com.remotekakeibo.model

import g_s_org.androidapp.com.remotekakeibo.common.Constants
import g_s_org.androidapp.com.remotekakeibo.common.KakeiboDate
import org.json.JSONObject
import java.time.DayOfWeek


class JsonKakeiboItem(val id: Int = -1,
                      val year: Int = Constants.DEFAULT_YEAR,
                      val month: Int = 1,
                      val day: Int = 1,
                      val dayOfWeek: Int = 1,
                      val category: String = "",
                      val type: Int = Constants.EXPENSE,
                      val price: Int = 0,
                      val detail: String = "",
                      val termsOfPayment: Int = Constants.CASH,
                      val isDeleted: Int = Constants.FALSE) {

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("year", year)
        json.put("month", month)
        json.put("day", day)
        json.put("dayOfWeek", dayOfWeek)
        json.put("category", category)
        json.put("type", type)
        json.put("price", price)
        json.put("detail", detail)
        json.put("termsOfPayment", termsOfPayment)
        json.put("isDeleted", isDeleted)
        return json
    }
}