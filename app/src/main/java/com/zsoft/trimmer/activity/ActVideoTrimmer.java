package com.zsoft.trimmer.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.akexorcist.localizationactivity.ui.LocalizationActivity;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.zsoft.trimmer.R;
import com.zsoft.trimmer.library.ui.seekbar.widgets.CrystalRangeSeekbar;
import com.zsoft.trimmer.library.ui.seekbar.widgets.CrystalSeekbar;
import com.zsoft.trimmer.library.utils.AudioVisualizer;
import com.zsoft.trimmer.library.utils.CustomProgressView;
import com.zsoft.trimmer.library.utils.LocaleHelper;
import com.zsoft.trimmer.library.utils.LogMessage;
import com.zsoft.trimmer.library.utils.TrimVideo;
import com.zsoft.trimmer.library.utils.TrimmerUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.Executors;


public class ActVideoTrimmer extends LocalizationActivity {
    //  private StyledPlayerView playerView;
    private AudioVisualizer visualizerView;
    private MediaPlayer videoPlayer;
    private ImageView imagePlayPause;
    private long totalDuration;

    private Dialog dialog;

    private Uri uri;

    private TextView txtStartDuration, txtEndDuration;

    private CrystalRangeSeekbar seekbar;

    private long lastMinValue = 0;

    private long lastMaxValue = 0;

    private MenuItem menuDone;

    private CrystalSeekbar seekbarController;

    private boolean isValidVideo = true;// isVideoEnded;
    private boolean isPlaying = false;

    private Handler seekHandler;

    private Bundle bundle;

    // private ProgressBar progressBar;

