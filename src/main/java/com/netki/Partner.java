package com.netki;

public class Partner extends BaseObject {

    private String id;
    private String name;
    private Requestor requestor;

    /**
     * Instantiate a Partner object
     */
    public Partner()
    {
        this(new Requestor());
    }

    /**
     * Instantiate a Partner object with a defined {@link Requestor}. This is used only in <b>TEST</b>.
     * @param requestor Requestor to use for API operations
     */
    public Partner(Requestor requestor)
    {
        this.requestor = requestor;
    }

    /**
     * Instantiate a Partner object with ID and Name
     *
     * @param id Netki Internal Partner ID
     * @param name Partner Name
     */
    public Partner(String id, String name) {
        this.id = id;
        this.name = name;
        this.requestor = new Requestor();
    }

    /**
     * Delete partner via Netki API
     *
     * @throws Exception Occurs on Bad HTTP Request / Response
     */
    public void delete() throws Exception {
        this.requestor.processRequest(
                this.apiKey,
                this.partnerId,
                this.apiUrl + "/v1/admin/partner/" + this.name,
                "DELETE",
                null
        );
    }

    /****************************
     * Getters and Setters
     */

    /**
     * Get Partner ID
     *
     * @return Partner ID
     */
    public String getId() {
        return id;
    }

    /**
     * Set Partner ID (readonly via Netki API)
     *
     * @param id Partner ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get Partner Name
     *
     * @return Partner Name
     */
    public String getName() {
        return name;
    }

    /**
     * Set Partner Name (readonly via Netki API)
     *
     * @param name Partner Name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get Requestor
     *
     * @return Requestor used for API operations
     */
    public Requestor getRequestor() {
        return requestor;
    }

    /**
     * Set Requestor
     *
     * @param requestor Requestor to use for API operations
     */
    public void setRequestor(Requestor requestor) {
        this.requestor = requestor;
    }

}
