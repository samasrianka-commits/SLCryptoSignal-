package com.slcrypto.signal

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Bg = Color(0xFF070B1A)
val Card = Color(0xFF0B1630)
val CardBorder = Color(0xFF2A3A6B)
val Green = Color(0xFF1DD1A1)
val Red = Color(0xFFFF5252)
val Gold = Color(0xFFF0B90B)
val Muted = Color(0xFFAAB4D0)
val Gray = Color(0xFF3B4566)

enum class Tab(val label: String, val icon: ImageVector) {
    Signals("Signals", Icons.Default.Sensors),
    Performance("Performance", Icons.Default.BarChart),
    Profile("Profile", Icons.Default.Person)
}

@Composable
fun TopBar(title: String, userName: String, subscribed: Boolean, onBack: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF0E1530))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
        }
        Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(userName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(if (subscribed) "VIP" else "Not Subscribed",
                color = if (subscribed) Gold else Muted, fontSize = 13.sp)
        }
    }
}

@Composable
fun BottomBar(selected: Tab, onSelect: (Tab) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(Color(0xFF0E1530))
            .border(1.dp, CardBorder, RoundedCornerShape(40.dp))
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Tab.values().forEach { t ->
            val c = if (t == selected) Green else Muted
            Column(
                Modifier.clickable { onSelect(t) }.padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(t.icon, null, tint = c)
                Text(t.label, color = c, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SignalsScreen(
    subscribed: Boolean,
    prices: Map<String, Double>,
    onOpen: (Signal) -> Unit,
    onLocked: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Text("All Signals  ${sampleSignals.size}", color = Color.White, fontSize = 26.sp,
            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp))
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(sampleSignals) { s ->
                val locked = s.vip && !subscribed
                if (locked) LockedCard(s, onLocked)
                else SignalCard(s, prices[s.symbol] ?: s.demoCurrent) { onOpen(s) }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun SignalCard(s: Signal, current: Double, onClick: () -> Unit) {
    val move = pctMove(s.entry, current)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Card)
            .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(Color(0xFF3B4A9A)),
                contentAlignment = Alignment.Center) {
                Text(s.symbol.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(s.symbol, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(10.dp))
                    Text(s.side, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF14A58B)).padding(horizontal = 12.dp, vertical = 4.dp))
                }
                Text(s.time, color = Muted, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Gray))
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            LabeledValue("Entry", fmtPrice(s.entry), Color.White, Alignment.Start)
            LabeledValue("Current", fmtPrice(current), Color.White, Alignment.CenterHorizontally)
            LabeledValue("Stop Loss", fmtPrice(s.stop), Red, Alignment.End)
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Gray))
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Price move", color = Color.White, fontSize = 15.sp)
            Text(fmtPct(move), color = if (move >= 0) Green else Red,
                fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LabeledValue(label: String, value: String, color: Color, align: Alignment.Horizontal) {
    Column(horizontalAlignment = align) {
        Text(label, color = Muted, fontSize = 14.sp)
        Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LockedCard(s: Signal, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Card)
            .border(1.5.dp, Color(0xFF3558D6), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(Color(0xFF2E7BD6)),
                contentAlignment = Alignment.Center) {
                Text(s.symbol.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(s.symbol, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(s.time, color = Muted, fontSize = 12.sp)
            }
            Icon(Icons.Default.Lock, null, tint = Gold)
            Spacer(Modifier.width(8.dp))
            Text("VIP", color = Gold, fontWeight = FontWeight.Bold,
                modifier = Modifier.border(1.dp, Gold, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp))
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Gray))
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Entry", color = Muted); Spacer(Modifier.width(8.dp)); Mask(Modifier.width(70.dp))
            Spacer(Modifier.width(14.dp))
            Text("TP", color = Muted); Spacer(Modifier.width(8.dp)); Mask(Modifier.width(100.dp))
            Spacer(Modifier.width(14.dp))
            Text("SL", color = Muted); Spacer(Modifier.width(8.dp)); Mask(Modifier.width(70.dp))
        }
    }
}

