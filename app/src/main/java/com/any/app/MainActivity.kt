package com.any.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.any.app.ui.theme.AnyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnyTheme {
                AnyApp()
            }
        }
    }
}

private object Destination {
    const val HOME = 0
    const val ANY = 1
    const val MARKET = 2
    const val SETTINGS = 3
}

@Composable
fun AnyApp() {
    var destination by rememberSaveable { mutableIntStateOf(Destination.HOME) }
    val showBottomBar = destination == Destination.HOME || destination == Destination.ANY

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (destination != Destination.HOME) {
                AnyTopBar(
                    title = when (destination) {
                        Destination.MARKET -> "any market"
                        Destination.SETTINGS -> "设置"
                        else -> "Any"
                    },
                    showBack = destination != Destination.ANY,
                    onBack = { destination = Destination.ANY }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                AnyBottomBar(
                    selected = destination,
                    onSelect = { destination = it }
                )
            }
        }
    ) { innerPadding ->
        when (destination) {
            Destination.HOME -> HomeScreen(Modifier.padding(innerPadding))
            Destination.ANY -> AnyMenuScreen(
                onOpenMarket = { destination = Destination.MARKET },
                onOpenSettings = { destination = Destination.SETTINGS },
                modifier = Modifier.padding(innerPadding)
            )
            Destination.MARKET -> MarketScreen(Modifier.padding(innerPadding))
            Destination.SETTINGS -> SettingsScreen(Modifier.padding(innerPadding))
        }
    }
}

@Composable
private fun AnyBottomBar(selected: Int, onSelect: (Int) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AnyBottomItem(
                title = "首页",
                icon = { Icon(Icons.Outlined.Home, contentDescription = "首页") },
                selected = selected == Destination.HOME,
                onClick = { onSelect(Destination.HOME) }
            )
            AnyBottomItem(
                title = "Any",
                icon = { Icon(Icons.Outlined.Extension, contentDescription = "Any") },
                selected = selected == Destination.ANY,
                onClick = { onSelect(Destination.ANY) }
            )
        }
    }
}

@Composable
private fun RowScope.AnyBottomItem(
    title: String,
    icon: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides contentColor
        ) {
            icon()
            Spacer(Modifier.width(8.dp))
            Text(title, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnyTopBar(title: String, showBack: Boolean, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
private fun HomeScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Any",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "一切皆可组合。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AnyMenuScreen(
    onOpenMarket: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MenuGroup {
            AnyMenuRow(
                title = "any market",
                icon = { Icon(Icons.Outlined.Storefront, contentDescription = null) },
                onClick = onOpenMarket
            )
            MenuDivider()
            AnyMenuRow(
                title = "设置",
                icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                onClick = onOpenSettings
            )
        }
    }
}

@Composable
private fun MenuGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 0.dp,
        content = { Column(content = content) }
    )
}

@Composable
private fun MenuDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 68.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    )
}

@Composable
private fun AnyMenuRow(
    title: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            icon()
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MarketScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "选择你需要的模块",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "让 Any 保持轻便，只留下真正有用的功能。",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        PluginCard(
            name = "Any Note",
            description = "记录想法、笔记和灵感。",
            icon = { Icon(Icons.AutoMirrored.Outlined.Notes, contentDescription = null) }
        )
        PluginCard(
            name = "Any Train",
            description = "训练计划、打卡和进步统计。",
            icon = { Icon(Icons.Outlined.FitnessCenter, contentDescription = null) }
        )
    }
}

@Composable
private fun PluginCard(
    name: String,
    description: String,
    icon: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(
                    androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    icon()
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.SemiBold)
                Text(
                    description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Text(
                text = "即将推出",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isChecking by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var status by rememberSaveable { mutableStateOf("检查 Any 是否有新版本") }
    var downloadUrl by rememberSaveable { mutableStateOf("") }
    var sha256 by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Any", fontWeight = FontWeight.SemiBold)
                Text(
                    "当前版本 v${BuildConfig.VERSION_NAME}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        isChecking = true
                        status = "正在检查 GitHub Release…"
                        downloadUrl = ""
                        sha256 = ""
                        AnyUpdateChecker.checkLatestRelease(
                            onSuccess = { release ->
                                isChecking = false
                                val currentTags = setOf(
                                    BuildConfig.VERSION_NAME,
                                    "v${BuildConfig.VERSION_NAME}"
                                )
                                status = if (release.tagName in currentTags) {
                                    "当前已经是最新版本"
                                } else {
                                    "发现新版本 ${release.tagName}：${release.title}"
                                }
                                downloadUrl = release.downloadUrl
                                sha256 = release.sha256
                            },
                            onError = { message ->
                                isChecking = false
                                status = message
                            }
                        )
                    },
                    enabled = !isChecking
                ) {
                    Text(if (isChecking) "检查中…" else "检查更新")
                }
                if (downloadUrl.isNotBlank()) {
                    Button(
                        onClick = {
                            isDownloading = true
                            status = "正在下载更新…"
                            ApkUpdater.downloadAndInstall(
                                context = context,
                                downloadUrl = downloadUrl,
                                expectedSha256 = sha256,
                                onProgress = { progress ->
                                    status = "正在下载更新… $progress%"
                                },
                                onReady = {
                                    isDownloading = false
                                    status = "下载完成，等待系统安装确认"
                                },
                                onError = { message ->
                                    isDownloading = false
                                    status = message
                                }
                            )
                        },
                        enabled = !isDownloading && !isChecking
                    ) {
                        Text(if (isDownloading) "下载中…" else "下载并安装更新")
                    }
                }
                Text(
                    status,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AnyPreview() {
    AnyTheme {
        AnyApp()
    }
}
