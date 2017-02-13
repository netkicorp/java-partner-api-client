package com.netki;

/**
 * Superclass used for all Netki data containers
 */
abstract class BaseObject {

    protected NetkiClient client;

    /**
     * Set Object's Associated NetkiClient
     * @param client NetkiClient
     */
    void setClient(NetkiClient client) {
        this.client = client;
    }

    /**
     * Get Object's Associated NetkiClient
     * @return NetkiClient
     */
    NetkiClient getClient() {
        return client;
    }

}