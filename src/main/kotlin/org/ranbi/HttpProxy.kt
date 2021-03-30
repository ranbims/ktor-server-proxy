package org.ranbi

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import io.ktor.utils.io.*
import java.io.File
import java.security.KeyStore

@InternalAPI
class HttpProxy {

    fun loadKeyStore(): KeyStore {
        val file: File = File("temporary.jks")
        val fis = file.inputStream()
        val keyStore = KeyStore.getInstance("JKS")
        keyStore.load(fis, "changeit".toCharArray())
        return keyStore
    }

    fun startProxy(keyStore: KeyStore? = null, alias: String = "", passward: String = "") {
        embeddedServer(Netty, environment = applicationEngineEnvironment {

            connector {
                port = 8080
                host = "127.0.0.1"

                module {
                    val client = HttpClient(CIO) {
                        followRedirects = true
                    }
                    val wikipediaLang = "en"
                    intercept(ApplicationCallPipeline.Call) {
                        val channel: ByteReadChannel = call.request.receiveChannel()
                        val size = channel.availableForRead
                        val byteArray: ByteArray = ByteArray(size)
                        channel.readFully(byteArray)

                        try {
                            val response: HttpResponse =
                                client.request("https://$wikipediaLang.wikipedia.org${call.request.uri}") {
                                    method = call.request.httpMethod
                                    headers {
                                        appendAll(call.request.headers.filter { key, _ ->
                                            !key.equals(
                                                HttpHeaders.ContentType,
                                                ignoreCase = true
                                            ) && !key.equals(HttpHeaders.ContentLength, ignoreCase = true
                                            ) && !key.equals(HttpHeaders.Host, ignoreCase = true)
                                        })
                                    }
                                    if (call.request.httpMethod.equals(HttpMethod.Post)) {
                                        body = ByteArrayContent(byteArray, call.request.contentType())
                                    }
                                }
                            val proxiedHeaders = response.headers
                            val location = proxiedHeaders[HttpHeaders.Location]
                            val contentType = proxiedHeaders[HttpHeaders.ContentType]
                            val contentLength = proxiedHeaders[HttpHeaders.ContentLength]
                            call.respond(object : OutgoingContent.WriteChannelContent() {
                                override val contentLength: Long? = contentLength?.toLong()
                                override val contentType: ContentType? =
                                    contentType?.let { ContentType.parse(it) }
                                override val headers: Headers = Headers.build {
                                    appendAll(proxiedHeaders.filter { key, _ ->
                                        !key.equals(
                                            HttpHeaders.ContentType,
                                            ignoreCase = true
                                        ) && !key.equals(HttpHeaders.ContentLength, ignoreCase = true)
                                    })
                                }
                                override val status: HttpStatusCode? = response.status
                                override suspend fun writeTo(channel: ByteWriteChannel) {
                                    response.content.copyAndClose(channel)
                                }
                            })
                        } catch (e: Exception) {
                            System.err.println(e);
                        }

                    }
                }
            }

            if (keyStore != null) {
                sslConnector(keyStore!!, alias!!, { passward.toCharArray() }, { passward.toCharArray() }) {
                    port = 8181
                }
            }
        }).start(wait = true)
    }
}