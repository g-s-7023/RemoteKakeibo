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


/**
 * Created by nao on 2018/04/21.
 */
class HttpPost(val mUrlString: String = "", val mBody: JSONArray, val mCallback:WeakReference<KakeiboCallback>)
    : AsyncTask<Unit, Unit, JSONArray>() {

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
            responseString = readInputStream(stream)
            stream.close()
            // convert to json object and return it
            return JSONArray(responseString)
        } catch (e: MalformedURLException) {
            Log.e("HttpPost", "", e)
        } catch (e: IOException) {
            Log.e("HttpPost", "", e)
        } catch (e: JSONException) {
            Log.e("HttpPost", "", e)
        } finally {
            con?.disconnect()
        }
        return null
    }

    public override fun onPostExecute(result: JSONArray) {
        // callback must be implemented by the caller
        val c = this.mCallback.get()
        if (c != null) {
            c.callback(result)
        } else {
            Log.e("HttpPost", "mCallback is not set")
        }
    }

    @Throws(IOException::class, UnsupportedEncodingException::class)
    private fun readInputStream(buf: InputStream): String {
        val sb = StringBuilder()
        val br = BufferedReader(InputStreamReader(buf, "UTF-8"))
        var line = br.readLine()
        while (line != null) {
            sb.append(line)
            br.readLine()
        }
        return sb.toString()
    }

    interface KakeiboCallback {
        fun callback(result: JSONArray)
    }
}