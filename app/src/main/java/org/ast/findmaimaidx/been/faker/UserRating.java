package org.ast.findmaimaidx.been.faker;

import java.util.List;

public class UserRating {
    private int rating;
    private List<MusicRating> ratingList;
    private List<MusicRating> newRatingList;
    private List<MusicRating> nextRatingList;
    private List<MusicRating> nextNewRatingList;
    private Udemae udemae;

    // Getters and Setters
    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public List<MusicRating> getRatingList() {
        return ratingList;
    }

    public void setRatingList(List<MusicRating> ratingList) {
        this.ratingList = ratingList;
    }

    public List<MusicRating> getNewRatingList() {
        return newRatingList;
    }

    public void setNewRatingList(List<MusicRating> newRatingList) {
        this.newRatingList = newRatingList;
    }

    public List<MusicRating> getNextRatingList() {
        return nextRatingList;
    }

    public void setNextRatingList(List<MusicRating> nextRatingList) {
        this.nextRatingList = nextRatingList;
    }

    public List<MusicRating> getNextNewRatingList() {
        return nextNewRatingList;
    }

    public void setNextNewRatingList(List<MusicRating> nextNewRatingList) {
        this.nextNewRatingList = nextNewRatingList;
    }

    public Udemae getUdemae() {
        return udemae;
    }

    public void setUdemae(Udemae udemae) {
        this.udemae = udemae;
    }
}