package ai.bale.musicplayer.fragments

import ai.bale.musicplayer.adapter.PlayListAdapter
import ai.bale.musicplayer.databinding.PlaylistFragmentBinding
import ai.bale.musicplayer.models.Music
import ai.bale.musicplayer.services.PlayerProvider
import ai.bale.musicplayer.services.PlayerService
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import java.lang.Exception

class PlayListFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlayListAdapter
    private lateinit var binding: PlaylistFragmentBinding
    private val permission = "Manifest.permission.READ_EXTERNAL_STORAGE"
    private lateinit var storagePerm: ActivityResultLauncher<String>
    private val musicList = mutableListOf<Music>()
    private lateinit var player: ExoPlayer
    private lateinit var serviceConnection: ServiceConnection

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PlaylistFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        player = PlayerProvider.Player
        setService()
        getLocalMusics()
        adapter = PlayListAdapter(musicList)
        recyclerView = binding.playlistRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setService(){
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                val binder = p1 as PlayerService.ServiceBinder
                PlayerProvider.Player = binder.getPlayerService().player
            }

            override fun onServiceDisconnected(p0: ComponentName?) {}
        }

        val intent = Intent(requireContext(), PlayerService::class.java)
        requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun fetchSongs() {
        musicList.clear()
        var queryUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            queryUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATA
        )

        try {
            val cursor = requireContext().contentResolver.query(
                queryUri,
                projection,
                null,
                null,
                null
            ) ?: return

            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)


            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val duration = cursor.getString(durationColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val data = cursor.getString(dataColumn)

                val albumArtworkUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                )

                val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                val music = Music(title, uri, artist, duration, albumArtworkUri, data)
                musicList.add(music)
            }
        } catch (_: Exception) {
        }
    }

    private fun permRequest() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fetchSongs()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                AlertDialog.Builder(context)
                    .setTitle("Requesting Music Permission")
                    .setMessage("Allow Program to fetch songs on your device")
                    .setPositiveButton("allow") { _, _ -> storagePerm.launch(permission) }
                    .setNegativeButton("Cancel") { d, _ ->
                        Toast.makeText(context, "Permission denied by user", Toast.LENGTH_SHORT)
                            .show()
                        d.dismiss()
                    }
                    .show()

        } else{
            Toast.makeText(context, "You canceled to show songs", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getLocalMusics() {
        /*storagePerm = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                fetchSongs()
            } else {
                permRequest()
            }
        }
        storagePerm.launch(permission)*/
        fetchSongs()
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unbindService(serviceConnection)
    }
}