package com.netki;

import com.google.api.client.util.Joiner;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.IntNode;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.spongycastle.jce.spec.ECNamedCurveSpec;

import java.security.*;
import java.util.*;


/**
 * Netki Partner Client
 */
public class NetkiClient {

    private String partnerId;
    private String apiKey;
    private String apiUrl = "https://api.netki.com";
    private String partnerKskHex;
    private String partnerKskSigHex;
    private KeyPair userKey;
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
        this(partnerId, apiKey, apiUrl, null);
    }

    /**
     * Instantiate a NetkiClient (with specific Requestor)
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

        if(requestor != null) {
            this.requestor = requestor;
        }

        if (apiUrl != null && !apiUrl.equals("")) {
            this.apiUrl = apiUrl;
        }
    }

    /**
     * Instantiate NetkiClient using Distributed API Access
     *
     * @param partnerKeySigningKeyHex Partner Key Signing Key (KSK) DER-Encoded (in HEX format)
     * @param partnerKSKSignature Signature over userKey DER-encoded public key by Partner KSK (in HEX format)
     * @param userKey User's KeyPair
     * @param apiUrl Netki Base URL (i.e., https://api.netki.com). A value of null leaves the default
     */
    public NetkiClient(String partnerKeySigningKeyHex, String partnerKSKSignature, KeyPair userKey, String apiUrl) throws Exception {
        this(partnerKeySigningKeyHex, partnerKSKSignature, userKey, apiUrl, null);
    }

    /**
     * Instantiate NetkiClient using Distributed API Access (with specific Requestor)
     * @param partnerKeySigningKeyHex Partner Key Signing Key (KSK) DER-Encoded (in HEX format)
     * @param partnerKSKSignature Signature over userKey DER-encoded public key by Partner KSK (in HEX format)
     * @param userKey User's KeyPair
     * @param apiUrl Netki Base URL (i.e., https://api.netki.com). A value of null leaves the default
     * @param requestor Netki Requestor (Used in <b>TEST</b>)
     */
    public NetkiClient(String partnerKeySigningKeyHex, String partnerKSKSignature, KeyPair userKey, String apiUrl, Requestor requestor) throws Exception {

        if(!userKey.getPublic().getAlgorithm().equals("ECDSA")) {
            throw new Exception("userKey MUST be an ECDSA Key");
        }

        if(!((ECNamedCurveSpec)((BCECPublicKey) userKey.getPublic()).getParams()).getName().equals("secp256k1")) {
            throw new Exception("userKey MUST be on the secp256k1 curve");
        }

        this.partnerKskHex = partnerKeySigningKeyHex;
        this.partnerKskSigHex = partnerKSKSignature;
        this.userKey = userKey;

        if (apiUrl != null && !apiUrl.equals("")) {
            this.apiUrl = apiUrl;
        }

        if(requestor != null) {
            this.requestor = requestor;
        }
    }

    /**
     * Instantiate NetkiClient using Signed Partner API Access
     * @param partnerId Netki Partner ID
     * @param userKey User's KeyPair
     * @param apiUrl Netki Base URL (i.e., https://api.netki.com). A value of null leaves the default
     */
    public NetkiClient(String partnerId, KeyPair userKey, String apiUrl) throws Exception {
        this(partnerId, userKey, apiUrl, null);
    }

    /**
     * Instantiate NetkiClient using Signed Partner API Access (with specific Requestor)
     * @param partnerId Netki Partner ID
     * @param userKey User's KeyPair
     * @param apiUrl Netki Base URL (i.e., https://api.netki.com). A value of null leaves the default
     * @param requestor Netki Requestor (Used in <b>TEST</b>)
     */
    public NetkiClient(String partnerId, KeyPair userKey, String apiUrl, Requestor requestor) throws Exception {

        if(!userKey.getPublic().getAlgorithm().equals("ECDSA")) {
            throw new Exception("userKey MUST be an ECDSA Key");
        }

        if(!((ECNamedCurveSpec)((BCECPublicKey) userKey.getPublic()).getParams()).getName().equals("secp256k1")) {
            throw new Exception("userKey MUST be on the secp256k1 curve");
        }

        this.partnerId = partnerId;
        this.userKey = userKey;

        if (apiUrl != null && !apiUrl.equals("")) {
            this.apiUrl = apiUrl;
        }

        if(requestor != null) {
            this.requestor = requestor;
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

        String uri = "/v1/partner/walletname";
        if (args.size() > 0) {
            uri = uri + "?" + Joiner.on('&').join(args);
        }

        String respStr = this.requestor.processRequest(
                this,
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

            wn.setClient(this);
            results.add(wn);
        }

        return results;
    }

    /**
     * Create New WalletName
     *
     * @param domainName Domain Name of Wallet Name
     * @param name Name of Wallet Name
     * @param externalId ExternalID of Wallet Name
     * @return Newly Created WalletName. <b>NOTE:</b> the WalletName must be save()ed to commit to Netki
     */
    public WalletName createWalletName(String domainName, String name, String externalId) {
        WalletName wn = new WalletName ();
        wn.setDomainName(domainName);
        wn.setName(name);
        wn.setExternalId(externalId);
        wn.setClient(this);
        return wn;
    }

    /**
     * Create New Certificate
     * @return Newly created Certificate <b>NOTE:</b> the Certificate must go through the
     * process of ordering as Customer data is not persisted.
     */
    public Certificate createCertificate() {
        Certificate cert = new Certificate();
        cert.setClient(this);
        return cert;
    }

    /**
     * Retrieve Existing Certificate
     * @param id Certificate Id
     * @return Retrieved Certificate from Netki
     * @throws Exception
     */
    public Certificate getCertificate(String id) throws Exception {
        Certificate cert = this.createCertificate();
        cert.setId(id);
        cert.setClient(this);
        cert.getStatus();
        return cert;
    }

    /**
     * Retrieve Available Products
     * @return List of Available Products
     * @throws Exception
     */
    public List<Product> getAvailableProducts() throws Exception {

        List<Product> results = new LinkedList<Product>();

        String respStr = this.requestor.processRequest(
                this,
                "/v1/certificate/products",
                "GET",
                null
        );

        JsonNode respJson = this.mapper.readTree(respStr);

        if (respJson.get("products") == null) {
            return results;
        }

        for (JsonNode data : respJson.get("products")) {
            Product p = new Product();

            if(data.get("id") == null) {
                throw new NetkiException("Product Response Missing ID Field");
            }

            p.setId(data.get("id").asText());

            if(data.get("product_name") != null)
                p.setName(data.get("product_name").asText());

            if(data.get("current_tier") != null)
                p.setCurrentTierName(data.get("current_tier").asText());

            if(data.get("term") != null)
                p.setTerm(data.get("term").asInt());

            if (data.get("current_price") != null) {
                Iterator<Map.Entry<String, JsonNode>> priceIterator = data.get("current_price").getFields();
                while(priceIterator.hasNext()) {
                    Map.Entry<String, JsonNode> field = priceIterator.next();
                    p.setCurrentPrice(field.getKey(), field.getValue().asInt());
                }
            }

            results.add(p);
        }

        return results;

    }

    /**
     * Retrieve CA Certificates
     * @return String CACert File
     * @throws Exception
     */
    public String getCACertBundle() throws Exception {

        String respStr = this.requestor.processRequest(
                this,
                "/v1/certificate/cacert",
                "GET",
                null
        );

        JsonNode respJson = this.mapper.readTree(respStr);

        if (respJson.get("cacerts") == null) {
            return "";
        }
        return respJson.get("cacerts").asText();

    }

    /**
     * Retrieve Current Account Balance in USD Cents
     * @return Integer representation of current account balance in USD Cents
     * @throws Exception
     */
    public Integer getAccountBalance() throws Exception {

        String respStr = this.requestor.processRequest(
                this,
                "/v1/certificate/balance",
                "GET",
                null
        );

        JsonNode respJson = this.mapper.readTree(respStr);

        if (respJson.get("available_balance") == null) {
            return 0;
        }
        return respJson.get("available_balance").asInt();

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
                this,
                "/v1/admin/partner/" + partnerName,
                "POST",
                null
        );

        JsonNode data = this.mapper.readTree(responseStr);

        Partner partner = new Partner(data.get("partner").get("id").asText(), data.get("partner").get("name").asText());
        partner.setClient(this);
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
                this,
                "/v1/admin/partner",
                "GET",
                null
        );

        JsonNode data = this.mapper.readTree(responseStr);

        if (data.get("partners") == null) {
            return partners;
        }

        for (JsonNode partner : data.get("partners")) {
            Partner p = new Partner (partner.get("id").asText(), partner.get("name").asText());
            p.setClient(this);
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
                this,
                "/v1/partner/domain/" + domainName,
                "POST",
                submitData
        );

        JsonNode data = this.mapper.readTree(responseStr);

        Domain domain = new Domain(domainName);
        domain.setClient(this);
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
                this,
                "/api/domain",
                "GET",
                null
        );

        JsonNode data = this.mapper.readTree(responseStr);

        if (data.get("domains") == null) {
            return domains;
        }

        for (JsonNode domain : data.get("domains")) {
            Domain d = new Domain(domain.get("domain_name").asText(), this.requestor);
            d.setClient(this);
            d.loadStatus();
            d.loadDnssecDetails();
            domains.add(d);
        }

        return domains;
    }

    /**
     * Get NetkiClient partnerId
     * @return Netki Partner ID
     */
    public String getPartnerId() {
        return partnerId;
    }

    /**
     * Get Netki API Key
     * @return Netki API Key
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Get Netki API Base URL
     * @return Netki API Base URL
     */
    public String getApiUrl() {
        return apiUrl;
    }

    /**
     * Get Partner Key Signing Key (KSK) in Hex Format
     * @return Partner KSK in Hex Format
     */
    public String getPartnerKskHex() {
        return partnerKskHex;
    }

    /**
     * Get Partner-generated Signature over User's Public Key (in DER Format) using the Partner KSK
     * @return
     */
    public String getPartnerKskSigHex() {
        return partnerKskSigHex;
    }

    /**
     * Get User's KeyPair
     * @return KeyPair
     */
    public KeyPair getUserKey() {
        return userKey;
    }

