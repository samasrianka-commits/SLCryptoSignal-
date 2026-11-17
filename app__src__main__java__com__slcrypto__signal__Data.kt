package com.slcrypto.signal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class Signal(
    val symbol: String,
    val side: String,
    val time: String,
    val entry: Double,
    val stop: Double,
    val demoCurrent: Double,
    val vip: Boolean,
    val leverage: Int = 28,
    val maxRisk: Int = 3,
    val stepPct: Double = 0.9
) {
    /** TP1..TP5, each stepPct% above the previous (from entry). */
    fun targets(): List<Double> = (1..5).map { entry * (1 + stepPct / 100.0 * it) }
}

/** Sample signals. Replace with your own data / backend later. */
val sampleSignals = listOf(
    Signal("ETHUSDT", "Buy", "17-11-2026 03:27 AM", 1826.65, 1810.73, 1828.27, false),
    Signal("UNIUSDT", "Buy", "17-11-2026 03:20 AM", 5.2089, 5.115, 5.324, false),
    Signal("AVAXUSDT", "Buy", "15-11-2026 04:00 AM", 7.155, 6.743, 7.212, false),
    Signal("ADAUSDT", "Buy", "15-11-2026 02:32 AM", 0.1492, 0.1426, 0.1535, false),
    Signal("VETUSDT", "Buy", "17-11-2026 06:46 AM", 0.0212, 0.0203, 0.0213, true),
    Signal("NEARUSDT", "Buy", "17-11-2026 06:42 AM", 2.41, 2.31, 2.42, true),
    Signal("SUIUSDT", "Buy", "17-11-2026 06:18 AM", 1.92, 1.84, 1.93, true),
    Signal("BCHUSDT", "Buy", "17-11-2026 04:12 AM", 455.0, 436.0, 457.0, true)
)

fun fmtPrice(v: Double): String =
    if (v >= 100) String.format(Locale.US, "%.2f", v)
    else String.format(Locale.US, "%.4f", v)

fun fmtPct(v: Double): String =
    (if (v >= 0) "+" else "") + String.format(Locale.US, "%.2f%%", v)

fun pctMove(entry: Double, current: Double): Double = (current - entry) / entry * 100.0

/** Live prices from Binance public API (no key needed). Returns empty map on failure. */
suspend fun fetchPrices(symbols: List<String>): Map<String, Double> = withContext(Dispatchers.IO) {
    try {
        val arr = symbols.joinToString(",") { "\"$it\"" }
        val encoded = java.net.URLEncoder.encode("[$arr]", "UTF-8")
        val conn = URL("https://api.binance.com/api/v3/ticker/price?symbols=$encoded")
            .openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        val body = conn.inputStream.bufferedReader().use { it.readText() }
        val json = JSONArray(body)
        val out = mutableMapOf<String, Double>()
        for (i in 0 until json.length()) {
            val o = json.getJSONObject(i)
            out[o.getString("symbol")] = o.getString("price").toDouble()
        }
        out
    } catch (e: Exception) {
        emptyMap()
    }
}
