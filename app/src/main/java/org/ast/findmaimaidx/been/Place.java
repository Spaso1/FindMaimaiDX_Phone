package org.ast.findmaimaidx.been;

public class Place {
    int id;
    private String name;
    private String province;
    private String city;
    private String area;
    private String address;
    private int isUse;
    private double x;
    private double y;
    private int count;
    private int good;
    private int bad;
    public Place(int id, String name, String province, String city, String area, String address, int isUse, double x, double y, int count,int good,int bad) {
        this.id = id;
        this.name = name;
        this.province = province;
        this.city = city;
        this.area = area;
        this.address = address;
        this.isUse = isUse;
        this.x = x;
        this.y = y;
        this.count = count;
        this.good = good;
        this.bad = bad;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getGood() {
        return good;
    }

    public void setGood(int good) {
        this.good = good;
    }

    public int getBad() {
        return bad;
    }

    public void setBad(int bad) {
        this.bad = bad;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public int getIsUse() {
        return isUse;
    }

    public void setIsUse(int isUse) {
        this.isUse = isUse;
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
}
