package com.parkourcraft.parkour.data.levels;

public class LevelData {

    private int ID;
    private int reward;
    private int scoreModifier;
    private float rating;
    private int ratingsCount;

    // Object is used to store all the level data from the database
    public LevelData(int ID, int reward, int score_modifier, float rating, int ratingsCount) {
        this.ID = ID;
        this.reward = reward;
        this.scoreModifier = score_modifier;
        this.rating = rating;
        this.ratingsCount = ratingsCount;
    }

    public int getID() {
        return ID;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public int getReward() {
        return reward;
    }

    public void setScoreModifier(int scoreModifier) {
        this.scoreModifier = scoreModifier;
    }

    public int getScoreModifier() {
        return scoreModifier;
    }

    public float getRating() { return rating; }

    public void setRating(float rating) { this.rating = rating; }

    public int getRatingsCount() { return ratingsCount; }

    public void setRatingsCount(int ratingsCount) { this.ratingsCount = ratingsCount; }

}