//    public static void main(String[] args) {
//
//        String ecdsaPrivKey = "30818D020100301006072A8648CE3D020106052B8104000A047630740201010420B5ECE22AB6FCBCAF4BB9B965125C7D96C6FD9988F21A60A24291B5AC9A99626BA00706052B8104000AA14403420004DEC7133D28727AE93AF1003E24538E6471698A86309A1946865D31E8B43748790C6D7AB25132A53D1B2593DACA8C32ACA7083F46E277F8CE374311D2C9F727A5";
//        String partnerId = "XXXXXXXXXXXXXXXXXXXX";
//        String apiKey = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
//        String partnerKsk = "3056301006072a8648ce3d020106052b8104000a03420004b185c46e29ad04654caa96f168c9b7c7d476103ec07274749c08af8c91a0ded94b04ba8b791b79913776a05a77b6f2408aea43cb4a9e3c30d593a475de82c3f5";
//        String partnerKskSig = "30450221008cecbf4776c5d6ef713cd4bb4e4c4db7181ae0629859ccd77aca4a62a39bfc7a0220273145a3baf7338efa8bed231f9af0afcb3b88bacad4dbb653385cd3448d42d3";
//
//        // Generate New User Key For Each Invocation
//        Security.insertProviderAt(new BouncyCastleProvider(), 1);
//        KeyPair keyPair = null;
//        KeyPair storedKeyPair = null;
//        try {
//            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
//            KeyPairGenerator generator = KeyPairGenerator.getInstance("ECDSA", "SC");
//            generator.initialize(ecSpec, new SecureRandom());
//            keyPair = generator.generateKeyPair();
//        } catch (Exception e) {
//            System.out.println("Error Creating ECKeyPair: " + e.toString());
//            System.exit(-1);
//        }
//
//        // Re-create KeyPair from Existing ecdsaPrivKey
//        KeyPair kp = null;
//        try {
//            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "SC");
//            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
//            ECPrivateKeySpec privKeySpec = new ECPrivateKeySpec(new BigInteger(1, BaseEncoding.base16().decode(ecdsaPrivKey)), ecSpec);
//            PrivateKey privKey = keyFactory.generatePrivate(privKeySpec);
//
//            // Generate Public Key from Private Key
//            X9ECParameters ecp = SECNamedCurves.getByName("secp256k1");
//            ECPoint curvePt = ecp.getG().multiply(((BCECPrivateKey)privKey).getD());
//            ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(curvePt, ecSpec);
//            PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
//            kp = new KeyPair(pubKey, privKey);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (NoSuchProviderException e) {
//            e.printStackTrace();
//        } catch (InvalidKeySpecException e) {
//            e.printStackTrace();
//        }
//
//        NetkiClient client = null;
//        try {
//            //client = new NetkiClient(partnerKsk, partnerKskSig, keyPair, null);
//            client = new NetkiClient(partnerKsk, partnerKskSig, kp, "http://localhost:5000");
//        } catch (Exception e) {
//            System.out.println("NetkiClient CTOR Thew Exception: " + e.toString());
//            System.exit(-1);
//        }
//
//        try {
//
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
//
//    }
    
}
