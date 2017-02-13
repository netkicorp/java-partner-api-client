package com.netki;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomerData {

    // Name
    private String firstName;
    private String lastName;
    private String middleName;

    // Address
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    // Other
    private String phone;
    private String email;
    private String organizationName;
    private String ssn;
    private Date dob;

    private List<IdentityDocument> identityDocuments = new ArrayList<IdentityDocument>(2);

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public List<IdentityDocument> getIdentityDocuments() {
        return identityDocuments;
    }

    public void setIdentityDocuments(List<IdentityDocument> docs) {
        this.identityDocuments = docs;
    }

    public void addIdentity(IdentityDocument doc) {
        identityDocuments.add(doc);
    }
}
