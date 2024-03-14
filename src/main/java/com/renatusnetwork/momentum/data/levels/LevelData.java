package com.renatusnetwork.momentum.data.levels;

public class LevelData {

    private int ID;
    private int reward;
    private int scoreModifier;

    // Object is used to store all the level data from the database
    public LevelData(int ID, int reward, int score_modifier) {
        this.ID = ID;
        this.reward = reward;
        this.scoreModifier = score_modifier;
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

}
