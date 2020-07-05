package com.example.artghul_drama;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.innovattic.rangeseekbar.RangeSeekBar;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;


public class player extends AppCompatActivity {
    private boolean isSekking,isNeedCrop ;
    private int duration;

    private static final int SELECT_AUDIO = 1002;

    private String inAudio;
    private String inVideo;
    private  String root;
    private String outPath;
    private Thread muxThread;

    private RelativeLayout cropView;
    private RangeSeekBar rangeSeekbar;
    private TextView current_audio, audio_total,crop_duraion,audio_title;
    private VideoView videoPlayer;
    private ProgressBar progressBar;

    private Timer audioTimer;
    private MediaPlayer audioPlyaer = null;
    private int start_mil,stop_mil;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
//        progressBar = findViewById(R.id.progressBar_player);
        inVideo = getIntent().getExtras().getString("PATH");

        final TextView total_time  = findViewById(R.id.total_sec_textView);
        final TextView current_time = findViewById(R.id.current_sec_textView);
        videoPlayer = findViewById(R.id.videoView);
        final SeekBar seek = findViewById(R.id.seekBar);
        current_audio = findViewById(R.id.current_sec_audio);
        audio_total = findViewById(R.id.total_sec_audio);
        crop_duraion = findViewById(R.id.crop_duraion_textView);
        audio_title = findViewById(R.id.audio_title_textView);
        rangeSeekbar = findViewById(R.id.rangeSeekBar);
        cropView = findViewById(R.id.cropView);
        progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);
        videoPlayer.setVideoURI(Uri.parse(inVideo));
        videoPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                duration = mediaPlayer.getDuration();
                int sec = duration/1000;
                total_time.setText(String.format("%02d:%02d", sec / 60, sec % 60));
                current_time.setText("00:00");
                seek.setMax(duration);
                Log.d("max",duration+"---");
            }
        });

        rangeSeekbar.setSeekBarChangeListener(new RangeSeekBar.SeekBarChangeListener() {
            @Override
            public void onStartedSeeking() {
                audioPlyaer.pause();
            }

            @Override
            public void onStoppedSeeking() {
                audioPlyaer.start();
            }

            @Override
            public void onValueChanged(int i, int i1) {
                stop_mil = i1;
                start_mil = i;
                int c = (i1-i)/1000;
                crop_duraion.setText(String.format("%02d:%02d",c/60,c%60));
                int start = i/1000;
                current_audio.setText(String.format("%02d:%02d",start/60,start%60));
                int stop = i1/1000;
                audio_total.setText(String.format("%02d:%02d",stop/60,stop%60));
                isNeedCrop = start_mil > 100 || stop_mil< audioPlyaer.getDuration()-100;
            }
        });


        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Log.d("SeekBar","i="+i+"  , "+b);
                if (isSekking){
                    int sec = i/1000;
                    current_time.setText(String.format("%02d:%02d", sec / 60, sec % 60));
                    videoPlayer.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("SeekBar","onStartTrackingTouch=--------------");
                isSekking = true;
                videoPlayer.pause();

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("SeekBar","onStopTrackingTouch=-----------------");
                isSekking = false;
            videoPlayer.start();
            }
        });
        final Timer videoTimer = new Timer();
        videoTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       if (videoPlayer.isPlaying()){
                           int sec = videoPlayer.getCurrentPosition()/1000;
                           current_time.setText(String.format("%02d:%02d", sec / 60, sec % 60));
                           seek.setProgress(videoPlayer.getCurrentPosition());

                       }
                   }
               });
            }
        },500,1000);


        videoPlayer.start();

