package org.astral.findmaimaiultra.been.pixiv.model.pages;

import java.util.Map;

public class Meta {
    private String title;
    private String description;
    private String canonical;
    private Map<String, String> alternateLanguages;
    private String descriptionHeader;
    private Ogp ogp;
    private Twitter twitter;

    // Getters and Setters
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

    public String getCanonical() {
        return canonical;
    }

    public void setCanonical(String canonical) {
        this.canonical = canonical;
    }

    public Map<String, String> getAlternateLanguages() {
        return alternateLanguages;
    }

    public void setAlternateLanguages(Map<String, String> alternateLanguages) {
        this.alternateLanguages = alternateLanguages;
    }

    public String getDescriptionHeader() {
        return descriptionHeader;
    }

    public void setDescriptionHeader(String descriptionHeader) {
        this.descriptionHeader = descriptionHeader;
    }

    public Ogp getOgp() {
        return ogp;
    }

    public void setOgp(Ogp ogp) {
        this.ogp = ogp;
    }

    public Twitter getTwitter() {
        return twitter;
    }

    public void setTwitter(Twitter twitter) {
        this.twitter = twitter;
    }
}
