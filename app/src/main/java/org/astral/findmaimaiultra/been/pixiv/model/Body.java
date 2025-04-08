package org.astral.findmaimaiultra.been.pixiv.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Body {
    @SerializedName("illustManga")
    public IllustManga illustManga;

    @SerializedName("popular")
    public Popular popular;
    @SerializedName("relatedTags")
    public List<String> relatedTags;

    @SerializedName("tagTranslation")
    public TagTranslation tagTranslation;

    @SerializedName("zoneConfig")
    public ZoneConfig zoneConfig;

    @SerializedName("extraData")
    public ExtraData extraData;

    public List<String> getRelatedTags() {
        return relatedTags;
    }

    public void setRelatedTags(List<String> relatedTags) {
        this.relatedTags = relatedTags;
    }

    public TagTranslation getTagTranslation() {
        return tagTranslation;
    }

    public void setTagTranslation(TagTranslation tagTranslation) {
        this.tagTranslation = tagTranslation;
    }

    public ZoneConfig getZoneConfig() {
        return zoneConfig;
    }

    public void setZoneConfig(ZoneConfig zoneConfig) {
        this.zoneConfig = zoneConfig;
    }

    public ExtraData getExtraData() {
        return extraData;
    }

    public void setExtraData(ExtraData extraData) {
        this.extraData = extraData;
    }

    public IllustManga getIllustManga() {
        return illustManga;
    }

    public void setIllustManga(IllustManga illustManga) {
        this.illustManga = illustManga;
    }

    public Popular getPopular() {
        return popular;
    }

    public void setPopular(Popular popular) {
        this.popular = popular;
    }
}
