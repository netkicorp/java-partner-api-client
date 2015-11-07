package com.netki;

/**
 * Superclass used for all Netki data containers
 */
public abstract class BaseObject {

    protected String apiUrl;
    protected String apiKey;
    protected String partnerId;

    /**
     *  Set Data API Information for CRUD operations
     *
     * @param apiUrl Netki Partner API URL Base (i.e., https://api.netki.com)
     * @param apiKey Netki Partner API Key
     * @param partnerId Netki Partner ID
     */
    public void setApiOpts(String apiUrl, String apiKey, String partnerId) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.partnerId = partnerId;
    }

    /**
     * Get Netki Partner API Url Base
     *
     * @return Netki Partner API Url Base
     */
    public String getApiUrl() {
        return this.apiUrl;
    }

    /**
     * Get Netki Partner API Key
     *
     * @return Netki Partner API Key
     */
    public String getApiKey() {
        return this.apiKey;
    }

    /**
     * Get Netki Partner ID
     *
     * @return Netki Partner ID
     */
    public String getPartnerId() {
        return this.partnerId;
    }

}