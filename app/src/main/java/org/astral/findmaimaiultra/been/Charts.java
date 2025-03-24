package org.astral.findmaimaiultra.been;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Charts {
    @SerializedName("dx")
    private List<Chart> dx;
    @SerializedName("sd")
    private List<Chart> sd;

    // Getters and Setters

    public List<Chart> getDx() {
        return dx;
    }

    public void setDx(List<Chart> dx) {
        this.dx = dx;
    }

    public List<Chart> getSd() {
        return sd;
    }

    public void setSd(List<Chart> sd) {
        this.sd = sd;
    }
}
