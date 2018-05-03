package g_s_org.androidapp.com.remotekakeibo.model

import android.os.AsyncTask
import android.util.Log
import com.google.android.gms.security.ProviderInstaller
import g_s_org.androidapp.com.remotekakeibo.common.Constants
import org.json.JSONArray
import org.json.JSONException
import java.io.*
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import java.util.Collections.singletonList




class HttpPostKakeibo(val mUrlString: String, val mBody: JSONArray, val mIds: MutableList<Int>, val mCallback: WeakReference<KakeiboSyncCallback>)
    : AsyncTask<Unit, Unit, JSONArray>() {

    constructor(url: String, body: JSONArray, ids: MutableList<Int>, callback: KakeiboSyncCallback) : this(url, body, ids, WeakReference(callback))

    fun setListener(callback: KakeiboSyncCallback) {
        mCallback.apply { callback }
    }

    override fun doInBackground(vararg params: Unit?): JSONArray? {
        var con: HttpURLConnection? = null
        try {
            // make URL
            val url = URL(mUrlString)



            // 実機だとSSLProtocolExceptionが発生する。GAEはTLSしか受け付けないが、クライアントはSSL2.3でアクセスしようとしているっぽい
            // エミュレータからだとpingは通る



            // restrict SSL and enable TLS for successful https access(doesn't work)
            /*
            val sslContext = SSLContext.getInstance("TLSv1.2")
            sslContext.init(null, null, null)
            val engine = sslContext.createSSLEngine()
            engine.enabledProtocols = arrayOf("TLSv1.2")
            */

            // make HttpURLConnection Object
            con = url.openConnection() as HttpsURLConnection
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
            val responseString = readInputStream(StringBuilder(), BufferedReader(InputStreamReader(stream, "UTF-8")))
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
    tailrec fun readInputStream(sb: StringBuilder, br: BufferedReader): String {
        val next = br.readLine()
        return when (next) {
            null -> sb.toString()
            else -> readInputStream(sb.append(next), br)
        }
    }

    interface KakeiboSyncCallback {
        fun callback(result: JSONArray, ids: MutableList<Int>)
    }
}