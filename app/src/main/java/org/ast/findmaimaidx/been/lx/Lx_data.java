package org.ast.findmaimaidx.been.lx;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Lx_data {
    @SerializedName("standard_total")
    private int standardTotal;

    @SerializedName("dx_total")
    private int dxTotal;

    @SerializedName("standard")
    private List<Lx_chart> standard;

    @SerializedName("dx")
    private List<Lx_chart> dx;

    // Getters and Setters
    public int getStandardTotal() {
        return standardTotal;
    }

    public void setStandardTotal(int standardTotal) {
        this.standardTotal = standardTotal;
    }

    public int getDxTotal() {
        return dxTotal;
    }

    public void setDxTotal(int dxTotal) {
        this.dxTotal = dxTotal;
    }

    public List<Lx_chart> getStandard() {
        return standard;
    }

    public void setStandard(List<Lx_chart> standard) {
        this.standard = standard;
    }

    public List<Lx_chart> getDx() {
        return dx;
    }

    public void setDx(List<Lx_chart> dx) {
        this.dx = dx;
    }
}
