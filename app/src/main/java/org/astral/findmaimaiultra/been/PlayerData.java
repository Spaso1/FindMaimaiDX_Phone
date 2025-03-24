package org.astral.findmaimaiultra.been;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PlayerData {
    @SerializedName("additional_rating")
    private int additionalRating;
    @SerializedName("charts")
    private Charts charts;
    @SerializedName("nickname")
    private String nickname;
    @SerializedName("plate")
    private String plate;
    @SerializedName("rating")
    private int rating;
    @SerializedName("user_general_data")
    private Object userGeneralData;
    @SerializedName("username")
    private String username;

    // Getters and Setters

    public int getAdditionalRating() {
        return additionalRating;
    }

    public void setAdditionalRating(int additionalRating) {
        this.additionalRating = additionalRating;
    }

    public Charts getCharts() {
        return charts;
    }

    public void setCharts(Charts charts) {
        this.charts = charts;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Object getUserGeneralData() {
        return userGeneralData;
    }

    public void setUserGeneralData(Object userGeneralData) {
        this.userGeneralData = userGeneralData;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
