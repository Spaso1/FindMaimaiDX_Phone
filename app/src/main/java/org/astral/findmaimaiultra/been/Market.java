package org.astral.findmaimaiultra.been;

public class Market {
    private int id;
    private String marketName;
    private double distance;
    private int parentId;
    private double x;
    private double y;
    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\":" + id +
                ", \"marketName\":\"" + marketName + '\"' +
                ", \"distance=\":" + distance +
                ", \"parentId\":" + parentId +
                ", \"x\":" + x +
                ", \"y\":" + y +
                ", \"type\":" + type +
                '}';
    }
}
