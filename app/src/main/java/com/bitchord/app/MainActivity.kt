package com.bitchord.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val Accent = Color(0xFFBB86FC)
private val Background = Color(0xFF0D0D0D)

data class Song(val title: String, val artist: String, val duration: String) {
    val seconds: Int
        get() {
            val parts = duration.split(":")
            return (parts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 +
                (parts.getOrNull(1)?.toIntOrNull() ?: 0)
        }
}

private fun formatTime(totalSeconds: Int): String =
    "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF121212),
                    surface = Color(0xFF1E1E1E),
                    primary = Accent
                )
            ) {
                BitChordTvScreen()
            }
        }
    }
}

@Composable
fun BitChordTvScreen() {
    val playlist = remember {
        listOf(
            Song("Midnight City", "M83", "4:03"),
            Song("Starboy", "The Weeknd", "3:50"),
            Song("Blinding Lights", "The Weeknd", "3:20"),
            Song("Get Lucky", "Daft Punk", "4:08"),
            Song("Instant Crush", "Daft Punk ft. Julian Casablancas", "5:37")
        )
    }

    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var elapsed by rememberSaveable { mutableIntStateOf(0) }
    val song = playlist[currentIndex]

    fun selectSong(index: Int, play: Boolean) {
        currentIndex = index.coerceIn(0, playlist.lastIndex)
        elapsed = 0
        isPlaying = play
    }

    fun next() {
        if (currentIndex < playlist.lastIndex) selectSong(currentIndex + 1, isPlaying)
        else { isPlaying = false; elapsed = 0 }
    }

    fun previous() {
        if (elapsed > 3 || currentIndex == 0) elapsed = 0
        else selectSong(currentIndex - 1, isPlaying)
    }

    LaunchedEffect(isPlaying, currentIndex) {
        while (isPlaying) {
            delay(1000)
            elapsed++
            if (elapsed >= song.seconds) next()
        }
    }

    val view = LocalView.current
    DisposableEffect(isPlaying) {
        view.keepScreenOn = isPlaying
        onDispose { view.keepScreenOn = false }
    }

    val playFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { playFocus.requestFocus() }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 48.dp, vertical = 27.dp)
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.MediaPlayPause -> { isPlaying = !isPlaying; true }
                    Key.MediaPlay -> { isPlaying = true; true }
                    Key.MediaPause -> { isPlaying = false; true }
                    Key.MediaNext -> { next(); true }
                    Key.MediaPrevious -> { previous(); true }
                    Key.MediaFastForward -> {
                        elapsed = (elapsed + 10).coerceAtMost(song.seconds - 1); true
                    }
                    Key.MediaRewind -> { elapsed = (elapsed - 10).coerceAtLeast(0); true }
                    else -> false
                }
            }
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF262626)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(110.dp),
                    tint = Color.White.copy(alpha = 0.6f)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = song.title,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                fontSize = 20.sp,
                color = Color.LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(20.dp))

            Column(modifier = Modifier.width(360.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF333333))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(
                                (elapsed.toFloat() / song.seconds.coerceAtLeast(1)).coerceIn(0f, 1f)
                            )
                            .fillMaxHeight()
                            .background(Accent)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(elapsed), fontSize = 16.sp, color = Color.Gray)
                    Text(song.duration, fontSize = 16.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TvIconButton(
                    icon = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    onClick = { previous() }
                )
                TvIconButton(
                    icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier.focusRequester(playFocus),
                    size = 84.dp
                )
                TvIconButton(
                    icon = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    onClick = { next() }
                )
            }
        }

        Spacer(Modifier.width(32.dp))

        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
        ) {
            Text(
                text = "Up Next",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
            ) {
                itemsIndexed(playlist) { index, item ->
                    TvSongItem(
                        song = item,
                        isSelected = index == currentIndex,
                        onSelect = { selectSong(index, play = true) }
                    )
                }
            }
        }
    }
}

@Composable
fun TvIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 68.dp
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.15f else 1f, label = "buttonScale")

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .onFocusChanged { focused = it.isFocused }
            .clip(CircleShape)
            .background(if (focused) Color.White else Color(0xFF2A2A2A))
            .border(if (focused) 3.dp else 0.dp, Accent, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (focused) Color.Black else Color.White,
            modifier = Modifier.size(size / 2)
        )
    }
}

@Composable
fun TvSongItem(
    song: Song,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.03f else 1f, label = "itemScale")
    val shape = RoundedCornerShape(14.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .onFocusChanged { focused = it.isFocused }
            .clip(shape)
            .background(
                when {
                    focused -> Color(0xFF383838)
                    isSelected -> Color(0xFF222222)
                    else -> Color(0xFF181818)
                }
            )
            .border(if (focused) 3.dp else 0.dp, Accent, shape)
            .clickable(onClick = onSelect)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Accent else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                fontSize = 16.sp,
                color = Color.LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(text = song.duration, fontSize = 16.sp, color = Color.Gray)
    }
}
