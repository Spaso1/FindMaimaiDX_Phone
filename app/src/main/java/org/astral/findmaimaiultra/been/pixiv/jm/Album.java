package org.astral.findmaimaiultra.been.pixiv.jm;

import java.io.Serializable;
import java.util.List;

public class Album  {
    private String album_id;
    private String scramble_id;
    private String name;
    private int page_count;
    private String pub_date;
    private String update_date;
    private int likes;
    private int views;
    private int comment_count;
    private List<String> works;
    private List<String> actors;
    private List<String> authors;
    private List<String> tags;
    private List<String> image_urls;
    private List<RelatedItem> related_list;
    private List<Integer> nums;

    public List<Integer> getNums() {
        return nums;
    }

    public void setNums(List<Integer> nums) {
        this.nums = nums;
    }

    // Getters and Setters
    public String getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(String album_id) {
        this.album_id = album_id;
    }

    public String getScramble_id() {
        return scramble_id;
    }

    public void setScramble_id(String scramble_id) {
        this.scramble_id = scramble_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPage_count() {
        return page_count;
    }

    public void setPage_count(int page_count) {
        this.page_count = page_count;
    }

    public String getPub_date() {
        return pub_date;
    }

    public void setPub_date(String pub_date) {
        this.pub_date = pub_date;
    }

    public String getUpdate_date() {
        return update_date;
    }

    public void setUpdate_date(String update_date) {
        this.update_date = update_date;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getComment_count() {
        return comment_count;
    }

    public void setComment_count(int comment_count) {
        this.comment_count = comment_count;
    }

    public List<String> getWorks() {
        return works;
    }

    public void setWorks(List<String> works) {
        this.works = works;
    }

    public List<String> getActors() {
        return actors;
    }

    public void setActors(List<String> actors) {
        this.actors = actors;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getImage_urls() {
        return image_urls;
    }

    public void setImage_urls(List<String> image_urls) {
        this.image_urls = image_urls;
    }

    public List<RelatedItem> getRelated_list() {
        return related_list;
    }

    public void setRelated_list(List<RelatedItem> related_list) {
        this.related_list = related_list;
    }
}

