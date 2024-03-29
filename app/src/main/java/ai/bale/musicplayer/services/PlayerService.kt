package ai.bale.musicplayer.services

import ai.bale.musicplayer.MainActivity
import ai.bale.musicplayer.R
import ai.bale.musicplayer.fragments.PlayListFragment
import ai.bale.musicplayer.fragments.PlayerFragment
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.util.NotificationUtil.IMPORTANCE_HIGH

class PlayerService : Service() {
    private val serviceBinder: ServiceBinder = ServiceBinder()

    inner class ServiceBinder : Binder() {
        fun getPlayerService(): PlayerService {
            return this@PlayerService
        }
    }

    lateinit var player: ExoPlayer
    private lateinit var notificationManager: PlayerNotificationManager
    private val appTitle = "Music Player"
    private val notificationId = 1234;

    private val listener: PlayerNotificationManager.NotificationListener =
        object : PlayerNotificationManager.NotificationListener {
            override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                super.onNotificationCancelled(notificationId, dismissedByUser)
                stopForeground(true)
                if (player.isPlaying) {
                    player.pause()
                }
            }

            override fun onNotificationPosted(
                notificationId: Int,
                notification: Notification,
                ongoing: Boolean
            ) {
                super.onNotificationPosted(notificationId, notification, ongoing)
                startForeground(notificationId, notification)
            }
        }

    private val adapter: PlayerNotificationManager.MediaDescriptionAdapter =
        object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: Player): CharSequence {
                return player.currentMediaItem?.mediaMetadata?.title ?: "Music Title"
            }

            override fun createCurrentContentIntent(player: Player): PendingIntent? {
                val playerIntent = Intent(applicationContext, MainActivity::class.java)
                playerIntent.putExtra("openPlayerFragment",true)
                val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                return PendingIntent.getActivity(applicationContext, 0, playerIntent, flags)
            }

            override fun getCurrentContentText(player: Player): CharSequence? {
                return null
            }

            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap? {
                val view = ImageView(applicationContext)
                view.setImageURI(player.currentMediaItem?.mediaMetadata?.artworkUri)

                val bitmapDrawable: BitmapDrawable = (view.drawable ?: ContextCompat
                    .getDrawable(applicationContext, R.drawable.cover_music)) as BitmapDrawable

                return bitmapDrawable.bitmap
            }
        }

    override fun onCreate() {
        super.onCreate()

        player = PlayerProvider.Player

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        player.setAudioAttributes(audioAttributes, true)

        notificationManager = PlayerNotificationManager
            .Builder(this, notificationId, appTitle).setChannelImportance(IMPORTANCE_HIGH)
            .setNotificationListener(listener)
            .setMediaDescriptionAdapter(adapter)
            .setChannelDescriptionResourceId(R.string.app_name)
            .setNextActionIconResourceId(R.drawable.next_icon)
            .setPreviousActionIconResourceId(R.drawable.previous_icon)
            .setPauseActionIconResourceId(R.drawable.pause_icon)
            .setPlayActionIconResourceId(R.drawable.play_icon)
            .setChannelNameResourceId(R.string.app_name)
            .setStopActionIconResourceId(R.drawable.stop_icon)
            .build()


        notificationManager.setPlayer(player)
        notificationManager.setPriority(PRIORITY_MAX)
        notificationManager.setUseRewindAction(false)
        notificationManager.setUseFastForwardAction(false)
    }


    override fun onBind(intent: Intent): IBinder {
        return serviceBinder
    }


    override fun onDestroy() {
        if (player.isPlaying) {
            player.stop()
        }
        notificationManager.setPlayer(null)
        player.release()
        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }
}