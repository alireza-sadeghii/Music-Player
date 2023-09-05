package ai.bale.musicplayer.adapter

import ai.bale.musicplayer.R
import ai.bale.musicplayer.databinding.ListItemBinding
import ai.bale.musicplayer.models.Music
import ai.bale.musicplayer.services.PlayerProvider
import ai.bale.musicplayer.services.PlayerService
import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.text.format.DateUtils
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import java.io.IOException
import java.lang.Exception
import kotlin.math.roundToInt

class PlayListViewHolder(private val binding: ListItemBinding, private val musics: List<Music>) :
    RecyclerView.ViewHolder(binding.root) {
    private lateinit var player: ExoPlayer
    fun bind(music: Music) {
        player = PlayerProvider.Player
        binding.musicTitle.text = music.title
        binding.musicSinger.text = music.singer
        binding.musicDuration.text = calculateDuration(music.duration.toLong())

        try {
            binding.musicCoverImage.setImageURI(music.coverUri)

            if (binding.musicCoverImage.drawable == null) {
                binding.musicCoverImage.setImageResource(R.drawable.cover_music)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        binding.root.setOnClickListener {
            try {
                binding.root.context.startService(
                    Intent(
                        binding.root.context.applicationContext,
                        PlayerService::class.java
                    )
                )

                binding.root.context.startService(
                    Intent(
                        binding.root.context.applicationContext,
                        PlayerService::class.java
                    )
                )
                val navController = binding.root.findNavController()
                navController.navigate(R.id.action_playListFragment_to_playerFragment)

                if (!player.isPlaying) {
                    player.setMediaItems(getPlayerMedia(), position, 0)
                } else {
                    player.pause()
                    player.seekTo(position, 0)
                }
                player.prepare()
                player.play()

            } catch (e: Exception) {
                Log.v("checking", e.message.toString())
            }
        }
    }


    private fun getPlayerMedia(): List<MediaItem> {
        val mediaItems = mutableListOf<MediaItem>()

        for (song in musics) {
            val newMedia = MediaItem.Builder()
                .setUri(song.uri)
                .setMediaMetadata(getSongData(song))
                .build()
            mediaItems.add(newMedia)
        }

        return mediaItems
    }

    private fun getSongData(song: Music): MediaMetadata {
        return MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtworkUri(song.coverUri)
            .build()
    }

    private fun calculateDuration(duration: Long): String {
        return DateUtils.formatElapsedTime(duration / 1000)
    }
}