//        root = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+File.pathSeparator;
        root = Environment.getExternalStorageDirectory().getAbsolutePath()+"/AudioVideoMixer/";
        File f = new File(root);
        if (!f.isDirectory()){
            f.mkdir();
        }
        outPath = root + "output_video.mp4";
    }
    public void add_audio(View view){
        videoPlayer.pause();
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent, SELECT_AUDIO);
        }else{
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setDataAndType(Uri.parse(inAudio), "Audio/*");
            startActivityForResult(i,SELECT_AUDIO);
        }

    }
    public void mux_audio(final View view){
        Log.d("DONE","------start = "+start_mil+"    stop=="+stop_mil + "  toatal="+audioPlyaer.getDuration());
        cropView.setVisibility(View.GONE);
        audioTimer.cancel();
        audioTimer.purge();
        audioPlyaer.stop();
        audioPlyaer.release();
        audioPlyaer = null;
        progressBar.setVisibility(View.VISIBLE);
        muxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (isNeedCrop){
                    String ext = inAudio.substring(inAudio.lastIndexOf("."));
                    Log.d("--mux_audio--","-----ext-----="+ext);
                    //File f= new File(root + "inacrop."+ext);
//                    FFmpeg ffmpeg = FFmpeg.getInstance(player.this);
                    FFmpeg.execute("-y -ss "+(start_mil/1000)+" -i "+inAudio+" -t "+(stop_mil/1000)+" -c copy "+ root + "inacrop"+ext);
//                    ffmpeg.execute(new String[]{"-y -ss " + (start_mil / 1000) + " -i " + inAudio + " -t " + (stop_mil / 1000) + " -c copy " + root + "inacrop" + ext}, new FFcommandExecuteResponseHandler() {
//                        @Override
//                        public void onSuccess(String message) {
//
//                        }
//
//                        @Override
//                        public void onProgress(String message) {
//
//                        }
//
//                        @Override
//                        public void onFailure(String message) {
//
//                        }
//
//                        @Override
//                        public void onStart() {
//
//                        }
//
//                        @Override
//                        public void onFinish() {
//
//                        }
//                    });
                    inAudio = root + "inacrop"+ext;
                    Log.d("--FFMPEG","-----------cropped to AAC-------------------"+inAudio);
                    if(ext.toLowerCase().equals(".mp3")) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) { }
                        FFmpeg.execute("-y -i " + inAudio + " -c:a aac -b:a 192k " + root + "ina.aac");
                        inAudio = root + "ina.aac";
                        Log.d("--FFMPEG","-----------conrted to AAC-------------------"+inAudio);
                    }
                    final boolean succ = muxing1();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(succ){
                                videoPlayer.pause();
                                videoPlayer.setVideoPath(outPath);
                                videoPlayer.start();
                                //new AlertDialog.Builder(player.this).setPositiveButton("OK",null).setMessage("Mixing completed successfully").show();
                            }else{
                                new AlertDialog.Builder(player.this).setPositiveButton("OK",null).setMessage("Mixing Failed please try again!").show();
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });

                }else{
                    String[] a =inAudio.split("\\.");
                    String ext = a[a.length-1];
                    if(ext.toLowerCase().equals("mp3")) {
                       FFmpeg.execute("-y -i " + inAudio + " -c:a aac -b:a 192k " + root + "ina.aac");
                        inAudio = root + "ina.aac";
                    }
                    final boolean succ = muxing1();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(succ){
                                videoPlayer.pause();
                                videoPlayer.setVideoPath(outPath);
                                videoPlayer.start();
                               // new AlertDialog.Builder(player.this).setPositiveButton("OK",null).setMessage("Mixing completed successfully").show();
                            }else{

                                new AlertDialog.Builder(player.this).setPositiveButton("OK",null).setMessage("Mixing Failed please try again!").show();
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });

                }
            }
        });
        muxThread.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_AUDIO){
            if(resultCode == RESULT_OK){
                inAudio = getPath(data.getData());
                initAudioPlayer();
            }else{
                new AlertDialog.Builder(this).setMessage("Audio picking failed please try again!").setPositiveButton("Ok",null).show();
            }

        }
    }

    private void initAudioPlayer() {
        cropView.setVisibility(View.VISIBLE);
        if (audioPlyaer == null){
            audioPlyaer = new MediaPlayer();
            audioPlyaer.setLooping(true);
            audioPlyaer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    rangeSeekbar.setMax(mediaPlayer.getDuration());
                    stop_mil = mediaPlayer.getDuration();
                    mediaPlayer.start();
                    audioTimer = new Timer();
                    audioTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                           runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   if (audioPlyaer.isPlaying()){
                                       int sec = audioPlyaer.getCurrentPosition();
                                       if(sec<stop_mil) {
                                           current_audio.setText(String.format("%02d:%02d", (sec/1000)/60,(sec/1000)%60));
                                       }else{
                                           audioPlyaer.seekTo(start_mil);
                                       }

                                   }
                               }
                           });
                        }
                    },500,1000);
                }
            });
        }
        try {
            audioPlyaer.setDataSource(inAudio);

            audioPlyaer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if(cursor!=null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        else return null;
    }
    private boolean muxing1() {
        File saveFile = new File(outPath);
        if (saveFile.exists()) {
            saveFile.delete();
        }

        try {
            saveFile.createNewFile();
            // get the video file duration in microseconds
            long duration = getVideoDuration(inVideo)*1000;
            Log.d("DDDDURATION0-", String.valueOf(duration));
//            saveFile.createNewFile();

            MediaExtractor videoExtractor = new MediaExtractor();
            videoExtractor.setDataSource(inVideo);

            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(inAudio);

            MediaMuxer muxer = new MediaMuxer(outPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            videoExtractor.selectTrack(0);
            MediaFormat videoFormat = videoExtractor.getTrackFormat(0);
            int videoTrack = muxer.addTrack(videoFormat);

            audioExtractor.selectTrack(0);
            MediaFormat audioFormat = audioExtractor.getTrackFormat(0);
            int audioTrack = muxer.addTrack(audioFormat);

            boolean sawEOS = false;
            int offset = 100;
            int sampleSize = 1000 * 1024;
            ByteBuffer videoBuf = ByteBuffer.allocate(sampleSize);
            ByteBuffer audioBuf = ByteBuffer.allocate(sampleSize);
            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();

            videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

            muxer.start();

            int frameRate = videoFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
            int videoSampleTime = (int) (1000 * 1000 / (long)frameRate);
            while (!sawEOS) {
                videoBufferInfo.offset = offset;
                videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);
                Log.d("vvvvv", String.valueOf(videoBufferInfo.size));
                if (videoBufferInfo.size < 0) {
                    sawEOS = true;
                    videoBufferInfo.size = 0;

                } else {
                    videoBufferInfo.presentationTimeUs += videoSampleTime;
                    videoBufferInfo.flags = videoExtractor.getSampleFlags();
                    muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo);
                    videoExtractor.advance();
                }
            }

            boolean sawEOS2 = false;
            long sampleTime = 0L;
            while (!sawEOS2) {
                Log.d("iiiiiii", String.valueOf(audioBufferInfo.presentationTimeUs));
                audioBufferInfo.offset = offset;
                audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);
                if (audioBufferInfo.presentationTimeUs >= duration) {
                    sawEOS2 = true;
                    audioBufferInfo.size = 0;
                } else {
                    if (audioBufferInfo.size < 0) {
                        sampleTime = audioBufferInfo.presentationTimeUs;
                        audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                        continue;
                    }
                }
                audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime() + sampleTime;
                audioBufferInfo.flags = audioExtractor.getSampleFlags();
                muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo);
                audioExtractor.advance();
            }

            muxer.stop();
            muxer.release();
            videoExtractor.release();
            audioExtractor.release();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    private long getVideoDuration(String inVideo) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//use one of overloaded setDataSource() functions to set your data source
        retriever.setDataSource(this, Uri.parse(inVideo));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time );

        retriever.release();
        return timeInMillisec;
    }
}