    private long currentDuration, lastClickedTime;
    Runnable updateSeekbar = new Runnable() {
        @Override
        public void run() {
            try {
                currentDuration = videoPlayer.getCurrentPosition();
                if (!videoPlayer.isPlaying()) {
                    return;
                }

                if (currentDuration <= lastMaxValue)
                    seekbarController.setMinStartValue((int) currentDuration).apply();
                else
                    videoPlayer.pause();

            } finally {
                seekHandler.postDelayed(updateSeekbar, 10);
            }
        }
    };
    private String outputPath;
    private CustomProgressView progressView;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_video_trimmer);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_baseline_arrow_back_24));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        bundle = getIntent().getExtras();
        progressView = new CustomProgressView(this);


    }

    @Override
    protected void attachBaseContext(@NotNull Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //  playerView = findViewById(R.id.player_view_lib);
        visualizerView = findViewById(R.id.visualizerView);
        imagePlayPause = findViewById(R.id.image_play_pause);
        seekbar = findViewById(R.id.range_seek_bar);
        txtStartDuration = findViewById(R.id.txt_start_duration);
        txtEndDuration = findViewById(R.id.txt_end_duration);
        seekbarController = findViewById(R.id.seekbar_controller);
        //progressBar = findViewById(R.id.progress_circular);
        seekHandler = new Handler();
        //  initPlayer();
        setDataInView();
    }

    private void setDataInView() {
        try {
            Runnable fileUriRunnable = () -> {
                String p = bundle.getString(TrimVideo.TRIM_VIDEO_URI);
                uri = Uri.fromFile(new File(p));
                runOnUiThread(() -> {
                    LogMessage.v("VideoUri:: " + uri);
                    //progressBar.setVisibility(View.GONE);
                    totalDuration = TrimmerUtils.getDuration(ActVideoTrimmer.this, uri);
                    imagePlayPause.setOnClickListener(v ->
                            onVideoClicked());
//                    Objects.requireNonNull(playerView.getVideoSurfaceView()).setOnClickListener(v ->
//                            onVideoClicked());
                    visualizerView.setOnClickListener(v ->
                            onVideoClicked()
                    );
                    buildMediaSource(uri);
                    setUpSeekBar();
                });
            };
            Executors.newSingleThreadExecutor().execute(fileUriRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void onVideoClicked() {
        try {
            if (videoPlayer.isPlaying()) {
                imagePlayPause.setVisibility(View.VISIBLE);
                videoPlayer.pause();
                // visualizerView.disable();
            } else {
                videoPlayer.start();
                seekTo(lastMinValue);
                // visualizerView.enable();
                imagePlayPause.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void seekTo(long sec) {
        Log.e("dkdkdkdkkdkdk", "" + sec + "=>" + (int) (sec));
        if (videoPlayer != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                videoPlayer.seekTo((int) (sec), MediaPlayer.SEEK_CLOSEST);
            else
                videoPlayer.seekTo((int) sec);
        }
        // videoPlayer.seekTo((int) (sec));
    }


    private void buildMediaSource(Uri mUri) {
        try {
            String p = bundle.getString(TrimVideo.TRIM_VIDEO_URI);
            videoPlayer = new MediaPlayer();
            videoPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                videoPlayer.setDataSource(p);
            } catch (IOException e) {
                // Error, do something
            }

            videoPlayer.prepareAsync();
            videoPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    // isVideoEnded = false;
                    imagePlayPause.setVisibility(View.GONE);
                    startProgress();
                    visualizerView.getPathMedia(videoPlayer);
                    LogMessage.v("onPlayerStateChanged: Ready to play.");
                }
            });

            videoPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.e("ddkdkkdkdkd", "onCompletion");
                    LogMessage.v("onPlayerStateChanged: Video ended.");
                    imagePlayPause.setVisibility(View.VISIBLE);
                    //mp.reset();
                    // mp.start();
                    seekTo(lastMinValue);
                    // mp.pause();

                    // isVideoEnded = true;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpSeekBar() {
        seekbar.setVisibility(View.VISIBLE);
        txtStartDuration.setVisibility(View.VISIBLE);
        txtEndDuration.setVisibility(View.VISIBLE);
        seekbarController.setMaxValue(totalDuration).apply();
        seekbar.setMaxValue(totalDuration).apply();
        seekbar.setMaxStartValue((float) totalDuration).apply();
        seekbar.setGap(2).apply();
        lastMaxValue = totalDuration;
        seekbar.setOnRangeSeekbarChangeListener((minValue, maxValue) -> {
            long minVal = (long) minValue;
            long maxVal = (long) maxValue;
            if (lastMinValue != minVal || lastMaxValue != maxVal) {
                seekTo((long) minValue);
            }
            lastMinValue = minVal;
            lastMaxValue = maxVal;
            txtStartDuration.setText(TrimmerUtils.formatSeconds(minVal / (1000)));
            txtEndDuration.setText(TrimmerUtils.formatSeconds(maxVal / (1000)));
//            if (trimType == 3)
//                setDoneColor(minVal, maxVal);
        });

        seekbarController.setOnSeekbarFinalValueListener(value -> {
            long value1 = (long) value;
            if (value1 < lastMaxValue && value1 > lastMinValue) {
                seekTo(value1);
                return;
            }
            if (value1 > lastMaxValue)
                seekbarController.setMinStartValue(lastMaxValue).apply();
            else if (value1 < lastMinValue) {
                seekbarController.setMinStartValue(lastMinValue).apply();
                // if (videoPlayer.isPlaying())
                seekTo(lastMinValue);
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        imagePlayPause.setVisibility(View.VISIBLE);
        videoPlayer.pause();
        //visualizerView.disable();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoPlayer != null)
            videoPlayer.release();
        if (progressView != null && progressView.isShowing())
            progressView.dismiss();
        deleteFile("temp_file");
        stopRepeatingTask();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuDone = menu.findItem(R.id.action_done);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_done) {
            //preventing multiple clicks
            if (SystemClock.elapsedRealtime() - lastClickedTime < 800)
                return true;
            lastClickedTime = SystemClock.elapsedRealtime();
            trimVideo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void trimVideo() {
        if (isValidVideo) {
            String p = bundle.getString(TrimVideo.TRIM_VIDEO_URI);
            //not exceed given maxDuration if has given
            outputPath = getFileName();
            LogMessage.v("outputPath::" + outputPath + new File(outputPath).exists());
            LogMessage.v("sourcePath::" + uri);
            videoPlayer.pause();
            // visualizerView.disable();
            imagePlayPause.setVisibility(View.VISIBLE);
            showProcessingDialog();
            String[] complexCommand;

            //Log.e("kddkdkdkdkdkd",TrimmerUtils.formatCSeconds(lastMinValue/1000)+"=>"+TrimmerUtils.formatCSeconds((lastMaxValue - lastMinValue)/1000));
            complexCommand = new String[]{"-ss", TrimmerUtils.formatCSeconds(lastMinValue / 1000),
                    "-i", p,
                    "-t",
                    TrimmerUtils.formatCSeconds((lastMaxValue - lastMinValue) / 1000),
                    "-async", "1", "-strict", "-2", "-c", "copy", outputPath};
            execFFmpegBinary(complexCommand, true);
        }

    }

    private String getFileName() {

        File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        //if you want to create a sub-dir
        root = new File(root, "TrimmedAudio");
        if (!root.exists())
            root.mkdir();
        // String path = getExternalFilesDir("TrimmedVideo").getPath();
        String path = root.getPath();
        Calendar calender = Calendar.getInstance();
        String fileDateTime = calender.get(Calendar.YEAR) + "_" +
                calender.get(Calendar.MONTH) + "_" +
                calender.get(Calendar.DAY_OF_MONTH) + "_" +
                calender.get(Calendar.HOUR_OF_DAY) + "_" +
                calender.get(Calendar.MINUTE) + "_" +
                calender.get(Calendar.SECOND);
        String fName = "trimmed_video_";
        if (fileName != null && !fileName.isEmpty())
            fName = fileName;
        File newFile = new File(path + File.separator +
                (fName) + fileDateTime + "." + TrimmerUtils.getFileExtension(this, uri));
        return String.valueOf(newFile);
    }


    private void execFFmpegBinary(final String[] command, boolean retry) {
        try {
            new Thread(() -> {
                int result = FFmpeg.execute(command);
                if (result == 0) {
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
                            + outputPath)));
                    dialog.dismiss();
                    Intent intent = new Intent(this, AudioPlayer.class);
                    intent.putExtra(TrimVideo.TRIMMED_VIDEO_PATH, outputPath);
                    startActivity(intent);
                    finish();
                    // }
                } else if (result == 255) {
                    LogMessage.v("Command cancelled");
                    if (dialog.isShowing())
                        dialog.dismiss();
                } else {
                    // Failed case:
                    // line 489 command fails on some devices in
                    // that case retrying with accurateCmt as alternative command
                    if (retry
                        // && !isAccurateCut
                    ) {
                        File newFile = new File(outputPath);
                        if (newFile.exists())
                            newFile.delete();
                        execFFmpegBinary(getAccurateCmd(), false);
                    } else {
                        if (dialog.isShowing())
                            dialog.dismiss();
                        runOnUiThread(() ->
                                Toast.makeText(ActVideoTrimmer.this, "Failed to trim", Toast.LENGTH_SHORT).show());
                    }
                }
            }).start();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] getAccurateCmd() {
        return new String[]{"-ss", TrimmerUtils.formatCSeconds(lastMinValue)
                , "-i", String.valueOf(uri), "-t",
                TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue),
                "-async", "1", outputPath};
    }

    private void showProcessingDialog() {
        try {
            dialog = new Dialog(this);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.alert_convert);
            TextView txtCancel = dialog.findViewById(R.id.txt_cancel);
            dialog.setCancelable(false);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            txtCancel.setOnClickListener(v -> {
                dialog.dismiss();
                FFmpeg.cancel();
            });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void startProgress() {
        updateSeekbar.run();
    }

    void stopRepeatingTask() {
        seekHandler.removeCallbacks(updateSeekbar);
    }

}
