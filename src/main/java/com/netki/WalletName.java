package com.netki;

import java.util.*;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Wallet Name data container
 */
public class WalletName extends BaseObject {

    private String id;
    private String domainName;
    private String name;
    private String externalId;
    private Requestor requestor = new Requestor();

    private Map<String, String> wallets = new HashMap<String, String>();

    /**
     * Instantiate an empty WalletName object
     */
    public WalletName() {}

    /**
     * Instantiate an empty WalletName object using a specified {@link Requestor}. Used only in <b>TEST</b>.
     *
     * @param requestor Requestor to use for Netki API interaction
     */
    public WalletName(Requestor requestor)
    {
        if(requestor != null)
        {
            this.requestor = requestor;
        }
    }

    /**
     * Get WalletName used currencies
     *
     * @return List of used currency shortcodes
     */
    public List<String> getUsedCurrencies() {
        String[] currencyArray = this.wallets.keySet().toArray(new String[this.wallets.size()]);
        return Arrays.asList(currencyArray);
    }

    /**
     * Get WalletName wallet address for specified currency shortcode
     *
     * @param currency Currency shortcode (i.e, btc, ltc, nmc, etc)
     * @return Wallet Address
     */
    public String getWalletAddress(String currency) {
        if (this.wallets.containsKey(currency)) {
            return this.wallets.get(currency);
        }
        return null;
    }

    /**
     * Set wallet address for specified currency shortcode
     *
     * @param currency Currency Shortcode
     * @param walletAddress Wallet Address
     */
    public void setCurrencyAddress(String currency, String walletAddress) {
        this.wallets.put(currency, walletAddress);
    }

    /**
     * Remove Wallet Address for specified currency shortcode
     *
     * @param currency Currency Shortcode
     */
    public void removeCurrencyAddress(String currency) {
        if(this.wallets.containsKey(currency)) {
            this.wallets.remove(currency);
        }
    }

    /**
     * Save WalletName to Netki API
     *
     * @throws Exception Occurs on Bad HTTP Request / Response
     */
    public void save() throws Exception {

        Map<String, Object> fullRequest = new HashMap<String, Object> ();
        Map<Object, Object> requestObj = new HashMap<Object, Object> ();

        // Create JSON Request Object
        requestObj.put("name", this.name);
        requestObj.put("domain_name", this.domainName);
        requestObj.put("external_id", this.externalId);

        if (this.id != null) {
            requestObj.put("id", this.id);
        }

        List<Object> walletsList = new ArrayList<Object>();
        for (String currency : this.wallets.keySet())
        {
            Map<String, String> walletPair = new HashMap<String, String>();
            walletPair.put("currency", currency);
            walletPair.put("wallet_address", this.wallets.get(currency));
            walletsList.add(walletPair);
        }

        requestObj.put("wallets", walletsList);

        List<Object> walletNamesObj = new ArrayList<Object>();
        walletNamesObj.add(requestObj);
        fullRequest.put("wallet_names", walletNamesObj);

        ObjectMapper mapper = new ObjectMapper();
        String requestJson;

        try {
            requestJson = mapper.writeValueAsString(fullRequest);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Unable to Build JSON Request");
        }

        if (this.id != null) {
            this.requestor.processRequest(
                    this.getClient(),
                    "/v1/partner/walletname",
                    "PUT",
                    requestJson
            );
        } else {
            String respJsonString = this.requestor.processRequest(
                    this.getClient(),
                    "/v1/partner/walletname",
                    "POST",
                    requestJson
            );

            JsonNode responseNode = mapper.readTree(respJsonString);

            if(responseNode.get("wallet_names") != null && responseNode.get("wallet_names").isArray()) {
                for(JsonNode wnNode : responseNode.get("wallet_names")) {
                    if(wnNode.get("domain_name").asText().equals(this.domainName) && wnNode.get("name").asText().equals(this.name)) {
                        this.id = wnNode.get("id").asText();
                    }
                }
            }
        }
    }

    /**
     * Delete WalletName via Netki API
     *
     * @throws Exception Occurs on Bad HTTP Request / Response
     */
    public void delete() throws Exception {

        if (this.id == null) {
            throw new Exception("Unable to Delete Object that Does Not Exist Remotely");
        }

        this.requestor.processRequest(
                this.getClient(),
                "/v1/partner/walletname/" + this.domainName + "/" + this.id,
                "DELETE",
                null
        );

    }

    /************************
     * Getters and Setters
     */

    /**
     * Get WalletName ID
     *
     * @return WalletName ID
     */
    public String getId() {
        return id;
    }

    /**
     * Set WalletName ID (readonly via API)
     *
     * @param id WalletName ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get WalletName Domain Name
     *
     * @return Domain Name
     */
    public String getDomainName() {
        return domainName;
    }

    /**
     * Set WalletName Domain Name (readonly via API)
     *
     * @param domainName Domain Name
     */
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    /**
     * Get WalletName Name
     *
     * @return Name
     */
    public String getName() {
        return name;
    }

    /**
     * Set WalletName Name
     *
     * @param name Name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get WalletName External ID
     *
     * @return External ID
     */
    public String getExternalId() {
        return externalId;
    }

    /**
     * Set WalletName External ID
     *
     * @param externalId External ID
     */
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    /**
     * Get Requestor used for Netki API interaction
     *
     * @return Requestor
     */
    public Requestor getRequestor() {
        return requestor;
    }

    /**
     * Set Requestor to use for Netki API interaction
     *
     * @param requestor Requestor
     */
    public void setRequestor(Requestor requestor) {
        this.requestor = requestor;
    }

}
