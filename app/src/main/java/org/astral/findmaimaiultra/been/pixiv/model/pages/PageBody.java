package org.astral.findmaimaiultra.been.pixiv.model.pages;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class PageBody {
    private String illustId;
    private String illustTitle;
    private String illustComment;
    private String id;
    private String title;
    private String description;
    private int illustType;
    private Date createDate;
    private Date uploadDate;
    private int restrict;
    private int xRestrict;
    private int sl;
    private Urls urls;
    private Tags tags;
    private String alt;
    private String userId;
    private String userName;
    private String userAccount;
    private Map<String, UserIllust> userIllusts;
    private boolean likeData;
    private int width;
    private int height;
    private int pageCount;
    private int bookmarkCount;
    private int likeCount;
    private int commentCount;
    private int responseCount;
    private int viewCount;
    private String bookStyle;
    private boolean isHowto;
    private boolean isOriginal;
    private List<?> imageResponseOutData;
    private List<?> imageResponseData;
    private int imageResponseCount;
    private Object pollData;
    private Object seriesNavData;
    private Object descriptionBoothId;
    private Object descriptionYoutubeId;
    private Object comicPromotion;
    private Object fanboxPromotion;
    private List<?> contestBanners;
    private boolean isBookmarkable;
    private Object bookmarkData;
    private Object contestData;
    private ZoneConfig zoneConfig;
    private TitleCaptionTranslation titleCaptionTranslation;
    private boolean isUnlisted;
    private Object request;
    private int commentOff;
    private int aiType;
    private Object reuploadDate;
    private boolean locationMask;
    private boolean commissionLinkHidden;
    private boolean isLoginOnly;

    // Getters and Setters
    // (Add getters and setters for all fields)


    public String getIllustId() {
        return illustId;
    }

    public void setIllustId(String illustId) {
        this.illustId = illustId;
    }

    public String getIllustTitle() {
        return illustTitle;
    }

    public void setIllustTitle(String illustTitle) {
        this.illustTitle = illustTitle;
    }

    public String getIllustComment() {
        return illustComment;
    }

    public void setIllustComment(String illustComment) {
        this.illustComment = illustComment;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIllustType() {
        return illustType;
    }

    public void setIllustType(int illustType) {
        this.illustType = illustType;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public int getRestrict() {
        return restrict;
    }

    public void setRestrict(int restrict) {
        this.restrict = restrict;
    }

    public int getxRestrict() {
        return xRestrict;
    }

    public void setxRestrict(int xRestrict) {
        this.xRestrict = xRestrict;
    }

    public int getSl() {
        return sl;
    }

    public void setSl(int sl) {
        this.sl = sl;
    }

    public Urls getUrls() {
        return urls;
    }

    public void setUrls(Urls urls) {
        this.urls = urls;
    }

    public Tags getTags() {
        return tags;
    }

    public void setTags(Tags tags) {
        this.tags = tags;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
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

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public Map<String, UserIllust> getUserIllusts() {
        return userIllusts;
    }

    public void setUserIllusts(Map<String, UserIllust> userIllusts) {
        this.userIllusts = userIllusts;
    }

    public boolean isLikeData() {
        return likeData;
    }

    public void setLikeData(boolean likeData) {
        this.likeData = likeData;
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

    public int getBookmarkCount() {
        return bookmarkCount;
    }

    public void setBookmarkCount(int bookmarkCount) {
        this.bookmarkCount = bookmarkCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getResponseCount() {
        return responseCount;
    }

    public void setResponseCount(int responseCount) {
        this.responseCount = responseCount;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public String getBookStyle() {
        return bookStyle;
    }

    public void setBookStyle(String bookStyle) {
        this.bookStyle = bookStyle;
    }

    public boolean isHowto() {
        return isHowto;
    }

    public void setHowto(boolean howto) {
        isHowto = howto;
    }

    public boolean isOriginal() {
        return isOriginal;
    }

    public void setOriginal(boolean original) {
        isOriginal = original;
    }

    public List<?> getImageResponseOutData() {
        return imageResponseOutData;
    }

    public void setImageResponseOutData(List<?> imageResponseOutData) {
        this.imageResponseOutData = imageResponseOutData;
    }

    public List<?> getImageResponseData() {
        return imageResponseData;
    }

    public void setImageResponseData(List<?> imageResponseData) {
        this.imageResponseData = imageResponseData;
    }

    public int getImageResponseCount() {
        return imageResponseCount;
    }

    public void setImageResponseCount(int imageResponseCount) {
        this.imageResponseCount = imageResponseCount;
    }

    public Object getPollData() {
        return pollData;
    }

    public void setPollData(Object pollData) {
        this.pollData = pollData;
    }

    public Object getSeriesNavData() {
        return seriesNavData;
    }

    public void setSeriesNavData(Object seriesNavData) {
        this.seriesNavData = seriesNavData;
    }

    public Object getDescriptionBoothId() {
        return descriptionBoothId;
    }

    public void setDescriptionBoothId(Object descriptionBoothId) {
        this.descriptionBoothId = descriptionBoothId;
    }

    public Object getDescriptionYoutubeId() {
        return descriptionYoutubeId;
    }

    public void setDescriptionYoutubeId(Object descriptionYoutubeId) {
        this.descriptionYoutubeId = descriptionYoutubeId;
    }

    public Object getComicPromotion() {
        return comicPromotion;
    }

    public void setComicPromotion(Object comicPromotion) {
        this.comicPromotion = comicPromotion;
    }

    public Object getFanboxPromotion() {
        return fanboxPromotion;
    }

    public void setFanboxPromotion(Object fanboxPromotion) {
        this.fanboxPromotion = fanboxPromotion;
    }

    public List<?> getContestBanners() {
        return contestBanners;
    }

    public void setContestBanners(List<?> contestBanners) {
        this.contestBanners = contestBanners;
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

    public Object getContestData() {
        return contestData;
    }

    public void setContestData(Object contestData) {
        this.contestData = contestData;
    }

    public ZoneConfig getZoneConfig() {
        return zoneConfig;
    }

    public void setZoneConfig(ZoneConfig zoneConfig) {
        this.zoneConfig = zoneConfig;
    }

    public TitleCaptionTranslation getTitleCaptionTranslation() {
        return titleCaptionTranslation;
    }

    public void setTitleCaptionTranslation(TitleCaptionTranslation titleCaptionTranslation) {
        this.titleCaptionTranslation = titleCaptionTranslation;
    }

    public boolean isUnlisted() {
        return isUnlisted;
    }

    public void setUnlisted(boolean unlisted) {
        isUnlisted = unlisted;
    }

    public Object getRequest() {
        return request;
    }

    public void setRequest(Object request) {
        this.request = request;
    }

    public int getCommentOff() {
        return commentOff;
    }

    public void setCommentOff(int commentOff) {
        this.commentOff = commentOff;
    }

    public int getAiType() {
        return aiType;
    }

    public void setAiType(int aiType) {
        this.aiType = aiType;
    }

    public Object getReuploadDate() {
        return reuploadDate;
    }

    public void setReuploadDate(Object reuploadDate) {
        this.reuploadDate = reuploadDate;
    }

    public boolean isLocationMask() {
        return locationMask;
    }

    public void setLocationMask(boolean locationMask) {
        this.locationMask = locationMask;
    }

    public boolean isCommissionLinkHidden() {
        return commissionLinkHidden;
    }

    public void setCommissionLinkHidden(boolean commissionLinkHidden) {
        this.commissionLinkHidden = commissionLinkHidden;
    }

    public boolean isLoginOnly() {
        return isLoginOnly;
    }

    public void setLoginOnly(boolean loginOnly) {
        isLoginOnly = loginOnly;
    }
}
