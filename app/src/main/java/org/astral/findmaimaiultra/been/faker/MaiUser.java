package org.astral.findmaimaiultra.been.faker;

import java.util.List;

public class MaiUser {
    private int userId;
    private int length;
    private int nextIndex;
    private List<UserMusicList> userMusicList;

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getNextIndex() {
        return nextIndex;
    }

    public void setNextIndex(int nextIndex) {
        this.nextIndex = nextIndex;
    }

    public List<UserMusicList> getUserMusicList() {
        return userMusicList;
    }

    public void setUserMusicList(List<UserMusicList> userMusicList) {
        this.userMusicList = userMusicList;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", length=" + length +
                ", nextIndex=" + nextIndex +
                ", userMusicList=" + userMusicList +
                '}';
    }
}
