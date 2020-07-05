package com.example.artghul_drama;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;


public class MainActivity extends AppCompatActivity {


    private static final int SELECT_VIDEO = 1;

   // private Button btnAudioTrim;
    private static final int REQUEST_ID_PERMISSIONS = 1;
    private ProgressBar progressBar;
    private final String TAG ="MUXER";
    private String inAudio;
    private String inVideo;
    private  String root;
    private String outPath;
    private video_adpter adapterVideo;
    private List<String> videoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        String vl = PreferenceManager.getDefaultSharedPreferences(this).getString("VIDEO_LIST",null);
//        if (vl!=null){
//            videoList = new Gson().fromJson(vl,new TypeToken<List<String>>(){}.getType());
//        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED  ){
           ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
        }
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, SELECT_VIDEO);

            }
        });


        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
//        mux.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mux.setEnabled(false);
//
//                progressBar.setVisibility(View.VISIBLE);
////                Controller.getInstance().run(new String[]{  "-i", inAudio, "-c:a", "libfdk_aac", root+"ina.m4a"});
////                Log.d("coo-",FFmpeg.getMediaInformation(root+"ina.mp3").getFormat());
//                String[] a =inAudio.split("\\.");
//                String ext = a[a.length-1];
//                if(ext.toLowerCase().equals("mp3")) {
//                    FFmpeg.execute("-i " + inAudio + " -c:a aac -b:a 192k " + root + "ina.aac");
//                    inAudio = root + "ina.aac";
//                }
//                if(muxing1()){
//                    new AlertDialog.Builder(MainActivity.this).setPositiveButton("OK",null).setMessage("Mixing completed successfully").show();
//                }else{
//                    new AlertDialog.Builder(MainActivity.this).setPositiveButton("OK",null).setMessage("Mixing Failed please try again!").show();
//                }
//                videoList.add(outPath);
//                adapterVideo.notifyDataSetChanged();
//                mux.setEnabled(true);
//
//                progressBar.setVisibility(View.GONE);
//                PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putString("VIDEO_LIST",new Gson().toJson(videoList)).apply();
//            }
//        });
//        inVideo = root + "inv.mp4";
//        inAudio = root + "ina.MP3";
        root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+File.pathSeparator;
        outPath = root + "output_video.mp4";
    }

    public void open_videos_activity(View view){
        startActivity(new Intent(this,videos_list.class));
    }
    private void downloadLinks() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                String uri = "https://drive.google.com/uc?export=download&id=10huIHzVTp8CTjCgXFyq4be7Ey0vsLxxP";

                    URLConnection con = new URL(uri).openConnection();
                    con.connect();
                    final BufferedReader bf = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    final StringBuffer stringBuffer = new StringBuffer();
                    for (String l; (l = bf.readLine()) !=null;){
                       stringBuffer.append(l);
                    }
                    bf.close();
                    //Thread.sleep(600);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshList(stringBuffer.toString());
                            progressBar.setVisibility(View.GONE);
                        }
                    });

                }catch (Exception e){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                    Log.e("doewn",e.getMessage());
                }
            }
        }).start();
    }

    private void refreshList(String data) {
//        RecyclerView rv = findViewById(R.id.recyclerView_videos);
//        rv.setLayoutManager(new GridLayoutManager(this,2));
//        List<String> l = new Gson().fromJson(data,new TypeToken<List<String>>(){}.getType());
//        rv.setAdapter(new video_adpter(this,l));

    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO},
                REQUEST_ID_PERMISSIONS);
    }

    private boolean checkStoragePermission() {
        return (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ID_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Permission granted, Click again", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
             if (requestCode == SELECT_VIDEO) {
                inVideo = getPath(data.getData());
                if(inVideo == null) {
                    Toast.makeText(MainActivity.this,"Video Picking failed try again!",Toast.LENGTH_LONG);
                        //finish();
                } else {
                    Intent intent = new Intent(this,player.class);
                    intent.putExtra("PATH",inVideo);
                    startActivity(intent);
                }
            }
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

    private void muxing() {
        try {

            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "final2.mp4");
            file.createNewFile();

            MediaExtractor videoExtractor = new MediaExtractor();
            videoExtractor.setDataSource(inVideo);

            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(inAudio);

            Log.d(TAG, "Video Extractor Track Count " + videoExtractor.getTrackCount());
            Log.d(TAG, "Audio Extractor Track Count " + audioExtractor.getTrackCount());

            MediaMuxer muxer = new MediaMuxer(outPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            videoExtractor.selectTrack(0);
            MediaFormat videoFormat = videoExtractor.getTrackFormat(0);
            int videoTrack = muxer.addTrack(videoFormat);

            audioExtractor.selectTrack(0);
            MediaFormat audioFormat = audioExtractor.getTrackFormat(0);
            int audioTrack = muxer.addTrack(audioFormat);

            Log.d(TAG, "Video Format " + videoFormat.toString());
            Log.d(TAG, "Audio Format " + audioFormat.toString());

            boolean sawEOS = false;
            int frameCount = 0;
            int offset = 100;
            int sampleSize = 256 * 1024;
            ByteBuffer videoBuf = ByteBuffer.allocate(sampleSize);
            ByteBuffer audioBuf = ByteBuffer.allocate(sampleSize);
            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();


            videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

            muxer.start();

            while (!sawEOS) {
                videoBufferInfo.offset = offset;
                videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);


                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
                    Log.d(TAG, "saw input EOS.");
                    sawEOS = true;
                    videoBufferInfo.size = 0;

                } else {
                    videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
                    videoBufferInfo.flags = videoExtractor.getSampleFlags();
                    muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo);
                    videoExtractor.advance();


                    frameCount++;
                    Log.d(TAG, "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs + " Flags:" + videoBufferInfo.flags + " Size(KB) " + videoBufferInfo.size / 1024);
                    Log.d(TAG, "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs + " Flags:" + audioBufferInfo.flags + " Size(KB) " + audioBufferInfo.size / 1024);

                }
            }

            Toast.makeText(getApplicationContext(), "frame:" + frameCount, Toast.LENGTH_SHORT).show();


            boolean sawEOS2 = false;
            int frameCount2 = 0;
            while (!sawEOS2) {
                frameCount2++;

                audioBufferInfo.offset = offset;
                audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);

                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
                    Log.d(TAG, "saw input EOS.");
                    sawEOS2 = true;
                    audioBufferInfo.size = 0;
                } else {
                    audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                    audioBufferInfo.flags = audioExtractor.getSampleFlags();
                    muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo);
                    audioExtractor.advance();


                    Log.d(TAG, "--Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs + " Flags:" + videoBufferInfo.flags + " Size(KB) " + videoBufferInfo.size / 1024);
                    Log.d(TAG, "--Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs + " Flags:" + audioBufferInfo.flags + " Size(KB) " + audioBufferInfo.size / 1024);

                }
            }

            Toast.makeText(getApplicationContext(), "frame:" + frameCount2, Toast.LENGTH_SHORT).show();

            muxer.stop();
            muxer.release();


        } catch (IOException e) {
            Log.d(TAG, "Mixer Error 1 " + e.getMessage());
        } catch (Exception e) {
            Log.d(TAG, "Mixer Error 2 " + e.getMessage());
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
