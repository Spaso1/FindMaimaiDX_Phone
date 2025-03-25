package org.astral.findmaimaiultra.been.faker;

public class MusicRating {
    private int musicId;
    private String musicName;
    private int level;
    private double level_info;
    private int romVersion;
    private int achievement;
    private int rating;
    private String type;
    private int playCount;
    private int comboStatus;
    private int syncStatus;
    private int deluxscoreMax;
    private int scoreRank;
    private int extNum1;
    private int extNum2;

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public int getComboStatus() {
        return comboStatus;
    }

    public void setComboStatus(int comboStatus) {
        this.comboStatus = comboStatus;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    public int getDeluxscoreMax() {
        return deluxscoreMax;
    }

    public void setDeluxscoreMax(int deluxscoreMax) {
        this.deluxscoreMax = deluxscoreMax;
    }

    public int getScoreRank() {
        return scoreRank;
    }

    public void setScoreRank(int scoreRank) {
        this.scoreRank = scoreRank;
    }

    public int getExtNum1() {
        return extNum1;
    }

    public void setExtNum1(int extNum1) {
        this.extNum1 = extNum1;
    }

    public int getExtNum2() {
        return extNum2;
    }

    public void setExtNum2(int extNum2) {
        this.extNum2 = extNum2;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    @Override
    public String toString() {
        return "MusicRating{" +
                "musicId=" + musicId +
                ", musicName='" + musicName + '\'' +
                ", level=" + level +
                ", level_info=" + level_info +
                ", romVersion=" + romVersion +
                ", achievement=" + achievement +
                ", rating=" + rating +
                ", type='" + type + '\'' +
                ", playCount=" + playCount +
                ", comboStatus=" + comboStatus +
                ", syncStatus=" + syncStatus +
                ", deluxscoreMax=" + deluxscoreMax +
                ", scoreRank=" + scoreRank +
                ", extNum1=" + extNum1 +
                ", extNum2=" + extNum2 +
                '}';
    }

    public void setAchievement(int achievement) {
        this.achievement = achievement;
    }
}

