package com.behsa.medportal.security.jwt;


import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class BlacklistedToken {

    @Id
    private String token;

    private Date expiryDate;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
}
