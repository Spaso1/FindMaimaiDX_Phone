package org.astral.findmaimaiultra.ui.login;

public class LoginRequest {
    private String email;
    private String codeOrPassword;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodeOrPassword() {
        return codeOrPassword;
    }

    public void setCodeOrPassword(String codeOrPassword) {
        this.codeOrPassword = codeOrPassword;
    }
}
