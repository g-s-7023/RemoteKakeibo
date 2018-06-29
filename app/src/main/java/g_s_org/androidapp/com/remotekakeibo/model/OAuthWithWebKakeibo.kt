package g_s_org.androidapp.com.remotekakeibo.model

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import g_s_org.androidapp.com.remotekakeibo.R
import g_s_org.androidapp.com.remotekakeibo.common.Constants.Companion.RC_GET_TOKEN


// todo
// cloud console上でclientIDの取得
// クライアント(このプログラム)からtokenの要求を実装
// tokenをゲットしたら、サーバにPOSTする処理の実装
// サーバ側でtokenを検証し、返信する処理の実装

fun Activity.getIdToken() {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            //.requestIdToken(getString(R.string.server_client_id))
            .requestEmail()
            .build()
    val signInIntent = GoogleSignIn.getClient(this, gso).signInIntent
    startActivityForResult(signInIntent, RC_GET_TOKEN)
}

fun hundleSignInResult(data: Intent?) {
    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
    try {
        val account = task.getResult(ApiException::class.java)
        val token = account.idToken
        // tokenをサーバへ送信
        // 描画処理(画面更新)
    } catch (e: ApiException) {
        Log.e("ERROR", e.toString())
    }
}

