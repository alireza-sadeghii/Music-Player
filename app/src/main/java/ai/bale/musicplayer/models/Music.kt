package ai.bale.musicplayer.models

import android.net.Uri

data class Music(
    val title: String,
    val singer: String,
    val duration: String,
    val coverUri: Uri,
    val data: String
)