@Composable
fun Mask(modifier: Modifier) {
    Text("••••", color = Muted, textAlign = TextAlign.Center,
        modifier = modifier.clip(RoundedCornerShape(10.dp)).background(Color(0xFF16254D)).padding(vertical = 6.dp))
}

@Composable
fun DetailScreen(s: Signal, current: Double) {
    val clipboard = LocalClipboardManager.current
    val ctx = LocalContext.current
    var showCopy by remember { mutableStateOf(false) }
    val tps = s.targets()
    val move = pctMove(s.entry, current)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("${s.symbol}  ${s.side}", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(s.time, color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoBox("Suggested Leverage", "X${s.leverage}", Color(0xFF6C8CFF), Modifier.weight(1f))
            InfoBox("Maximum suggested risk", "${s.maxRisk}%", Green, Modifier.weight(1f))
        }

        Spacer(Modifier.height(22.dp))
        Text("Price position", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        val points = buildList {
            add(Triple("Stop", s.stop, Red))
            add(Triple("Entry", s.entry, Gold))
            tps.forEachIndexed { i, v -> add(Triple("TP${i + 1}", v, if (current >= v) Green else Gray)) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            points.forEach { (label, v, c) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(16.dp).clip(CircleShape).background(c))
                    Spacer(Modifier.height(6.dp))
                    Text(label, color = if (label == "Stop") Red else if (label == "Entry") Gold else Green,
                        fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(fmtPrice(v), color = Muted, fontSize = 9.sp)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("Current  ${fmtPrice(current)}", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Card)
                .border(1.dp, CardBorder, RoundedCornerShape(24.dp)).padding(18.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Market move", color = Muted)
                Text(fmtPct(move), color = if (move >= 0) Green else Red, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("From entry", color = Muted, fontSize = 13.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Estimated at X${s.leverage}", color = Muted)
                val est = move * s.leverage
                Text(fmtPct(est), color = if (est >= 0) Green else Red, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Before fees", color = Muted, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(22.dp))
        Text("Targets", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Card)
                .border(1.dp, CardBorder, RoundedCornerShape(24.dp)).padding(14.dp)
        ) {
            Row(Modifier.fillMaxWidth()) {
                listOf("Target", "Price", "Profit %", "At X${s.leverage}").forEach {
                    Text(it, color = Muted, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                }
            }
            tps.forEachIndexed { i, v ->
                val p = pctMove(s.entry, v)
                Box(Modifier.fillMaxWidth().padding(vertical = 8.dp).height(1.dp).background(Gray))
                Row(Modifier.fillMaxWidth()) {
                    Text("TP${i + 1}", color = Green, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    Text(fmtPrice(v), color = if (current >= v) Green else Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    Text(fmtPct(p), color = Green, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    Text(fmtPct(p * s.leverage), color = Green, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("Copy levels", color = Color.White, modifier = Modifier
            .fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFF1E4BD8))
            .clickable { showCopy = !showCopy }.padding(14.dp),
            textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        if (showCopy) {
            Spacer(Modifier.height(12.dp))
            val items = buildList {
                add("ENTRY" to s.entry); add("STOP" to s.stop)
                tps.forEachIndexed { i, v -> add("Target ${i + 1}" to v) }
            }
            items.chunked(2).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                    rowItems.forEach { (label, v) ->
                        Column(
                            Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(Color(0xFF1B1F2E)).padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(label, color = if (label == "STOP") Red else if (label == "ENTRY") Green else Color.White, fontWeight = FontWeight.Bold)
                            Text(fmtPrice(v), color = Color.White, fontSize = 18.sp)
                            Spacer(Modifier.height(8.dp))
                            Box(
                                Modifier.clip(RoundedCornerShape(20.dp)).background(Color(0xFF1EA0FF))
                                    .clickable {
                                        clipboard.setText(AnnotatedString(fmtPrice(v)))
                                        Toast.makeText(ctx, "Copied ${fmtPrice(v)}", Toast.LENGTH_SHORT).show()
                                    }.padding(horizontal = 28.dp, vertical = 8.dp)
                            ) { Icon(Icons.Default.ContentCopy, null, tint = Color.White) }
                        }
                    }
                    if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("Leveraged trading is risky. Signals are not guaranteed profits.", color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
fun InfoBox(label: String, value: String, valueColor: Color, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(20.dp)).background(Card)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = Muted, fontSize = 14.sp, textAlign = TextAlign.Center)
        Text(value, color = valueColor, fontSize = 30.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PremiumScreen(onSubscribed: () -> Unit) {
    val ctx = LocalContext.current
    var plan by remember { mutableStateOf(2) }
    val plans = listOf(
        Triple("Weekly", "LKR 3,300.00", ""),
        Triple("Monthly", "LKR 9,925.00", "Rs 2481.25 / Week  Save Up to 24%"),
        Triple("Yearly", "LKR 82,750.00", "Rs 1591.35 / Week  Save Up to 51%")
    )
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Choose Your plan", color = Muted, fontSize = 18.sp, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
        plans.forEachIndexed { i, (name, price, sub) ->
            val sel = plan == i
            Box(Modifier.padding(vertical = 8.dp)) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color(0xFF141B33))
                        .border(BorderStroke(if (sel) 2.dp else 1.dp, if (sel) Green else Gray), RoundedCornerShape(24.dp))
                        .clickable { plan = i }.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(26.dp).clip(CircleShape).border(2.dp, if (sel) Green else Gray, CircleShape),
                        contentAlignment = Alignment.Center) {
                        if (sel) Box(Modifier.size(14.dp).clip(CircleShape).background(Green))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(price, color = Color.White, fontSize = 20.sp)
                        if (sub.isNotEmpty()) Text(sub, color = Gold, fontSize = 14.sp)
                    }
                }
                if (i == 2) Text("BEST VALUE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.TopEnd).padding(end = 10.dp)
                        .clip(RoundedCornerShape(12.dp)).background(Gold).padding(horizontal = 14.dp, vertical = 6.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Continue with Google Play", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Gold)
                .clickable {
                    // TODO: connect Google Play Billing (BillingClient) here, then call onSubscribed()
                    Toast.makeText(ctx, "Google Play Billing not connected yet (demo)", Toast.LENGTH_LONG).show()
                }.padding(vertical = 20.dp))
        Text("Secure payment through Google Play. Cancel anytime.", color = Muted, fontSize = 13.sp,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp))
        Text("Other payment options", color = Muted, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp))
        Text("Pay with Crypto  (USDT)", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, Green, RoundedCornerShape(20.dp))
                .clickable {
                    // TODO: open your USDT payment page / contact link
                    Toast.makeText(ctx, "Crypto payment link not set yet", Toast.LENGTH_LONG).show()
                }.padding(vertical = 18.dp))
        Spacer(Modifier.height(10.dp))
        Text("Subscription renews automatically unless cancelled before the renewal date.",
            color = Muted, fontSize = 12.sp)
        Text("Signals are not guaranteed profits.", color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(90.dp))
    }
}

@Composable
fun PerformanceScreen() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Performance", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("Sample numbers - connect real closed-signal results later.", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoBox("Win rate", "78%", Green, Modifier.weight(1f))
            InfoBox("Closed signals", "124", Color.White, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoBox("TP1 hit", "91%", Green, Modifier.weight(1f))
            InfoBox("Stop loss hit", "22%", Red, Modifier.weight(1f))
        }
    }
}

@Composable
fun ProfileScreen(userName: String, subscribed: Boolean, onUpgrade: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(24.dp))
        Box(Modifier.size(90.dp).clip(CircleShape).background(Card).border(1.dp, CardBorder, CircleShape),
            contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Person, null, tint = Green, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(userName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(if (subscribed) "VIP member" else "Free account", color = if (subscribed) Gold else Muted)
        Spacer(Modifier.height(24.dp))
        if (!subscribed) {
            Text("Upgrade to VIP", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Gold)
                    .clickable(onClick = onUpgrade).padding(vertical = 16.dp))
        }
    }
}
