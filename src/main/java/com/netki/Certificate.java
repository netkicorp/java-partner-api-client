package com.netki;

import com.google.common.base.CaseFormat;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.spongycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.spongycastle.asn1.x500.X500NameBuilder;
import org.spongycastle.asn1.x500.style.BCStyle;
import org.spongycastle.asn1.x509.*;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.openssl.jcajce.JcaPEMWriter;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;
import org.spongycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.spongycastle.pkcs.PKCS10CertificationRequest;
import org.spongycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.spongycastle.util.io.pem.PemObject;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.security.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Certificate extends BaseObject {

    private Requestor requestor = new Requestor();
    private ObjectMapper mapper = new ObjectMapper();

    private String id = null;
    private String dataToken = null;
    private String orderStatus = "UNKNOWN";
    private String orderError = null;
    private String productId = null;

    // Cert Bundle
    private String rootPem = null;
    private String certPem = null;
    private List<String> intermediateCerts = new ArrayList<String>(2);

    // Customer Data
    private CustomerData customerData = null;

    /**
     * Instantiate an empty Certificate object
     */
    public Certificate() {}

    /**
     * Instantiate an empty WalletName object using a specified {@link Requestor}. Used only in <b>TEST</b>.
     *
     * @param requestor Requestor to use for Netki API interaction
     */
    public Certificate(Requestor requestor)
    {
        if(requestor != null)
        {
            this.requestor = requestor;
        }
    }

    // Object Methods
    protected String generateCsr(KeyPair key) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        if (!key.getPrivate().getAlgorithm().equals("RSA")) {
            throw new NoSuchAlgorithmException("RSA KeyPair Required");
        }

        // Build X500 Name
        X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        nameBuilder.addRDN(BCStyle.C, this.getCustomerData().getCountry());
        nameBuilder.addRDN(BCStyle.O, this.getCustomerData().getOrganizationName());
        nameBuilder.addRDN(BCStyle.L, this.getCustomerData().getCity());
        nameBuilder.addRDN(BCStyle.CN, this.getCustomerData().getFirstName() + " " + this.getCustomerData().getLastName());
        nameBuilder.addRDN(BCStyle.STREET, this.getCustomerData().getStreetAddress());
        nameBuilder.addRDN(BCStyle.POSTAL_CODE, this.getCustomerData().getPostalCode());

        // Build Subject PublicKey Info
        SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(key.getPublic().getEncoded());

        // Create PCKS10 CSR Builder
        PKCS10CertificationRequestBuilder reqBuilder = new PKCS10CertificationRequestBuilder(nameBuilder.build(), subjectPublicKeyInfo);

        // Add basicConstraints and KeyUsage extensions
        ExtensionsGenerator extensionsGenerator = new ExtensionsGenerator();
        extensionsGenerator.addExtension(Extension.basicConstraints, false, new BasicConstraints(false));
        extensionsGenerator.addExtension(Extension.keyUsage, false, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment | KeyUsage.nonRepudiation));
        reqBuilder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, extensionsGenerator.generate());

        // Build & Sign CSR
        PKCS10CertificationRequest req = reqBuilder.build(new JcaContentSignerBuilder("SHA256withRSA").setProvider("SC").build(key.getPrivate()));

        // Validate CSR Signature
        if (!req.isSignatureValid(new JcaContentVerifierProviderBuilder().setProvider("SC").build(key.getPublic())))
        {
            throw new SignatureException("CSR Signature Failure");
        }

        // Convert to PEM and Return as String
        PemObject pemObject = new PemObject("CERTIFICATE REQUEST", req.getEncoded());
        StringWriter str = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(str);
        pemWriter.writeObject(pemObject);
        pemWriter.close();
        str.close();
        return str.toString();

    }

    public void submitUserData() throws Exception {

        Map<String, String> fullRequest = new HashMap<String, String> ();
        String requestJson;
        String respJsonString;

        if(this.getCustomerData() == null) {
            throw new Exception("Missing Customer Data");
        }

        for(Field field : this.getCustomerData().getClass().getDeclaredFields()) {
            field.setAccessible(true);

            if(field.getName().equals("organizationName")) continue;
            if(field.get(this.getCustomerData()) == null) continue;

            if(field.getType() == String.class) {
                fullRequest.put(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName()), (String)field.get(this.getCustomerData()));
            } else if(field.getType() == Date.class) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
                String dateString = sdf.format(field.get(this.getCustomerData()));
                fullRequest.put(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName()), dateString);
            }
        }

        int idIndex = 0;
        for(IdentityDocument id : this.getCustomerData().getIdentityDocuments()) {
            idIndex++;

            for(Field field : id.getClass().getDeclaredFields()) {

                field.setAccessible(true);

                if(field.get(id) == null) {
                    continue;
                }

                StringBuilder sb = new StringBuilder();
                sb.append("identity_");
                if(!field.getName().equals("identity")) {
                    sb.append(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName()));
                }
                if(idIndex > 1) {
                    sb.append(idIndex);
                }

                if(field.getType() == String.class) {
                    fullRequest.put(sb.toString(), (String)field.get(id));
                }
                else if(field.getType() == Date.class) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
                    String dateString = sdf.format(field.get(id));
                    fullRequest.put(sb.toString(), dateString);
                }
            }

        }

        fullRequest.put("product", this.getProductId());

        try {
            requestJson = mapper.writeValueAsString(fullRequest);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Unable to Build JSON Request");
        }

        respJsonString = this.requestor.processRequest(
                this.getClient(),
                "/v1/certificate/token",
                "POST",
                requestJson
        );

        JsonNode responseNode = mapper.readTree(respJsonString);

        if(responseNode.has("token")) {
            this.setDataToken(responseNode.get("token").asText());
        } else {
            throw new Exception("Data Token Missing from API Response");
        }

    }

    public void submitOrder(String stripeToken) throws Exception {

        Map<String, String> fullRequest = new HashMap<String, String> ();
        String requestJson;
        String respJsonString;

        if(this.getDataToken() == null) {
            throw new Exception("Missing dataToken");
        }

        fullRequest.put("certdata_token", this.getDataToken());
        fullRequest.put("email", this.getCustomerData().getEmail());
        fullRequest.put("product", this.getProductId());

        if(stripeToken != null) {
            fullRequest.put("stripe_token", stripeToken);
        }

        try {
            requestJson = mapper.writeValueAsString(fullRequest);
        } catch (Exception e) {
            throw new Exception("Unable to Build JSON Request");
        }

        respJsonString = this.requestor.processRequest(
                this.getClient(),
                "/v1/certificate",
                "POST",
                requestJson
        );

        JsonNode responseNode = mapper.readTree(respJsonString);

        if(responseNode.has("order_id")) {
            this.setId(responseNode.get("order_id").asText());
        } else {
            throw new Exception("Order ID Missing from API Response");
        }
    }

    public void submitCSR(KeyPair key) throws Exception {

        if(this.getId() == null) {
            throw new Exception("Certificate Must Have a Valid Order Number");
        }

        Map<String, Object> fullRequest = new HashMap<String, Object> ();
        fullRequest.put("signed_csr", this.generateCsr(key));

        String requestJson;

        try {
            requestJson = mapper.writeValueAsString(fullRequest);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Unable to Build JSON Request");
        }

        this.requestor.processRequest(
                this.getClient(),
                "/v1/certificate/" + this.id + "/csr",
                "POST",
                requestJson
        );

    }

    public void revoke(String reason) throws Exception {

        if(this.getId() == null) {
            throw new Exception("Certificate Must Have a Valid Order Number");
        }

        Map<String, Object> fullRequest = new HashMap<String, Object> ();
        fullRequest.put("revocation_reason", reason);

        String requestJson;

        try {
            requestJson = mapper.writeValueAsString(fullRequest);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Unable to Build JSON Request");
        }

        this.requestor.processRequest(
                this.getClient(),
                "/v1/certificate/" + this.id,
                "DELETE",
                requestJson
        );
    }

    public void getStatus() throws Exception {

        if(this.getId() == null) {
            throw new Exception("Certificate Must Have a Valid Order Number");
        }

        String respJsonString = this.requestor.processRequest(
                this.getClient(),
                "/v1/certificate/" + this.id,
                "GET",
                null
        );

        JsonNode responseNode = mapper.readTree(respJsonString);

        // Set Order Status
        if(responseNode.has("order_status")) {
            this.setOrderStatus(responseNode.get("order_status").asText());
        }

        // Set Order Error
        if(responseNode.has("order_error")) {
            this.setOrderError(responseNode.get("order_error").asText());
        }

        // Parse JSON Bundle
        if(responseNode.has("certificate_bundle") && responseNode.get("certificate_bundle") != null) {
            JsonNode bundle = responseNode.get("certificate_bundle");
            this.setRootPem(bundle.get("root").asText());
            this.setCertPem(bundle.get("certificate").asText());

            for(JsonNode intNode : bundle.get("intermediate")) {
                this.addIntermediateCert(intNode.asText());
            }

        }
    }

    public boolean isOrderComplete() {
        return this.getOrderStatus().equals("Order Finalized");
    }


    /***********************
     * Getters and Setters
     ***********************/
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDataToken() {
        return dataToken;
    }

    public void setDataToken(String dataToken) {
        this.dataToken = dataToken;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderError() {
        return orderError;
    }

    public void setOrderError(String orderError) {
        this.orderError = orderError;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getRootPem() {
        return rootPem;
    }

    public void setRootPem(String rootPem) {
        this.rootPem = rootPem;
    }

    public String getCertPem() {
        return certPem;
    }

    public void setCertPem(String certPem) {
        this.certPem = certPem;
    }

    public List<String> getIntermediateCerts() {
        return intermediateCerts;
    }

    public void setIntermediateCerts(List<String> intermediateCerts) {
        this.intermediateCerts = intermediateCerts;
    }

    public void addIntermediateCert(String certPem) {
        this.intermediateCerts.add(certPem);
    }

    public CustomerData getCustomerData() {
        return customerData;
    }

    public void setCustomerData(CustomerData customerData) {
        this.customerData = customerData;
    }

}
