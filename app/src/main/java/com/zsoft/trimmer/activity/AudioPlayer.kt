package com.zsoft.trimmer.activity

import android.content.DialogInterface
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.zsoft.trimmer.R
import com.zsoft.trimmer.databinding.ActivityAudioPlayerBinding
import com.zsoft.trimmer.library.utils.TrimVideo
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class AudioPlayer : AppCompatActivity() {
    lateinit var binding: ActivityAudioPlayerBinding
    lateinit var mediaPlayer: MediaPlayer
    private val myHandler: Handler = Handler(Looper.getMainLooper())
    private var startTime: Int = 0
    private var finalTime: Int = 0
    private var oneTimeOnly: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbar.setNavigationOnClickListener(View.OnClickListener { onBackPressed() })
        val bundle = intent.extras
        val p: String = bundle?.getString(TrimVideo.TRIMMED_VIDEO_PATH) ?: ""

        val file = File(p)
        binding.toolbar.title = file.name
        mediaPlayer = MediaPlayer()
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        try {
            mediaPlayer.setDataSource(p)
        } catch (e: IOException) {
            // Error, do something
        }
        mediaPlayer.prepareAsync()
        binding.visualizerView.getPathMedia(mediaPlayer)
        binding.imagePlayPause.setOnClickListener {
            play()
        }
        binding.visualizerView.setOnClickListener {
            play()
        }
        binding.info.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            // set message of alert dialog
            dialogBuilder.setMessage("Download/TrimmedAudio/" + file.name)
                // if the dialog is cancelable
                .setCancelable(false)
                // positive button text and action
                .setPositiveButton("OK", DialogInterface.OnClickListener { d, _ ->
                    d.dismiss()
                })
            val alert = dialogBuilder.create()
            alert.setTitle("Storage")
            alert.show()
        }
        binding.delete.setOnClickListener {
            if (file.exists()) {
                file.delete()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()

    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.pause()
        binding.imagePlayPause.visibility = View.VISIBLE
    }

    private fun play() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
            binding.imagePlayPause.visibility = View.GONE

            finalTime = mediaPlayer.duration
            startTime = mediaPlayer.currentPosition

            if (oneTimeOnly == 0) {
                binding.seekBar.max = finalTime
                oneTimeOnly = 1
            }

            binding.txt2.text = java.lang.String.format(
                "%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()))
            )

            binding.txt1.text = java.lang.String.format(
                "%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(startTime.toLong()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()))
            )

            binding.seekBar.progress = startTime
            myHandler.postDelayed(UpdateSongTime, 100)
        } else {
            mediaPlayer.pause()
            binding.imagePlayPause.visibility = View.VISIBLE
            myHandler.removeCallbacks(UpdateSongTime)
        }

        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mediaPlayer.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
    }

    private val UpdateSongTime: Runnable = object : Runnable {
        override fun run() {
            startTime = mediaPlayer.currentPosition
            binding.txt1.text = java.lang.String.format(
                "%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(startTime.toLong()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()))
            )
            binding.seekBar.progress = startTime as Int
            myHandler.postDelayed(this, 100)
        }
    }

}