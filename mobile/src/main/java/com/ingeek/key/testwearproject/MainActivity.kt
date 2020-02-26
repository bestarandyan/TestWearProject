package com.ingeek.key.testwearproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.contracts.contract

class MainActivity : AppCompatActivity(), DataClient.OnDataChangedListener, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {
    var googleApiClient: GoogleApiClient? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initServer()
        getScheme()
    }

    private fun initServer() {
        googleApiClient = GoogleApiClient.Builder(this)
            .addOnConnectionFailedListener(this)
            .addConnectionCallbacks(this)
            .addApi(Wearable.API).build()
        googleApiClient!!.connect()

        Wearable.getDataClient(this).addListener(this)
    }


    var handler = Handler{
        recvText.text = it.obj.toString()
        false
    }


    /**
     * 当被手表app唤醒时，获取scheme参数
     */
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


    override fun onConnected(p0: Bundle?) {
        Log.e("AppLog", "连接手表成功")
        Wearable.DataApi.addListener(googleApiClient, this)
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.e("AppLog", "连接手表中断")
    }

    override fun onDataChanged(p0: DataEventBuffer) {
        Log.d("AppLog", "收到手表发过来的数据")
        for (data in p0!!) {
            val uri = data.dataItem.uri
            val path = uri.path
            if (!TextUtils.isEmpty(path) && "/cmdData" == path) {
                val dataMap = DataMapItem.fromDataItem(data.dataItem).dataMap
                val cmdValue = dataMap.getString("cmdValue")
                Log.e("AppLog", "收到数据的时间是：" + dataMap.getString("dataTime"))
                if (!TextUtils.isEmpty(cmdValue)) {
                    Log.e("AppLog", "收到数据是：$cmdValue")
                    val message = Message()
                    message.obj = cmdValue
                    handler.sendMessage(message)
                }
                break
            }
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.e("AppLog", "连接手表失败")    }
}
