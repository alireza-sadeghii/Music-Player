package ai.bale.musicplayer.adapter

import ai.bale.musicplayer.R
import ai.bale.musicplayer.databinding.ListItemBinding
import ai.bale.musicplayer.models.Music
import android.graphics.BitmapFactory
import android.util.Log
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.lang.Exception
import kotlin.math.roundToInt

class PlayListViewHolder(private val binding: ListItemBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(music: Music){
        binding.musicTitle.text = music.title
        binding.musicSinger.text = music.singer
        binding.musicDuration.text = calculateDuration(music.duration.toInt())
        if (music.coverUri != null) {
            val contentResolver = binding.root.context.contentResolver
            try {
                val inputStream = contentResolver.openInputStream(music.coverUri)
                if (inputStream != null) {
                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeStream(inputStream, null, options)
                    inputStream.close()
                    if (options.outWidth > 0 && options.outHeight > 0) {
                        binding.musicCoverImage.setImageURI(music.coverUri)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        binding.root.setOnClickListener {
            Log.v("checking",music.title)
            try {
                val navController = binding.root.findNavController()
                val navGraph = navController.graph
                val actionId = R.id.action_playListFragment_to_playerFragment
                val action = navGraph.getAction(actionId)

                navController.navigate(actionId)
            }catch (e:Exception){
                Log.v("checking", e.message.toString())
            }
        }
    }

    private fun calculateDuration(duration: Int): String {
        var seconds = (duration / 1000F).roundToInt()
        val minutes = (seconds / 60F).roundToInt()
        seconds %= 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}