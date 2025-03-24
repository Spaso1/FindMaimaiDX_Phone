package org.astral.findmaimaiultra.been.lx;

import com.google.gson.annotations.SerializedName;

public class Lx_res {
    @SerializedName("success")
    private boolean success;

    @SerializedName("code")
    private int code;

    @SerializedName("data")
    private Lx_data data;

    // Getters and Setters
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

    public Lx_data getData() {
        return data;
    }

    public void setData(Lx_data data) {
        this.data = data;
    }
}
