package com.netki;

import com.google.api.client.util.Joiner;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Netki Partner Client
 */
public class NetkiClient {

    private String partnerId;
    private String apiKey;
    private String apiUrl = "https://api.netki.com";
    private Requestor requestor = new Requestor();
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Instantiate a NetkiClient object
     *
     * @param partnerId Netki Partner ID
     * @param apiKey Netki Partner API Key
     * @param apiUrl Netki Base URL (i.e., https://api.netki.com). A value of null leaves the default.
     */
    public NetkiClient(String partnerId, String apiKey, String apiUrl)
    {
        this.partnerId = partnerId;
        this.apiKey = apiKey;
        if (apiUrl != null && !apiUrl.equals("")) {
            this.apiUrl = apiUrl;
        }
    }

    /**
     * Instantiate a NetkiClient object in <b>TEST</b>.
     *
     * @param partnerId Netki Partner ID
     * @param apiKey Netki Partner API Key
     * @param apiUrl Netki Base URL (i.e., https://api.netki.com). A value of null leaves the default
     * @param requestor Netki Requestor (Used in <b>TEST</b>)
     */
    public NetkiClient(String partnerId, String apiKey, String apiUrl, Requestor requestor)
    {
        this.partnerId = partnerId;
        this.apiKey = apiKey;
        this.requestor = requestor;

        if (apiUrl != null && !apiUrl.equals("")) {
            this.apiUrl = apiUrl;
        }
    }

    /**
     * Get all Wallet Names
     *
     * @return List of WalletNames
     * @throws Exception  Occurs on Bad HTTP Request / Response
     */
    public List<WalletName> getWalletNames() throws Exception {
        return this.getWalletNames(null, null);
    }

    /**
     * Get Wallet Names matching the given criteria
     *
     * @param domainName Domain Name filter
     * @param externalId External ID filter
     * @return List of matching Wallet Names
     * @throws Exception Occurs on Bad HTTP Request / Response
     */
    public List<WalletName> getWalletNames(String domainName, String externalId) throws Exception {

        List<WalletName> results = new ArrayList<WalletName> ();

        List<String> args = new ArrayList<String>();
        if (domainName != null && !domainName.equals("")) {
            args.add("domain_name=" + domainName);
        }

        if (externalId != null && !externalId.equals("")) {
            args.add("external_id=" + externalId);
        }

        String uri = this.apiUrl + "/v1/partner/walletname";
        if (args.size() > 0) {
            uri = uri + "?" + Joiner.on('&').join(args);
        }

        String respStr = this.requestor.processRequest(
                this.apiKey,
                this.partnerId,
                uri,
                "GET",
                null
        );

        JsonNode respJson = this.mapper.readTree(respStr);

        if (respJson.get("wallet_name_count").asInt() == 0) {
            return results;
        }

        for (JsonNode data : respJson.get("wallet_names")) {
            WalletName wn = new WalletName(this.requestor);
            wn.setId(data.get("id").asText());
            wn.setDomainName(data.get("domain_name").asText());
            wn.setName(data.get("name").asText());
            wn.setExternalId(data.get("external_id").asText());

            for (JsonNode wallet : data.get("wallets")) {
                wn.setCurrencyAddress(wallet.get("currency").asText(), wallet.get("wallet_address").asText());
            }

            wn.setApiOpts(this.apiUrl, this.apiKey, this.partnerId);
            results.add(wn);
        }

        return results;
    }

    /**
     * Create New WalletName object
     *
     * @param domainName Domain Name of Wallet Name
     * @param name Name of Wallet Name
     * @param externalId ExternalID of Wallet Name
     * @return Newly Created WalletName object. <b>NOTE:</b> the WalletName must be save()ed to commit to Netki
     */
    public WalletName createWalletName(String domainName, String name, String externalId) {
        WalletName wn = new WalletName ();
        wn.setDomainName(domainName);
        wn.setName(name);
        wn.setExternalId(externalId);
        wn.setApiOpts(this.apiUrl, this.apiKey, this.partnerId);
        return wn;
    }

    /*
     * Partner Operations
     */

    /**
     * Create a new Partner
     *
     * @param partnerName New Partner Name
     * @return New Partner object that has already been saved to Netki
     * @throws Exception Occurs on Bad HTTP Request / Response
     */
    public Partner createPartner(String partnerName) throws Exception {

        String responseStr = this.requestor.processRequest(
                this.apiKey,
                this.partnerId,
                this.apiUrl + "/v1/admin/partner/" + partnerName,
                "POST",
                null
        );

        JsonNode data = this.mapper.readTree(responseStr);

        Partner partner = new Partner(data.get("partner").get("id").asText(), data.get("partner").get("name").asText());
        partner.setApiOpts(this.apiUrl, this.apiKey, this.partnerId);
        return partner;
    }

    /**
     * Get all partners
     *
     * @return List of Partners
     * @throws Exception Occurs on Bad HTTP Request / Response
     */
    public List<Partner> getPartners() throws Exception {

        List<Partner> partners = new ArrayList<Partner>();

        String responseStr = this.requestor.processRequest(
                this.apiKey,
                this.partnerId,
                this.apiUrl + "/v1/admin/partner",
                "GET",
                null
        );

        JsonNode data = this.mapper.readTree(responseStr);

        if (data.get("partners") == null) {
            return partners;
        }

        for (JsonNode partner : data.get("partners")) {
            Partner p = new Partner (partner.get("id").asText(), partner.get("name").asText());
            p.setApiOpts (this.apiUrl, this.apiKey, this.partnerId);
            partners.add(p);
        }

        return partners;
    }

    /*
     * Domains Operations
     */

    /**
     * Create and save a new Domain with Netki
     *
     * @param domainName Domain Name
     * @param partner Partner to own the domain (can be null if not owned by a partner)
     * @return New Domain object
     * @throws Exception Occurs on Bad HTTP Request / Response
     */
    public Domain createDomain(String domainName, Partner partner) throws Exception {

        String submitData = null;

        if (partner != null) {
            Map<String, String> subDict = new HashMap<String, String>();
            subDict.put("partner_id", partner.getId());

            submitData = this.mapper.writeValueAsString(subDict);
        }

        String responseStr = requestor.processRequest(
                apiKey,
                partnerId,
                this.apiUrl + "/v1/partner/domain/" + domainName,
                "POST",
                submitData
        );

        JsonNode data = this.mapper.readTree(responseStr);

        Domain domain = new Domain(domainName);
        domain.setApiOpts(this.apiUrl, this.apiKey, this.partnerId);
        domain.setStatus(data.get("status").asText());

        List<String> nameservers = new ArrayList<String>();
        for (JsonNode nsObj : data.get("nameservers")) {
            nameservers.add(nsObj.asText());
        }
        domain.setNameservers(nameservers);
        return domain;
    }

    /**
     * Get all partner domains
     *
     * @return List of partner domains
     * @throws Exception Occurs on Bad HTTP Request / Response
     */
    public List<Domain> getDomains() throws Exception {

        List<Domain> domains = new ArrayList<Domain>();

        String responseStr = requestor.processRequest(
                this.apiKey,
                this.partnerId,
                this.apiUrl + "/api/domain",
                "GET",
                null
        );

        JsonNode data = this.mapper.readTree(responseStr);

        if (data.get("domains") == null) {
            return domains;
        }

        for (JsonNode domain : data.get("domains")) {
            Domain d = new Domain(domain.get("domain_name").asText(), this.requestor);
            d.setApiOpts(this.apiUrl, this.apiKey, this.partnerId);
            d.loadStatus();
            d.loadDnssecDetails();
            domains.add(d);
        }

        return domains;
    }

    public static void main(String[] args) {

//        String partnerId = "XXXXXXXXXXXXXXXXXXXX";
//        String apiKey = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
//
//        NetkiClient client = new NetkiClient(partnerId, apiKey, "http://localhost:5000");
//
//        try {
//            List<Domain> domains = client.getDomains();
//
//            for (Domain domain : domains) {
//                if (domain.getName().equals("mgdtestdomain1.com") || domain.getName().equals("mgdpartnertestdomain.com")) {
//                    domain.delete();
//                }
//            }
//
//            for(Partner partner : client.getPartners()) {
//                if (partner.getName().equals("SubPartner 75")) {
//                    partner.delete();
//                }
//            }
//
//            Domain newTestDomain = client.createDomain("mgdtestdomain1.com", null);
//            List<Domain> domains2 = client.getDomains();
//            List<Partner> partners = client.getPartners();
//
//            Partner newPartner = client.createPartner("SubPartner 75");
//            List<Partner> partners2 = client.getPartners();
//
//            Domain partnerTestDomain = client.createDomain("mgdpartnertestdomain.com", newPartner);
//            List<Domain> domains3 = client.getDomains();
//
//            partnerTestDomain.delete();
//            newPartner.delete();
//            List<Partner> partners3 = client.getPartners();
//
//            // Test Wallets
//            List<WalletName> walletNames = client.getWalletNames();
//            WalletName walletName = client.createWalletName("mgdtestdomain1.com", "testwallet", "externalId");
//            walletName.setCurrencyAddress("btc", "1btcaddress");
//            walletName.save();
//            List<WalletName> walletNames2 = client.getWalletNames("mgdtestdomain1.com", null);
//
//            walletName.setCurrencyAddress("ltc", "LtcAddress1");
//            walletName.save();
//            List<WalletName> walletNames3 = client.getWalletNames("mgdtestdomain1.com", null);
//
//            walletName.delete();
//            newTestDomain.delete();
//            List<Domain> domains4 = client.getDomains();
//            System.out.println("DONE");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }
    
}
