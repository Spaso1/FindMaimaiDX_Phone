package org.astral.findmaimaiultra.been;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

public class Place implements Parcelable {
    private int id;
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
    public int num;
    public int numJ;
    private String meituan_link;
    private String douyin_link;

    public String getMeituan_link() {
        return meituan_link;
    }

    public void setMeituan_link(String meituan_link) {
        this.meituan_link = meituan_link;
    }

    public String getDouyin_link() {
        return douyin_link;
    }

    public void setDouyin_link(String douyin_link) {
        this.douyin_link = douyin_link;
    }

    public int getNumJ() {
        return numJ;
    }

    public void setNumJ(int numJ) {
        this.numJ = numJ;
    }

    public Place(int id, String name, String province, String city, String area, String address, int isUse, double x, double y, int count, int good, int bad) {
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

    protected Place(Parcel in) {
        id = in.readInt();
        name = in.readString();
        province = in.readString();
        city = in.readString();
        area = in.readString();
        address = in.readString();
        isUse = in.readInt();
        x = in.readDouble();
        y = in.readDouble();
        count = in.readInt();
        good = in.readInt();
        bad = in.readInt();
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(province);
        dest.writeString(city);
        dest.writeString(area);
        dest.writeString(address);
        dest.writeInt(isUse);
        dest.writeDouble(x);
        dest.writeDouble(y);
        dest.writeInt(count);
        dest.writeInt(good);
        dest.writeInt(bad);
    }
}
