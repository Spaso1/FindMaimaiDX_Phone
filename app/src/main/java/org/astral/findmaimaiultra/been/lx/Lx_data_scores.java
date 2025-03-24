package org.astral.findmaimaiultra.been.lx;

import com.google.gson.annotations.SerializedName;

public class Lx_data_scores {
    @SerializedName("success")
    private boolean success;

    @SerializedName("code")
    private int code;

    @SerializedName("data")
    private Lx_chart[] data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Lx_chart[] getData() {
        return data;
    }

    public void setData(Lx_chart[] data) {
        this.data = data;
    }
}
