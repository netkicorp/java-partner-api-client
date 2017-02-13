package com.netki;

import java.util.Date;

public class IdentityDocument {

    private String identity;
    private String type;
    private String state;
    private String gender;
    private String dlRtaNumber;
    private Date expiration;

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDlRtaNumber() {
        return dlRtaNumber;
    }

    public void setDlRtaNumber(String dlRtaNumber) {
        this.dlRtaNumber = dlRtaNumber;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

}
