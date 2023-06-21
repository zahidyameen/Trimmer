package com.zsoft.trimmer.adapters

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zsoft.trimmer.databinding.FileLayoutBinding
import com.zsoft.trimmer.activity.ActVideoTrimmer
import com.zsoft.trimmer.library.utils.TrimVideo
import com.zsoft.trimmer.models.Audios
import com.zsoft.trimmer.utils.Utils
import java.io.File


class FileAdapter(val context: Context) : ListAdapter<Audios, FileAdapter.VH>(Comparator) {
    override fun submitList(list: MutableList<Audios>?) {
        super.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(FileLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(
        private val binding: FileLayoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(audio: Audios) {
            binding.title.text = audio.name
            binding.size.text = Utils.fileSize(audio.size)
            val file=File(audio.uri)
            val uri=Uri.fromFile(file)
            Log.e("kdkkdkdkdkdkdkd", "" + uri.toString())
            val mp: MediaPlayer = MediaPlayer.create(context, uri)
            val duration: Long = mp.duration.toLong()
            binding.duration.text = Utils.formatDuration(duration)
            mp.release()
            binding.card.setOnClickListener {
                // val intent: Intent=Intent(context,)
                val intent = Intent(context, ActVideoTrimmer::class.java)
                // val gson = Gson()
                val bundle = Bundle()
                bundle.putString(TrimVideo.TRIM_VIDEO_URI, audio.uri)
                //bundle.putString(TrimVideo.TRIM_VIDEO_OPTION, gson.toJson(options))
                intent.putExtras(bundle)
                context.startActivity(intent)

            }
        }
    }

    companion object {
        private val Comparator = object : DiffUtil.ItemCallback<Audios>() {
            override fun areItemsTheSame(oldItem: Audios, newItem: Audios): Boolean {
                return oldItem.uri == newItem.uri
            }

            override fun areContentsTheSame(oldItem: Audios, newItem: Audios): Boolean {
                return oldItem == newItem
            }
        }
    }

}