package org.astral.findmaimaiultra.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.astral.findmaimaiultra.R;
import org.astral.findmaimaiultra.been.lx.Song;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private List<Song> songs;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Song song);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public SongAdapter(List<Song> songs) {
        this.songs = songs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.title.setText(song.getTitle());
        holder.artist.setText(song.getArtist());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(song);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView artist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.song_title);
            artist = itemView.findViewById(R.id.song_artist);
        }
    }
}