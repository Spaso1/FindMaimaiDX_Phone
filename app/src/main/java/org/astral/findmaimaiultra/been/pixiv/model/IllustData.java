package org.astral.findmaimaiultra.been.pixiv.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class IllustData {
    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("illustType")
    public int illustType;

    @SerializedName("xRestrict")
    public int xRestrict;

    @SerializedName("restrict")
    public int restrict;

    @SerializedName("sl")
    public int sl;

    @SerializedName("url")
    public String url;

    @SerializedName("description")
    public String description;

    @SerializedName("tags")
    public List<String> tags;

    @SerializedName("userId")
    public String userId;

    @SerializedName("userName")
    public String userName;

    @SerializedName("width")
    public int width;

    @SerializedName("height")
    public int height;

    @SerializedName("pageCount")
    public int pageCount;

    @SerializedName("isBookmarkable")
    public boolean isBookmarkable;

    @SerializedName("bookmarkData")
    public Object bookmarkData;

    @SerializedName("alt")
    public String alt;

    @SerializedName("titleCaptionTranslation")
    public TitleCaptionTranslation titleCaptionTranslation;

    @SerializedName("createDate")
    public String createDate;

    @SerializedName("updateDate")
    public String updateDate;

    @SerializedName("isUnlisted")
    public boolean isUnlisted;

    @SerializedName("isMasked")
    public boolean isMasked;

    @SerializedName("aiType")
    public int aiType;

    @SerializedName("visibilityScope")
    public int visibilityScope;

    @SerializedName("profileImageUrl")
    public String profileImageUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIllustType() {
        return illustType;
    }

    public void setIllustType(int illustType) {
        this.illustType = illustType;
    }

    public int getxRestrict() {
        return xRestrict;
    }

    public void setxRestrict(int xRestrict) {
        this.xRestrict = xRestrict;
    }

    public int getRestrict() {
        return restrict;
    }

    public void setRestrict(int restrict) {
        this.restrict = restrict;
    }

    public int getSl() {
        return sl;
    }

    public void setSl(int sl) {
        this.sl = sl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public boolean isBookmarkable() {
        return isBookmarkable;
    }

    public void setBookmarkable(boolean bookmarkable) {
        isBookmarkable = bookmarkable;
    }

    public Object getBookmarkData() {
        return bookmarkData;
    }

    public void setBookmarkData(Object bookmarkData) {
        this.bookmarkData = bookmarkData;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public TitleCaptionTranslation getTitleCaptionTranslation() {
        return titleCaptionTranslation;
    }

    public void setTitleCaptionTranslation(TitleCaptionTranslation titleCaptionTranslation) {
        this.titleCaptionTranslation = titleCaptionTranslation;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public boolean isUnlisted() {
        return isUnlisted;
    }

    public void setUnlisted(boolean unlisted) {
        isUnlisted = unlisted;
    }

    public boolean isMasked() {
        return isMasked;
    }

    public void setMasked(boolean masked) {
        isMasked = masked;
    }

    public int getAiType() {
        return aiType;
    }

    public void setAiType(int aiType) {
        this.aiType = aiType;
    }

    public int getVisibilityScope() {
        return visibilityScope;
    }

    public void setVisibilityScope(int visibilityScope) {
        this.visibilityScope = visibilityScope;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}