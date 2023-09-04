package ai.bale.musicplayer.services

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

object PlayerProvider {
    lateinit var Player: ExoPlayer
    var lastPlayed: MediaItem? = null
}