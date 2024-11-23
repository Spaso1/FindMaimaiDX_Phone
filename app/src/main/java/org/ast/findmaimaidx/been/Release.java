package org.ast.findmaimaidx.been;

public class Release {
    private String tagName;
    private String name;
    private String htmlUrl;
    private String body;

    public Release(String tagName, String name, String htmlUrl, String body) {
        this.tagName = tagName;
        this.name = name;
        this.htmlUrl = htmlUrl;
        this.body = body;
    }

    public String getTagName() {
        return tagName;
    }

    public String getName() {
        return name;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public String getBody() {
        return body;
    }
}
