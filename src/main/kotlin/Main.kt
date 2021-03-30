import io.ktor.util.*
import org.ranbi.HttpProxy

@InternalAPI
fun main() {
    val proxy = HttpProxy()

    // Start a proxy with no parameters means the server won't support https request.
    // proxy.startProxy()

    proxy.startProxy(proxy.loadKeyStore(), "mykey", "changeit")
}