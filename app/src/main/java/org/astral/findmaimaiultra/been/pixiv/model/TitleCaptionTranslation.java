package org.astral.findmaimaiultra.been.pixiv.model;

import com.google.gson.annotations.SerializedName;

public class TitleCaptionTranslation {
    @SerializedName("workTitle")
    public String workTitle;

    @SerializedName("workCaption")
    public String workCaption;

    public String getWorkTitle() {
        return workTitle;
    }

    public void setWorkTitle(String workTitle) {
        this.workTitle = workTitle;
    }

    public String getWorkCaption() {
        return workCaption;
    }

    public void setWorkCaption(String workCaption) {
        this.workCaption = workCaption;
    }
}