import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TinyLogger(private val logPrefix: String, private val allowDebug: Boolean) {

    private val timeOnlyFormatter: DateTimeFormatter = DateTimeFormatter.ISO_TIME

    fun info(str: String) {
        println("${tnow()} INFO $logPrefix: $str")
    }

    fun debug(str: String) {
        if (allowDebug) {
            println("${tnow()} DEBUG $logPrefix: $str")
        }

    }

    fun error(str: String) {
        println("${tnow()} ERROR $logPrefix: $str")
    }

    private fun tnow() = LocalDateTime.now().format(timeOnlyFormatter)
}
