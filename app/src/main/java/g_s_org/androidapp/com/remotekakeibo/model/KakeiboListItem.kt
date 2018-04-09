package g_s_org.androidapp.com.remotekakeibo.model

import g_s_org.androidapp.com.remotekakeibo.common.Constants

data class KakeiboListItem(var id:Int = -1,
                           var isSummary:Boolean = false,
                           var date:KakeiboDate = KakeiboDate(),
                           var category: String = "",
                           var type:Int = Constants.EXPENSE,
                           var price:Int = 0,
                           var detail:String = "",
                           var termsOfPayment:Int = Constants.CASH,
                           var subtotalIncome:Int = 0,
                           var subtotalExpense:Int = 0)