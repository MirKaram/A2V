package com.example.artghul_drama;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class adapter_video extends RecyclerView.Adapter<adapter_video.myHolder> {
    private List<String> list = new ArrayList<>();
    public adapter_video(){
        String fp = Environment.getExternalStorageDirectory().getAbsolutePath()+"/AudioVideoMixer/";
        File f = new File(fp);
        if (f.isDirectory()) {
            for (File p : f.listFiles()) {
                list.add(p.getAbsolutePath());
            }
        }
    }
    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_video,parent,false);
        return new myHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull myHolder holder, int position) {
        holder.name.setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class myHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public myHolder(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.name_textView);
        }
    }
}
