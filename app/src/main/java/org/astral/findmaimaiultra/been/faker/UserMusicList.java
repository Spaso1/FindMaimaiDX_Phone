package org.astral.findmaimaiultra.been.faker;

import java.util.List;

public class UserMusicList {
    private List<MusicRating> userMusicDetailList;
    private int length;

    // Getters and Setters
    public List<MusicRating> getUserMusicDetailList() {
        return userMusicDetailList;
    }

    public void setUserMusicDetailList(List<MusicRating> userMusicDetailList) {
        this.userMusicDetailList = userMusicDetailList;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return "UserMusicList{" +
                "userMusicDetailList=" + userMusicDetailList +
                ", length=" + length +
                '}';
    }
}
