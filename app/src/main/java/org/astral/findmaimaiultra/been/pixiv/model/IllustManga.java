package org.astral.findmaimaiultra.been.pixiv.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class IllustManga {
    @SerializedName("data")
    public List<IllustData> data;

    @SerializedName("total")
    public int total;

    @SerializedName("lastPage")
    public int lastPage;

    @SerializedName("bookmarkRanges")
    public List<BookmarkRange> bookmarkRanges;

    public List<IllustData> getData() {
        return data;
    }

    public void setData(List<IllustData> data) {
        this.data = data;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getLastPage() {
        return lastPage;
    }

    public void setLastPage(int lastPage) {
        this.lastPage = lastPage;
    }

    public List<BookmarkRange> getBookmarkRanges() {
        return bookmarkRanges;
    }

    public void setBookmarkRanges(List<BookmarkRange> bookmarkRanges) {
        this.bookmarkRanges = bookmarkRanges;
    }
}