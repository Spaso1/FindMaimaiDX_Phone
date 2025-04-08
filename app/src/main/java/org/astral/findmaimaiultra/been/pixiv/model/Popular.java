package org.astral.findmaimaiultra.been.pixiv.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Popular {
    @SerializedName("recent")
    public List<Object> recent;

    @SerializedName("permanent")
    public List<Object> permanent;

    public List<Object> getRecent() {
        return recent;
    }

    public void setRecent(List<Object> recent) {
        this.recent = recent;
    }

    public List<Object> getPermanent() {
        return permanent;
    }

    public void setPermanent(List<Object> permanent) {
        this.permanent = permanent;
    }
}
