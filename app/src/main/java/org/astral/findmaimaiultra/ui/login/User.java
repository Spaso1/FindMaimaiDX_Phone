package org.astral.findmaimaiultra.ui.login;

public class User {
    private String id;
    private String name;
    private String email;
    private String twoFactor;
    private String avatar;
    private String lastLogin;
    private String qqId;
    private String mai_avatarId;
    private String mai_userName;

    public String getQqId() {
        return qqId;
    }

    public void setQqId(String qqId) {
        this.qqId = qqId;
    }

    public String getMai_avatarId() {
        return mai_avatarId;
    }

    public void setMai_avatarId(String mai_avatarId) {
        this.mai_avatarId = mai_avatarId;
    }

    public String getMai_userName() {
        return mai_userName;
    }

    public void setMai_userName(String mai_userName) {
        this.mai_userName = mai_userName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTwoFactor() {
        return twoFactor;
    }

    public void setTwoFactor(String twoFactor) {
        this.twoFactor = twoFactor;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }
}