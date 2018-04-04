package g_s_org.androidapp.com.remotekakeibo.model

import java.util.*

/**
 * Created by C170044 on 2018/04/04.
 */

//===
//=== get priceString of priceStack
//===

// 4/3作り直し
// 再帰を使って書き直したい

fun Deque<Char>.getPrice(): String {
    // string builder
    val priceString = StringBuilder()
    // temporary deque
    val tempStack = ArrayDeque(this)

    for (i in this.indices) {
        if (i != 0 && i % 3 == 0) {
            // insert ',' every three digits
            priceString.append(',')
        }
        priceString.append(tempStack.removeLast())
    }
    // add '￥' at the end
    priceString.append('￥')
    // reverse order
    priceString.reverse()
    return priceString.toString()
}

//===
//=== store price to stack
//===

// 4/3作り直し
// 再帰を使って書き直したい

fun Deque<Char>.setPrice(price: Int) {
    // change price to type array of char
    val priceArray = price.toString().toCharArray()
    // initialize deque
    this.clear()
    // push array to deque
    for (ch in priceArray) {
        this.addLast(ch)
    }
}