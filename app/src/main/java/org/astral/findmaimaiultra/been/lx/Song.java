package org.astral.findmaimaiultra.been.lx;


import java.util.Map;

public class Song {
    private int id;
    private String title;
    private String artist;
    private String genre;
    private int bpm;
    private int version;
    private String rights;
    private Map<String, Difficulty[]> difficulties;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getBpm() {
        return bpm;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getRights() {
        return rights;
    }

    public void setRights(String rights) {
        this.rights = rights;
    }

    public Map<String, Difficulty[]> getDifficulties() {
        return difficulties;
    }

    public void setDifficulties(Map<String, Difficulty[]> difficulties) {
        this.difficulties = difficulties;
    }
}
