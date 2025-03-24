package org.astral.findmaimaiultra.been;

import android.os.Parcel;
import android.os.Parcelable;

public class Geocode implements Parcelable {
    private String formatted_address;
    private String country;
    private String province;
    private String citycode;
    private String city;
    private String district;
    private String location;
    private String level;

    public Geocode() {}

    // 必须实现的构造函数，用于从Parcel中读取数据
    protected Geocode(Parcel in) {
        formatted_address = in.readString();
        country = in.readString();
        province = in.readString();
        citycode = in.readString();
        city = in.readString();
        district = in.readString();
        location = in.readString();
        level = in.readString();
    }

    // 必须实现的方法，用于将对象写入Parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(formatted_address);
        dest.writeString(country);
        dest.writeString(province);
        dest.writeString(citycode);
        dest.writeString(city);
        dest.writeString(district);
        dest.writeString(location);
        dest.writeString(level);
    }

    // 必须实现的方法，返回一个CREATOR对象
    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Geocode> CREATOR = new Creator<Geocode>() {
        @Override
        public Geocode createFromParcel(Parcel in) {
            return new Geocode(in);
        }

        @Override
        public Geocode[] newArray(int size) {
            return new Geocode[size];
        }
    };

    // Getter and Setter methods
    public void setFormatted_address(String formatted_address) {
        this.formatted_address = formatted_address;
    }

    public String getFormatted_address() {
        return formatted_address;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getProvince() {
        return province;
    }

    public void setCitycode(String citycode) {
        this.citycode = citycode;
    }

    public String getCitycode() {
        return citycode;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getDistrict() {
        return district;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLevel() {
        return level;
    }
}
