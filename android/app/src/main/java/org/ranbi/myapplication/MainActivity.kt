package org.ranbi.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.ktor.util.*
import org.ranbi.HttpProxy
import java.security.KeyStore
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@InternalAPI
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        executorService.execute { runServer() }
    }

    private fun runServer() {
        val proxy = HttpProxy()

        val keyStore = loadKeyStore()
        // Start a proxy with no parameters means the server won't support https request.
        // proxy.startProxy()

        proxy.startProxy(keyStore, "mykey", "changeit")
    }

    private fun loadKeyStore(): KeyStore {
        // this file is converted from a jks keystore
        val openRawResource = resources.openRawResource(R.raw.temporary);

        // Android doesn't support "JKS"
        val keyStore = KeyStore.getInstance("BKS")
        keyStore!!.load(openRawResource, "changeit".toCharArray());
        return keyStore
    }
}