package com.netki;

/**
 * Superclass used for all Netki data containers
 */
public abstract class BaseObject {

    protected NetkiClient nkClient;

    /**
     * Set Object's Associated NetkiClient
     * @param nkClient NetkiClient
     */
    public void setNkClient(NetkiClient nkClient) {
        this.nkClient = nkClient;
    }

    /**
     * Get Object's Associated NetkiClient
     * @return NetkiClient
     */
    public NetkiClient getNkClient() {
        return nkClient;
    }

}