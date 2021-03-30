package org.ranbi.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.ktor.util.*
import org.ranbi.HttpProxy
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

        // not support https for now
        proxy.startProxy()
    }
}