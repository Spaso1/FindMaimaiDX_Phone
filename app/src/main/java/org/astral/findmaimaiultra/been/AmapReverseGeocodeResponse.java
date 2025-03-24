package org.astral.findmaimaiultra.been;

import java.util.List;

public class AmapReverseGeocodeResponse {
    private String status;
    private String info;
    private Regeocode regeocode;

    public String getStatus() {
        return status;
    }

    public Regeocode getRegeocode() {
        return regeocode;
    }

    public static class Regeocode {
        private String formatted_address;
        private AddressComponent addressComponent;

        public String getFormattedAddress() {
            return formatted_address;
        }

        public AddressComponent getAddressComponent() {
            return addressComponent;
        }
    }

    public static class AddressComponent {
        private String province;
        private List<String> city;

        public String getProvince() {
            return province;
        }

        public List<String> getCity() {
            return city;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public void setCity(List<String> city) {
            this.city = city;
        }
    }
}
