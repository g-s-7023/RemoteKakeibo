package g_s_org.androidapp.com.remotekakeibo.model

import android.os.AsyncTask
import android.util.Log
import g_s_org.androidapp.com.remotekakeibo.common.Constants
import org.json.JSONArray
import org.json.JSONException
import java.io.*
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*


class HttpPostKakeibo(val mUrlString: String, val mBody: JSONArray, val mIds:MutableList<Int>, val mCallback:WeakReference<KakeiboSyncCallback>)
    : AsyncTask<Unit, Unit, JSONArray>() {

    constructor(url:String, body:JSONArray, ids:MutableList<Int>,callback:KakeiboSyncCallback):this(url, body, ids, WeakReference(callback))

    fun setListener(callback:KakeiboSyncCallback){
        mCallback.apply { callback }
    }

    override fun doInBackground(vararg params: Unit?): JSONArray? {
        var con: HttpURLConnection? = null
        var url: URL
        var responseString:String
        try {
            // make URL
            url = URL(mUrlString)
            // make HttpURLConnection Object
            con = url.openConnection() as HttpURLConnection
            con.requestMethod = "POST"
            con.instanceFollowRedirects = false
            con.connectTimeout = Constants.HTTP_TIMEOUT
            con.readTimeout = Constants.HTTP_TIMEOUT
            con.doOutput = true
            con.doInput = true
            con.addRequestProperty("Content-Type", "application/json; charset=UTF-8")
            con.setRequestProperty("User-Agent", "Android")
            con.setRequestProperty("Accept-Language", Locale.getDefault().toString())
            // connect
            con.connect()
            // output
            val ps = PrintStream(con.outputStream)
            ps.print(mBody.toString())
            ps.close()
            // get response
            val statusCode = con.responseCode
            // get response body
            val stream = con.inputStream
            responseString = readInputStream(StringBuilder(), BufferedReader(InputStreamReader(stream, "UTF-8")))
            stream.close()
            // convert to json object and return it
            return JSONArray(responseString)
        } catch (e: MalformedURLException) {
            Log.e("HttpPostKakeibo", "", e)
        } catch (e: IOException) {
            Log.e("HttpPostKakeibo", "", e)
        } catch (e: JSONException) {
            Log.e("HttpPostKakeibo", "", e)
        } finally {
            con?.disconnect()
        }
        return null
    }

    public override fun onPostExecute(result: JSONArray) {
        // callback must be implemented by the caller
        val c = this.mCallback.get()
        if (c != null) {
            c.callback(result, mIds)
        } else {
            Log.e("HttpPostKakeibo", "mCallback is not set")
        }
    }

    @Throws(IOException::class, UnsupportedEncodingException::class)
    tailrec fun readInputStream(sb:StringBuilder, br:BufferedReader):String{
        val next = br.readLine()
        return when(next){
            null -> sb.toString()
            else -> readInputStream(sb.append(next), br)
        }
    }

    interface KakeiboSyncCallback{
        fun callback(result: JSONArray, ids:MutableList<Int>)
    }
}