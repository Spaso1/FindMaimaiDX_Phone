package org.ast.findmaimaidx.been;

public class PlaceContent {
    private int id;
    private String user_name;
    private String user_content;
    private int place_id;
    private boolean used;

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_content() {
        return user_content;
    }

    public void setUser_content(String user_content) {
        this.user_content = user_content;
    }

    public int getPlace_id() {
        return place_id;
    }

    public void setPlace_id(int place_id) {
        this.place_id = place_id;
    }
}
