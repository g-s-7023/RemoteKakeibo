package g_s_org.androidapp.com.remotekakeibo.common

import g_s_org.androidapp.com.remotekakeibo.common.Constants
import g_s_org.androidapp.com.remotekakeibo.common.KakeiboDate

data class KakeiboListItem(var id:Int = -1,
                           var isSummary:Boolean = false,
                           var date: KakeiboDate = KakeiboDate(),
                           var category: String = "",
                           var type:Int = Constants.EXPENSE,
                           var price:Int = 0,
                           var detail:String = "",
                           var termsOfPayment:Int = Constants.CASH,
                           var subtotalIncome:Int = 0,
                           var subtotalExpense:Int = 0){

    constructor(iSum: Boolean, d: KakeiboDate, subI: Int, subE: Int) :this(){
        isSummary = iSum
        date = d
        subtotalIncome = subI
        subtotalExpense = subE
    }

    constructor(i:Int, dat: KakeiboDate, c:String, t:Int, p:Int, det:String, tOfP:Int): this(){
        id = i
        date = dat
        category = c
        type = t
        price = p
        detail = det
        termsOfPayment = tOfP
    }
}