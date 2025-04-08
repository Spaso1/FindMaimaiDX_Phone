package org.astral.findmaimaiultra.been.pixiv.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class TagTranslation {
    @SerializedName("tagTranslation")
    public Map<String, Translation> tagTranslation;

    public static class Translation {
        @SerializedName("zh")
        public String zh;
    }
}
