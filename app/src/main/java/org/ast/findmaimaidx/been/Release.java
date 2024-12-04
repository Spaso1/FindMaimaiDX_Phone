package org.ast.findmaimaidx.been;

import com.google.gson.annotations.SerializedName;

public class Release {
    @SerializedName("tag_name")
    private String tagName;

    @SerializedName("name")
    private String name;

    @SerializedName("body")
    private String body;

    @SerializedName("html_url")
    private String htmlUrl;

    // Getters and setters
    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }
}
