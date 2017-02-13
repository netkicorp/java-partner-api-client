package com.netki;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Netki Partner Domain Object
 */
public class Domain extends BaseObject {

    private String name;
    private String status;
    private boolean delegationStatus;
    private String delegationMessage;
    private int walletNameCount;

    private Date nextRoll;
    private List<String> dsRecords;
    private List<String> nameservers;
    private String publicKeySigningKey;

    private Requestor requestor = new Requestor();
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Instantiate Domain Object with name
     *
     * @param name Domain Name
     */
    public Domain(String name) {
        this(name, new Requestor());
    }

    /**
     * Instantiate Domain Object with name and {@link Requestor}. Used for testing <b>ONLY</b>
     *
     * @param name Domain Name
     * @param requestor Requestor Object
     */
    public Domain(String name, Requestor requestor)
    {
        this.name = name;
        this.dsRecords = new ArrayList<String>();
        this.nameservers = new ArrayList<String>();

        if (requestor != null)
        {
            this.requestor = requestor;
        }
    }

    /**
     * Call Domain Deletion on Netki API
     *
     * @throws Exception Occurs on Bad HTTP Request / Response
     */
    public void delete() throws Exception {
        this.requestor.processRequest(
                this.getClient(),
                "/v1/partner/domain/" + this.name,
                "DELETE",
                null
        );
    }

    /**
     * Get Domain Status from Netki API
     *
     * @throws Exception Occurs on Bad HTTP Request / Response
     */
    public void loadStatus() throws Exception {
        String responseStr = this.requestor.processRequest(
                this.getClient(),
                "/v1/partner/domain/" + this.name,
                "GET",
                null
        );

        JsonNode jsonObj;
        try {
            jsonObj = this.mapper.readTree(responseStr);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        this.status = jsonObj.get("status").asText();
        this.delegationStatus = jsonObj.get("delegation_status").asBoolean();
        this.delegationMessage = jsonObj.get("delegation_message").asText();
        this.walletNameCount = jsonObj.get("wallet_name_count").asInt();
    }

    /**
     * Get Domain DNSSEC Status from Netki API
     *
     * @throws Exception Occurs on Bad HTTP Request / Response
     */
    public void loadDnssecDetails() throws Exception {
        String responseStr = this.requestor.processRequest(
                this.getClient(),
                "/v1/partner/domain/dnssec/" + this.name,
                "GET",
                null
        );

        JsonNode jsonObj;
        try {
            jsonObj = this.mapper.readTree(responseStr);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if(jsonObj.get("public_key_signing_key") != null) {
            this.publicKeySigningKey = jsonObj.get("public_key_signing_key").asText();
        }

        if (jsonObj.get("ds_records") != null && jsonObj.get("ds_records").isArray()) {
            for (JsonNode dsRecord : jsonObj.get("ds_records")) {
                this.dsRecords.add(dsRecord.asText());
            }
        }

        if (jsonObj.get("nameservers") != null && jsonObj.get("nameservers").isArray()) {
            for (JsonNode nameserver : jsonObj.get("nameservers")) {
                this.nameservers.add(nameserver.asText());
            }
        }

        if(jsonObj.get("nextroll_date") != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            this.nextRoll = sdf.parse(jsonObj.get("nextroll_date").asText());
        }
    }

    /**
     * Get Domain Name
     *
     * @return Domain Name
     */
    public String getName() {
        return name;
    }

    /**
     * Set Domain Name
     *
     * @param name Domain Name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get Domain Status
     *
     * @return Domain Status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set Domain Status
     *
     * @param status Domain Status (readonly via API)
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Get DNSSEC Delegation Status
     *
     * @return Domain DNSSEC delegation complete
     */
    public boolean getDelegationStatus() {
        return delegationStatus;
    }

    /**
     * Get DNSSEC Delegation Message
     *
     * @return Message describing domain's DNSSEC delegation status
     */
    public String getDelegationMessage() {
        return delegationMessage;
    }

    /**
     * Get Total Count of Wallet Names on Domain
     *
     * @return Wallet Name Count
     */
    public int getWalletNameCount() {
        return walletNameCount;
    }

    /**
     * Get Next DNSSEC KSK (Key Signing Key) Roll Date
     *
     * See: <a href="https://en.wikipedia.org/wiki/Domain_Name_System_Security_Extensions#Key_management">https://en.wikipedia.org/wiki/Domain_Name_System_Security_Extensions#Key_management</a>
     *
     * @return Next KSK Roll Date
     */
    public Date getNextRoll() {
        return nextRoll;
    }

    /**
     * Get Domain DS Records
     *
     * @return DS Records
     */
    public List<String> getDsRecords() {
        return dsRecords;
    }

    /**
     * Get Domain Nameservers
     *
     * @return Nameservers
     */
    public List<String> getNameservers() {
        return nameservers;
    }

    /**
     * Set Nameservers (not writable via Netki API)
     *
     * @param nameservers Nameservers
     */
    public void setNameservers(List<String> nameservers) {
        this.nameservers = nameservers;
    }

    /**
     * Get Public Key for KSK
     *
     * @return KSK Public Key
     */
    public String getPublicKeySigningKey() {
        return publicKeySigningKey;
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
     * @param requestor Requestor used for API operations
     */
    public void setRequestor(Requestor requestor) {
        this.requestor = requestor;
    }
}
