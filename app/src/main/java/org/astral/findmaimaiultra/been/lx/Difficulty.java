package org.astral.findmaimaiultra.been.lx;

public class Difficulty {
    private String type;
    private int difficulty;
    private String level;
    private double level_value;
    private String note_designer;
    private int version;
    private Notes notes;

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public double getLevel_value() {
        return level_value;
    }

    public void setLevel_value(double level_value) {
        this.level_value = level_value;
    }

    public String getNote_designer() {
        return note_designer;
    }

    public void setNote_designer(String note_designer) {
        this.note_designer = note_designer;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Notes getNotes() {
        return notes;
    }

    public void setNotes(Notes notes) {
        this.notes = notes;
    }
}
