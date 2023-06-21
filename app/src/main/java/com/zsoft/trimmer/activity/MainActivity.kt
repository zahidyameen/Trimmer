package com.zsoft.trimmer.activity

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.zsoft.trimmer.adapters.FileAdapter
import com.zsoft.trimmer.databinding.ActivityMainBinding
import com.zsoft.trimmer.models.Audios


class MainActivity : AppCompatActivity() {

    private lateinit var adapter: FileAdapter
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViews()
    }

    private fun setupViews() = binding.apply {
        adapter = FileAdapter(this@MainActivity)
        rvFiles.adapter = adapter
        //showFiles()
        val audioLists = ArrayList<Audios>()

        val strings = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
        ) // Can include more data for more details and check it.

        val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO)

        val queryUri: Uri = MediaStore.Files.getContentUri("internal")
        //val queryUri: Uri = MediaStore.Files.getContentUri("external")

        val cursor = contentResolver.query(
            queryUri,
            strings,
            selection,
            null,
            MediaStore.Files.FileColumns.DATE_ADDED + " ASC"
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val name: String = cursor.getString(1)
                    val uri = cursor.getString(2)
                    val duration = cursor.getLong(3)
                    val size = cursor.getLong(4)
                    audioLists.add(
                        Audios(
                            name = name, uri = uri, duration = duration, size = size
                        )
                    )
                } while (cursor.moveToNext())
            }
        }
        cursor!!.close()
        adapter.submitList(audioLists)
    }


}