package ai.bale.musicplayer.fragments

import ai.bale.musicplayer.R
import ai.bale.musicplayer.databinding.PlayerFragmentBinding
import ai.bale.musicplayer.databinding.PlaylistFragmentBinding
import ai.bale.musicplayer.services.PlayerProvider
import ai.bale.musicplayer.services.PlayerService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import java.lang.Exception
import kotlin.math.roundToInt

class PlayerFragment : Fragment() {
    private lateinit var binding: PlayerFragmentBinding
    private lateinit var player: ExoPlayer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PlayerFragmentBinding.inflate(layoutInflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        player = PlayerProvider.Player
        setPlayerControls()

    }

    private fun setPlayerControls() {
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                if (mediaItem == null) return
                binding.playerMusicTitle.text = mediaItem.mediaMetadata.title

                binding.playerProgressBar.progress = player.currentPosition.toInt()
                binding.playerProgressBar.max = player.duration.toInt()
                binding.playerMusicSinger.text = mediaItem.mediaMetadata.albumArtist

                updateTimePointer()

                binding.playerMusicCover.setImageURI(mediaItem.mediaMetadata.artworkUri)
                if (binding.playerMusicCover.drawable == null) {
                    binding.playerMusicCover.setImageResource(R.drawable.cover_music)
                    ContextCompat.getDrawable(binding.root.context, R.drawable.cover_music)
                        ?.let { updateBackgroundDrawable(it) }
                } else {
                    mediaItem.mediaMetadata.artworkUri?.let { updateBackgroundUri(it) }
                }

                binding.playerPlayButton.setImageResource(R.drawable.pause_icon)
                updateMediaProgress()

                if (!player.isPlaying) {
                    player.play()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == ExoPlayer.STATE_READY) {
                    val currentMedia = player.currentMediaItem ?: return
                    binding.playerMusicTitle.text = currentMedia.mediaMetadata.title
                    binding.playerProgressBar.progress = player.currentPosition.toInt()
                    binding.playerProgressBar.max = player.duration.toInt()
                    binding.playerMusicSinger.text = currentMedia.mediaMetadata.artist ?: "Artist"
                    binding.playerPlayButton.setImageResource(R.drawable.pause_icon)

                    updateTimePointer()

                    binding.playerMusicCover.setImageURI(currentMedia.mediaMetadata.artworkUri)
                    if (binding.playerMusicCover.drawable == null) {
                        binding.playerMusicCover.setImageResource(R.drawable.cover_music)
                        ContextCompat.getDrawable(binding.root.context, R.drawable.cover_music)
                            ?.let { updateBackgroundDrawable(it) }
                    } else {
                        currentMedia.mediaMetadata.artworkUri?.let { updateBackgroundUri(it) }
                    }

                    updateMediaProgress()
                } else {
                    binding.playerPlayButton.setImageResource(R.drawable.play_icon)
                }
            }
        })

        binding.playerNextButton.setOnClickListener { skipNext() }
        binding.playerPrevButton.setOnClickListener { skipPrevious() }
        binding.playerPlayButton.setOnClickListener { playOrPause() }
        setSeekBarListener()

        binding.playerMusicTitle.isSelected = true
        binding.playerMusicTitle.isSingleLine = true
        binding.playerMusicTitle.ellipsize = TextUtils.TruncateAt.MARQUEE
        binding.playerMusicTitle.marqueeRepeatLimit = -1
        binding.playerMusicTitle.isFocusable = true
        binding.playerMusicTitle.isFocusableInTouchMode = true
        binding.playerMusicTitle.requestFocus()

        updateForMiniPlayer()
    }

    private fun updateForMiniPlayer() {
        val currentMedia = player.currentMediaItem ?: return
        binding.playerMusicTitle.text = currentMedia.mediaMetadata.title
        binding.playerProgressBar.progress = player.currentPosition.toInt()
        binding.playerProgressBar.max = player.duration.toInt()
        binding.playerMusicSinger.text = currentMedia.mediaMetadata.artist ?: "Artist"
        binding.playerPlayButton.setImageResource(R.drawable.pause_icon)

        updateTimePointer()
        updateMediaProgress()

        binding.playerMusicCover.setImageURI(currentMedia.mediaMetadata.artworkUri)
        if (binding.playerMusicCover.drawable == null) {
            binding.playerMusicCover.setImageResource(R.drawable.cover_music)
            ContextCompat.getDrawable(binding.root.context, R.drawable.cover_music)
                ?.let { updateBackgroundDrawable(it) }
        } else {
            currentMedia.mediaMetadata.artworkUri?.let { updateBackgroundUri(it) }
        }

        binding.playerProgressBar.progress = player.currentPosition.toInt()

        if (!player.isPlaying) {
            binding.playerPlayButton.setImageResource(R.drawable.play_icon)
        }
    }

    private fun updateTimePointer() {
        binding.playerMusicCurrentTime.text = calculateDuration(player.currentPosition)
        binding.playerMusicFullTime.text = calculateDuration(player.duration)
    }

    private fun calculateDuration(duration: Long): String {
        return DateUtils.formatElapsedTime(duration / 1000)
    }

    private fun updateMediaProgress() {
        val handler = Handler().postDelayed({
            if (player.isPlaying) {
                binding.playerProgressBar.progress = player.currentPosition.toInt()
                updateTimePointer()
            }
            updateMediaProgress()
        }, 1000)
    }

    private fun updateBackgroundUri(uri: Uri) {
        val inputStream = binding.root.context.contentResolver.openInputStream(uri)
        val imageDrawable = Drawable.createFromStream(inputStream, uri.toString())
        val semiTransparentShape = ShapeDrawable(RectShape())
        semiTransparentShape.paint.color = 0xBA000000.toInt()

        val layers = arrayOf(imageDrawable, semiTransparentShape)
        val layerDrawable = LayerDrawable(layers)
        binding.playerMainFrameLayout.background = layerDrawable
    }

    private fun updateBackgroundDrawable(drawable: Drawable) {
        val semiTransparentShape = ShapeDrawable(RectShape())
        semiTransparentShape.paint.color = 0xBA000000.toInt()

        val layers = arrayOf(drawable, semiTransparentShape)
        val layerDrawable = LayerDrawable(layers)
        binding.playerMainFrameLayout.background = layerDrawable
    }

    private fun skipNext() {
        if (player.hasNextMediaItem()) {
            player.seekToNext()
        }
    }

    private fun skipPrevious() {
        if (player.hasPreviousMediaItem()) {
            player.seekToPrevious()
        }
    }

    private fun playOrPause() {
        if (player.isPlaying) {
            player.pause()
            binding.playerPlayButton.setImageResource(R.drawable.play_icon)
            return
        }
        player.play()
        binding.playerPlayButton.setImageResource(R.drawable.pause_icon)
    }

    private fun setSeekBarListener() {
        binding.playerProgressBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            var progress = 0
            var isTracking = false

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (seekBar == null || !fromUser) {
                    return
                }
                this.progress = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isTracking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar == null) {
                    return
                }
                isTracking = false
                player.seekTo(progress.toLong())
                if (!player.isPlaying) {
                    player.play()
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        PlayerProvider.Player = player

        outState.putLong("currentPosition", player.currentPosition)
        outState.putBoolean("isPlaying", player.isPlaying)

    }

}