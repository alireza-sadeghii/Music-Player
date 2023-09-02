package ai.bale.musicplayer.fragments

import ai.bale.musicplayer.databinding.PlayerFragmentBinding
import ai.bale.musicplayer.services.PlayerService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class PlayerFragment(): Fragment() {
    private lateinit var binding: PlayerFragmentBinding
    private lateinit var playerService: PlayerService

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PlayerService.ServiceBinder
            playerService = binder.getPlayerService()
            initUI()
        }

        override fun onServiceDisconnected(name: ComponentName?) {}
    }

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
        val intent = Intent(requireContext(), PlayerService::class.java)
        requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun initUI() {
        binding.playerPlayButton.setOnClickListener {
            if (!playerService.playing()) {
                playerService.play()
            }else{
                playerService.pause()
            }
        }

        updateUI(playerService.playing() ?: false)
    }

    private fun updateUI(isPlaying: Boolean) {
        if (isPlaying) {
            binding.playerPauseButton.visibility = View.VISIBLE
            binding.playerPlayButton.visibility = View.GONE
        } else {
            binding.playerPauseButton.visibility = View.GONE
            binding.playerPlayButton.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unbindService(serviceConnection)
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(requireContext(), PlayerService::class.java)
        requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onPause() {
        super.onPause()
        requireContext().unbindService(serviceConnection)
    }
}