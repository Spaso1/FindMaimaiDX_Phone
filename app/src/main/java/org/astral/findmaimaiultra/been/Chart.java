package org.astral.findmaimaiultra.been;

import com.google.gson.annotations.SerializedName;

public class Chart {
    @SerializedName("achievements")
    private double achievements;
    @SerializedName("ds")
    private double ds;
    @SerializedName("dxScore")
    private int dxScore;
    @SerializedName("fc")
    private String fc;
    @SerializedName("fs")
    private String fs;
    @SerializedName("level")
    private String level;
    @SerializedName("level_index")
    private int level_index;
    @SerializedName("level_label")
    private String levelLabel;
    @SerializedName("ra")
    private int ra;
    @SerializedName("rate")
    private String rate;
    @SerializedName("song_id")
    private int songId;
    @SerializedName("title")
    private String title;
    @SerializedName("type")
    private String type;

    public double getAchievements() {
        return achievements;
    }

    public void setAchievements(double achievements) {
        this.achievements = achievements;
    }

    public double getDs() {
        return ds;
    }

    public void setDs(double ds) {
        this.ds = ds;
    }

    public int getDxScore() {
        return dxScore;
    }

    public void setDxScore(int dxScore) {
        this.dxScore = dxScore;
    }

    public String getFc() {
        return fc;
    }

    public void setFc(String fc) {
        this.fc = fc;
    }

    public String getFs() {
        return fs;
    }

    public void setFs(String fs) {
        this.fs = fs;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getLevel_index() {
        return level_index;
    }

    public void setLevel_index(int level_index) {
        this.level_index = level_index;
    }

    public String getLevelLabel() {
        return levelLabel;
    }

    public void setLevelLabel(String levelLabel) {
        this.levelLabel = levelLabel;
    }

    public int getRa() {
        return ra;
    }

    public void setRa(int ra) {
        this.ra = ra;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // Getters and Setters
}
