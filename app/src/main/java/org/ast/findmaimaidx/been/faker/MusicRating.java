package org.ast.findmaimaidx.been.faker;

public class MusicRating {
    private int musicId;
    private String musicName;

    private int level;
    private double level_info;
    private int romVersion;
    private int achievement;
    private int rating;
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public double getLevel_info() {
        return level_info;
    }

    public void setLevel_info(double level_info) {
        this.level_info = level_info;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    // Getters and Setters
    public int getMusicId() {
        return musicId;
    }

    public void setMusicId(int musicId) {
        this.musicId = musicId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getRomVersion() {
        return romVersion;
    }

    public void setRomVersion(int romVersion) {
        this.romVersion = romVersion;
    }

    public int getAchievement() {
        return achievement;
    }

    public void setAchievement(int achievement) {
        this.achievement = achievement;
    }
}

