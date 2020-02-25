package com.ingeek.key.testwearproject

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.wearable.activity.WearableActivity
import android.support.wearable.view.ConfirmationOverlay
import android.util.Log
import android.view.View
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.CapabilityApi
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.wearable.intent.RemoteIntent
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : WearableActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, CapabilityApi.CapabilityListener {
    var googleApiClient: GoogleApiClient? = null
    private val CAPABILITY_PHONE_APP = "verify_remote_example_phone_app"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setAmbientEnabled()
        connectServer()
        initView()

    }

    private fun initView() {
        reconnectBtn.setOnClickListener { connectServer() }
        sendDataToAppBtn.setOnClickListener { sendData("这是从手表发送过来的数据") }
        startAppBtn.setOnClickListener { startRemoteIntent() }
        Wearable.CapabilityApi.addCapabilityListener(googleApiClient, this, CAPABILITY_PHONE_APP)

    }

    private fun sendData(cmd: String) {
        val request = PutDataMapRequest.create("/cmdData")
        val dataMap = request.dataMap
        dataMap.putString("cmdValue", cmd)
        dataMap.putString("dataTime", System.currentTimeMillis().toString())
        Wearable.DataApi.putDataItem(googleApiClient, request.asPutDataRequest())
            .setResultCallback { result ->
                if (result.status.isSuccess) {
                    Log.e("WearApp", "数据发送成功")
                } else {
                    Log.e("WearApp", "数据发送失败")
                }
            }
    }

    override fun onPause() {
        super.onPause()
        Wearable.CapabilityApi.removeCapabilityListener(googleApiClient, this, CAPABILITY_PHONE_APP)
    }

    private fun connectServer() {
        googleApiClient = GoogleApiClient.Builder(this)
            .addOnConnectionFailedListener(this)
            .addConnectionCallbacks(this)
            .addApi(Wearable.API).build()
        googleApiClient!!.registerConnectionCallbacks(this)
        googleApiClient!!.registerConnectionFailedListener(this)
        googleApiClient!!.connect()
    }

    private fun checkIfPhoneHasApp() {
        Log.d("WearApp", "checkIfPhoneHasApp()")
        val capabilityInfoTask = Wearable.CapabilityApi.getCapability(
            googleApiClient,
            CAPABILITY_PHONE_APP,
            CapabilityApi.FILTER_ALL
        )
        capabilityInfoTask.addBatchCallback { status ->
            if (status!!.isSuccess) {
                Log.e("WearApp", "手机上已安装该应用")
            }
        }
    }

    private val ANDROID_MARKET_APP_URI = "ingeek://ingeek:8080/homeActivity?tool_id=100"

    private fun startRemoteIntent() {
        val intentAndroid = Intent(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(Uri.parse(ANDROID_MARKET_APP_URI))

        RemoteIntent.startRemoteActivity(
            applicationContext,
            intentAndroid,
            mResultReceiver
        )

        checkIfPhoneHasApp()
    }

    // Result from sending RemoteIntent to phone to open app in play/app store.
    private val mResultReceiver: ResultReceiver = object : ResultReceiver(Handler()) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            if (resultCode == RemoteIntent.RESULT_OK) {
                ConfirmationOverlay().showOn(this@MainActivity)
                Log.e("wearmain", "启动app成功")
            } else if (resultCode == RemoteIntent.RESULT_FAILED) {
                ConfirmationOverlay()
                    .setType(ConfirmationOverlay.FAILURE_ANIMATION)
                    .showOn(this@MainActivity)
                Log.e("wearmain", "启动app失败")
            } else {
                throw IllegalStateException("Unexpected result $resultCode")
            }
        }
    }

    override fun onConnected(p0: Bundle?) {
        Log.e("wearmain", "连接成功")
        connectPhoneStatus.text = "与手机连接状态：已连接"
        reconnectBtn.visibility = View.GONE
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.e("wearmain", "连接中断")
        connectPhoneStatus.text = "与手机连接状态：已中断"
        reconnectBtn.visibility = View.VISIBLE
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.e("wearmain", "连接失败")
        connectPhoneStatus.text = "与手机连接状态：已断开"
        reconnectBtn.visibility = View.VISIBLE
    }


    private fun getScheme() {
        val intent = getIntent()
        val scheme = intent.getScheme()
        val dataString = intent!!.getDataString()
        val uri = intent.getData()
        Log.e("AppLog", "scheme:" + scheme)
        if (uri != null) {
            //完整的url信息
            val url = uri.toString()
            //scheme部分
            val schemes = uri.getScheme()
            //host部分
            val host = uri.getHost()
            //port部分
            val port = uri.getPort()
            //访问路径
            val path = uri.getPath()
            //编码路径
            val path1 = uri.getEncodedPath()
            //query部分
            val queryString = uri.getQuery()
            //获取参数值
            val systemInfo = uri.getQueryParameter("system")
            val id = uri.getQueryParameter("id")
            Log.e("AppLog", "host:" + host)
            Log.e("AppLog", "dataString:" + dataString)
            Log.e("AppLog", "id:" + id)
            Log.e("AppLog", "path:" + path)
            Log.e("AppLog", "path1:" + path1)
            Log.e("AppLog", "queryString:" + queryString)
        }
    }


    override fun onCapabilityChanged(p0: CapabilityInfo?) {

    }
}
