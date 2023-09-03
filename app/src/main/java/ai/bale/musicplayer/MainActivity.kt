package ai.bale.musicplayer

import ai.bale.musicplayer.databinding.ActivityMainBinding
import ai.bale.musicplayer.databinding.PlaylistFragmentBinding
import ai.bale.musicplayer.services.PlayerProvider
import ai.bale.musicplayer.services.PlayerService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import com.google.android.exoplayer2.ExoPlayer

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setPlayer()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val playListBinding = PlaylistFragmentBinding.inflate(layoutInflater)
        val toolbar = playListBinding.toolbar
        setSupportActionBar(toolbar)
    }

    private fun setPlayer(){
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                val binder = p1 as PlayerService.ServiceBinder
                PlayerProvider.Player = ExoPlayer.Builder(this@MainActivity).build()
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
                TODO("Not yet implemented")
            }
        }
        val intent = Intent(binding.root.context, PlayerService::class.java)
        binding.root.context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
}