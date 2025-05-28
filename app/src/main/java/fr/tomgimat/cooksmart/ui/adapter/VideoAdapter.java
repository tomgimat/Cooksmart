package fr.tomgimat.cooksmart.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import fr.tomgimat.cooksmart.R;
import fr.tomgimat.cooksmart.data.firebase.firestore.FirestoreVideo;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    private List<FirestoreVideo> videos;
    private Context context;

    public VideoAdapter(Context context, List<FirestoreVideo> videos) {
        this.context = context;
        this.videos = videos;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        FirestoreVideo video = videos.get(position);
        holder.videoTitle.setText(video.title);

        // Charger la miniature avec Glide
        if (video.thumbnailUrl != null && !video.thumbnailUrl.isEmpty()) {
            Glide.with(context)
                .load(video.thumbnailUrl)
                .centerCrop()
                .into(holder.thumbnail);
        }

        // Gérer le clic sur la vidéo
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(video.videoLink));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }


    @SuppressLint("NotifyDataSetChanged")
    public void setVideos(List<FirestoreVideo> videos) {
        this.videos = videos;
        notifyDataSetChanged();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        ImageView playButton;
        TextView videoTitle;

        VideoViewHolder(View view) {
            super(view);
            thumbnail = view.findViewById(R.id.thumbnail);
            playButton = view.findViewById(R.id.play_button);
            videoTitle = view.findViewById(R.id.video_title);
        }
    }
} 