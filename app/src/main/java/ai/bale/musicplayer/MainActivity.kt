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
import android.util.Log
import com.google.android.exoplayer2.ExoPlayer

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        PlayerProvider.Player = ExoPlayer.Builder(this).build()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val playListBinding = PlaylistFragmentBinding.inflate(layoutInflater)
        val toolbar = playListBinding.toolbar
        setSupportActionBar(toolbar)
    }
}