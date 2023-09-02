package ai.bale.musicplayer.adapter

import ai.bale.musicplayer.databinding.ListItemBinding
import ai.bale.musicplayer.models.Music
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


class PlayListAdapter(private val musics: List<Music>): RecyclerView.Adapter<PlayListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemBinding.inflate(inflater, parent, false)
        return PlayListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return musics.size
    }

    override fun onBindViewHolder(holder: PlayListViewHolder, position: Int) {
        val music = musics[position]
        holder.bind(music)
    }
}