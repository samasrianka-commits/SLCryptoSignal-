package com.slcrypto.signal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    var tab by remember { mutableStateOf(Tab.Signals) }
    var openSignal by remember { mutableStateOf<Signal?>(null) }
    var showPremium by remember { mutableStateOf(false) }
    var subscribed by remember { mutableStateOf(false) }
    val prices = remember { mutableStateMapOf<String, Double>() }
    val userName = "samantha"

    // Refresh live Binance prices every 10 seconds
    LaunchedEffect(Unit) {
        while (true) {
            val p = fetchPrices(sampleSignals.map { it.symbol })
            prices.putAll(p)
            delay(10_000)
        }
    }

    BackHandler(enabled = openSignal != null || showPremium) {
        if (showPremium) showPremium = false else openSignal = null
    }

    val title = when {
        showPremium -> "Premium"
        openSignal != null -> "Signal Details"
        tab == Tab.Signals -> "Home"
        else -> tab.label
    }
    val onBack: (() -> Unit)? = when {
        showPremium -> ({ showPremium = false })
        openSignal != null -> ({ openSignal = null })
        else -> null
    }

    Box(Modifier.fillMaxSize().background(Bg)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            TopBar(title, userName, subscribed, onBack)
            Box(Modifier.weight(1f)) {
                when {
                    showPremium -> PremiumScreen { subscribed = true; showPremium = false }
                    openSignal != null -> {
                        val s = openSignal!!
                        DetailScreen(s, prices[s.symbol] ?: s.demoCurrent)
                    }
                    tab == Tab.Signals -> SignalsScreen(
                        subscribed, prices,
                        onOpen = { openSignal = it },
                        onLocked = { showPremium = true }
                    )
                    tab == Tab.Performance -> PerformanceScreen()
                    else -> ProfileScreen(userName, subscribed) { showPremium = true }
                }
            }
            BottomBar(tab) { tab = it; openSignal = null; showPremium = false }
        }
    }
}
