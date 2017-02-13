package com.netki;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.security.*;
import java.util.*;

import static com.netki.TestUtil.generateKey;
import static java.util.Arrays.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@PowerMockIgnore({"javax.*", "org.spongycastle.*", "org.mockito.*", "com.madgag.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest(NetkiClient.class)
public class NetkiClientTest {

    Requestor mockRequestor;
    ObjectMapper mapper = new ObjectMapper();

    String partnerId = "partner_id";
    String apiKey = "api_key";
    String apiUrl = "https://server";

    @Before
    public void setUp() {
        this.mockRequestor = mock(Requestor.class);
    }

    @After
    public void tearDown() {
        reset(this.mockRequestor);
    }

    @Test
    public void ApiKeyInstantiation()
    {
        NetkiClient netki = new NetkiClient(this.partnerId, this.apiKey, this.apiUrl);
        assertNotNull(netki);
        assertEquals(this.partnerId, netki.getPartnerId());
        assertEquals(this.apiKey, netki.getApiKey());
        assertEquals(this.apiUrl, netki.getApiUrl());
    }

    @Test
    public void ApiKeyInstantiationDefaultURL()
    {
        NetkiClient netki = new NetkiClient(this.partnerId, this.apiKey, null);
        assertNotNull(netki);
        assertEquals(this.partnerId, netki.getPartnerId());
        assertEquals(this.apiKey, netki.getApiKey());
        assertEquals("https://api.netki.com", netki.getApiUrl());
    }

    @Test
    public void DistributedAccessInstantiation() {
        NetkiClient netki = null;
        KeyPair keyPair = generateKey("ECDSA");

        if(keyPair == null) {
            fail("keyPair is null");
        }

        try {
            netki = new NetkiClient("ffff", "eeee", keyPair, null);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertEquals("ffff", netki.getPartnerKskHex());
        assertEquals("eeee", netki.getPartnerKskSigHex());
        assertEquals(keyPair, netki.getUserKey());

    }

    @Test
    public void PartnerSignedAccessInstantiation() {
        NetkiClient netki = null;
        KeyPair keyPair = generateKey("ECDSA");

        try {
            netki = new NetkiClient("partnerId", keyPair, null);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertEquals("partnerId", netki.getPartnerId());
        assertEquals(keyPair, netki.getUserKey());

    }


    @Test
    public void GetWalletNamesNoWalletNames()
    {
        Map<String, Object> WalletNameGetResponse = new HashMap<String, Object>();
        WalletNameGetResponse.put("wallet_name_count", 0);

        try {
            when(this.mockRequestor.processRequest(any(NetkiClient.class), eq("/v1/partner/walletname"), eq("GET"), isNull(String.class))).thenReturn(mapper.writeValueAsString(WalletNameGetResponse));
        } catch (Exception e) {
            e.printStackTrace();
        }

        NetkiClient netki = new NetkiClient(this.partnerId, this.apiKey, this.apiUrl, this.mockRequestor);
        List<WalletName> results = null;
        try {
            results = netki.getWalletNames();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Validate Call
        try {
            verify(this.mockRequestor, times(1)).processRequest(any(NetkiClient.class), eq("/v1/partner/walletname"), eq("GET"), anyString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(0, results.size());
    }

    @Test
    public void GetWalletNamesAllOptions()
    {

        Map<String, Object> walletNameObj = new HashMap<String, Object>();
        walletNameObj.put("id", "id");
        walletNameObj.put("domain_name", "domain.com");
        walletNameObj.put("name", "wallet");
        walletNameObj.put("external_id", "external_id");

        Map<String, String> c1 = new HashMap<String, String>();
        Map<String, String> c2 = new HashMap<String, String>();

        c1.put("currency", "btc");
        c1.put("wallet_address", "1btcaddress");
        c2.put("currency", "ltc");
        c2.put("wallet_address", "Ltcaddress42");
        List<Object> walletsList = new ArrayList<Object>(asList(c1, c2));
        walletNameObj.put("wallets", walletsList);

        Map<String, Object> getResponse = new HashMap<String, Object>();
        getResponse.put("wallet_name_count", 1);
        getResponse.put("wallet_names", new ArrayList<Object>(Collections.singletonList(walletNameObj)));

        try {
            when(this.mockRequestor.processRequest(any(NetkiClient.class), eq("/v1/partner/walletname?domain_name=domain.com&external_id=external_id"), eq("GET"), isNull(String.class))).thenReturn(mapper.writeValueAsString(getResponse));
        } catch (Exception e) {
            e.printStackTrace();
        }

        NetkiClient netki = new NetkiClient(this.partnerId, this.apiKey, this.apiUrl, this.mockRequestor);
        List<WalletName> results = null;
        try {
            results = netki.getWalletNames("domain.com", "external_id");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Validate Call
        try {
            verify(this.mockRequestor, times(1)).processRequest(any(NetkiClient.class), eq("/v1/partner/walletname?domain_name=domain.com&external_id=external_id"), eq("GET"), anyString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(1, results.size());
        assertEquals("id", results.get(0).getId());
        assertEquals("domain.com", results.get(0).getDomainName());
        assertEquals("wallet", results.get(0).getName());
        assertEquals("external_id", results.get(0).getExternalId());
        assertEquals("1btcaddress", results.get(0).getWalletAddress("btc"));
        assertEquals("Ltcaddress42", results.get(0).getWalletAddress("ltc"));
        assertTrue(results.get(0).getUsedCurrencies().contains("btc"));
        assertTrue(results.get(0).getUsedCurrencies().contains("ltc"));
    }

    @Test
    public void CreateWalletName()
    {
        NetkiClient netki = new NetkiClient(this.partnerId, this.apiKey, this.apiUrl, this.mockRequestor);
        WalletName walletName = netki.createWalletName("domain.com", "wallet", "external_id");

        assertEquals("domain.com", walletName.getDomainName());
        assertEquals("wallet", walletName.getName());
        assertEquals("external_id", walletName.getExternalId());
        assertEquals(netki, walletName.getClient());
    }

    @Test
    public void CreateCertificate()
    {
        NetkiClient netki = new NetkiClient(this.partnerId, this.apiKey, this.apiUrl, this.mockRequestor);
        Certificate certificate = netki.createCertificate();
        assertEquals(netki, certificate.getClient());
    }

    @Test
    public void GetCertificate() throws Exception {

        Certificate mockCert = mock(Certificate.class);
        whenNew(Certificate.class).withNoArguments().thenReturn(mockCert);
        NetkiClient netki = new NetkiClient(this.partnerId, this.apiKey, this.apiUrl, this.mockRequestor);

        Certificate certificate = netki.getCertificate("id");

        verify(mockCert, times(1)).setId(eq("id"));
        verify(mockCert, times(1)).getStatus();
        assertEquals(netki, mockCert.getClient());
        assertEquals(certificate, mockCert);
    }

    @Test
    public void GetAvailableProducts_GoRight() throws Exception {

        NetkiClient netki = new NetkiClient(this.partnerId, this.apiKey, this.apiUrl, this.mockRequestor);
        String respJson = "{\"products\": [{\"current_price\": {\"UK\": 200, \"US\": 100}, \"id\": \"id1\", \"current_tier\": \"Base Tier\", \"product_name\": \"Product1\"}, {\"current_price\": {\"DE\": 500, \"AU\": 1000}, \"id\": \"id2\", \"current_tier\": \"Expensive Tier\", \"product_name\": \"Product2\"}]}";
        when(this.mockRequestor.processRequest(
                any(NetkiClient.class),
                eq("/v1/certificate/products"),
                eq("GET"),
                anyString())
        ).thenReturn(respJson);

        List<Product> products = netki.getAvailableProducts();

        assertEquals(2, products.size());
        assertEquals("id1", products.get(0).getId());
        assertEquals("Product1", products.get(0).getName());
        assertEquals("Base Tier", products.get(0).getCurrentTierName());
        assertEquals(new Integer(100), products.get(0).getCurrentPrice("US"));
        assertEquals(new Integer(200), products.get(0).getCurrentPrice("UK"));

        assertEquals("id2", products.get(1).getId());
        assertEquals("Product2", products.get(1).getName());
        assertEquals("Expensive Tier", products.get(1).getCurrentTierName());
        assertEquals(new Integer(1000), products.get(1).getCurrentPrice("AU"));
        assertEquals(new Integer(500), products.get(1).getCurrentPrice("DE"));

    }

    @Test
    public void GetAvailableProducts_MissingData() throws Exception {
        NetkiClient netki = new NetkiClient(this.partnerId, this.apiKey, this.apiUrl, this.mockRequestor);
        String respJson = "{\"products\": [{\"id\": \"id1\", \"current_tier\": \"Base Tier\", \"product_name\": \"Product1\"}, {\"current_price\": {\"DE\": 500, \"AU\": 1000}, \"id\": \"id2\"}]}";
        when(this.mockRequestor.processRequest(
                any(NetkiClient.class),
                eq("/v1/certificate/products"),
                eq("GET"),
                anyString())
        ).thenReturn(respJson);

        List<Product> products = netki.getAvailableProducts();

        assertEquals(2, products.size());
        assertEquals("id1", products.get(0).getId());
        assertEquals("Product1", products.get(0).getName());
        assertEquals("Base Tier", products.get(0).getCurrentTierName());
        assertEquals(0, products.get(0).getCountries().size());

        assertEquals("id2", products.get(1).getId());
        assertNull(products.get(1).getName());
        assertNull(products.get(1).getCurrentTierName());
        assertEquals(new Integer(1000), products.get(1).getCurrentPrice("AU"));
        assertEquals(new Integer(500), products.get(1).getCurrentPrice("DE"));
    }

    @Test
    public void GetAvailableProducts_MissingID() throws Exception {
        NetkiClient netki = new NetkiClient(this.partnerId, this.apiKey, this.apiUrl, this.mockRequestor);
        String respJson = "{\"products\": [{\"current_tier\": \"Base Tier\", \"product_name\": \"Product1\"}, {\"current_price\": {\"DE\": 500, \"AU\": 1000}, \"id\": \"id2\"}]}";
        when(this.mockRequestor.processRequest(
                any(NetkiClient.class),
                eq("/v1/certificate/products"),
                eq("GET"),
                anyString())
        ).thenReturn(respJson);

        try {
            netki.getAvailableProducts();
            fail("Expected Exception");
        } catch (NetkiException ne) {
            assertEquals("Product Response Missing ID Field", ne.getMessage());
        }
    }

    @Test
    public void GetCACertBundle_GoRight() throws Exception {
        NetkiClient netki = new NetkiClient(this.partnerId, this.apiKey, this.apiUrl, this.mockRequestor);
        String respJson = "{\"cacerts\":\"CACERT_PEMS\"}";
        when(this.mockRequestor.processRequest(
                any(NetkiClient.class),
                eq("/v1/certificate/cacert"),
                eq("GET"),
                isNull(String.class))
        ).thenReturn(respJson);

        String bundle = netki.getCACertBundle();
        assertEquals("CACERT_PEMS", bundle);

        verify(this.mockRequestor, times(1)).processRequest(any(NetkiClient.class), eq("/v1/certificate/cacert"), eq("GET"), isNull(String.class));
    }

    @Test
    public void GetCACertBundle_EmptyResponse() throws Exception {
        NetkiClient netki = new NetkiClient(this.partnerId, this.apiKey, this.apiUrl, this.mockRequestor);
        String respJson = "{\"other\":\"thing\"}";
        when(this.mockRequestor.processRequest(
                any(NetkiClient.class),
                eq("/v1/certificate/cacert"),
                eq("GET"),
                isNull(String.class))
        ).thenReturn(respJson);

        String bundle = netki.getCACertBundle();
        assertEquals("", bundle);

        verify(this.mockRequestor, times(1)).processRequest(any(NetkiClient.class), eq("/v1/certificate/cacert"), eq("GET"), isNull(String.class));
    }

    @Test
    public void GetAccountBalance_GoRight() throws Exception {
        NetkiClient netki = new NetkiClient(this.partnerId, this.apiKey, this.apiUrl, this.mockRequestor);
        String respJson = "{\"available_balance\": 100}";
        when(this.mockRequestor.processRequest(
                any(NetkiClient.class),
                eq("/v1/certificate/balance"),
                eq("GET"),
                isNull(String.class))
        ).thenReturn(respJson);

        Integer balance = netki.getAccountBalance();
        assertEquals(100, balance.intValue());

        verify(this.mockRequestor, times(1)).processRequest(any(NetkiClient.class), eq("/v1/certificate/balance"), eq("GET"), isNull(String.class));
    }

    @Test
    public void GetAccountBalance_EmptyResponse() throws Exception {
        NetkiClient netki = new NetkiClient(this.partnerId, this.apiKey, this.apiUrl, this.mockRequestor);
        String respJson = "{\"other\":\"thing\"}";
        when(this.mockRequestor.processRequest(
                any(NetkiClient.class),
                eq("/v1/certificate/balance"),
                eq("GET"),
                isNull(String.class))
        ).thenReturn(respJson);

        Integer balance = netki.getAccountBalance();
        assertEquals(0, balance.intValue());

        verify(this.mockRequestor, times(1)).processRequest(any(NetkiClient.class), eq("/v1/certificate/balance"), eq("GET"), isNull(String.class));
    }



    @Test
    public void CreatePartner()
    {
        Map<String, Object> postResponse = new HashMap<String, Object>();
        Map<String, String> partnerObj = new HashMap<String, String>();

        partnerObj.put("id", "partnerId");
        partnerObj.put("name", "My Partner");
        postResponse.put("partner", partnerObj);

        try {
            when(this.mockRequestor.processRequest(any(NetkiClient.class), eq("/v1/admin/partner/My Partner"), eq("POST"), isNull(String.class))).thenReturn(mapper.writeValueAsString(postResponse));
        } catch (Exception e) {
            e.printStackTrace();
        }
        NetkiClient netki = new NetkiClient(this.partnerId, this.apiKey, this.apiUrl, this.mockRequestor);

        Partner partner = null;
        try {
            partner = netki.createPartner("My Partner");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Validate Call
        try {
            verify(this.mockRequestor, times(1)).processRequest(any(NetkiClient.class), eq("/v1/admin/partner/My Partner"), eq("POST"), anyString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals("partnerId", partner.getId());
        assertEquals("My Partner", partner.getName());
        assertEquals(netki, partner.getClient());

    }

    @Test
    public void GetPartnersNoPartners()
    {
        Map<String, String> getResponse = new HashMap<String, String>();
        try {
            when(this.mockRequestor.processRequest(any(NetkiClient.class), eq("/v1/admin/partner"), eq("GET"), isNull(String.class))).thenReturn(mapper.writeValueAsString(getResponse));
        } catch (Exception e) {
            e.printStackTrace();
        }
        NetkiClient netki = new NetkiClient(this.partnerId, this.apiKey, this.apiUrl, this.mockRequestor);

        List<Partner> partners = null;
        try {
            partners = netki.getPartners();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Validate Call
        try {
            verify(this.mockRequestor, times(1)).processRequest(any(NetkiClient.class), eq("/v1/admin/partner"), eq("GET"), isNull(String.class));
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(0, partners.size());
    }

    @Test
    public void GetPartnersGoRight()
    {
        Map<String, Object> getResponse = new HashMap<String, Object>();
        Map<String, String> partnerObj = new HashMap<String, String>();

        partnerObj.put("id", "partner_id");
        partnerObj.put("name", "Test Partner");
        getResponse.put("partners", Collections.singletonList(partnerObj));

        try {
            when(this.mockRequestor.processRequest(any(NetkiClient.class), eq("/v1/admin/partner"), eq("GET"), isNull(String.class))).thenReturn(mapper.writeValueAsString(getResponse));
        } catch (Exception e) {
            e.printStackTrace();
        }
        NetkiClient netki = new NetkiClient(this.partnerId, this.apiKey, this.apiUrl, this.mockRequestor);

        List<Partner> partners = null;
        try {
            partners = netki.getPartners();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Validate Call
        try {
            verify(this.mockRequestor, times(1)).processRequest(any(NetkiClient.class), eq("/v1/admin/partner"), eq("GET"), isNull(String.class));
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(1, partners.size());
        assertEquals("partner_id", partners.get(0).getId());
        assertEquals("Test Partner", partners.get(0).getName());
        assertEquals(netki, partners.get(0).getClient());
    }

    @Test
    public void CreateDomainWithSubpartner()
    {
        Map<String, Object> postResponse = new HashMap<String, Object>();
        postResponse.put("status", "completed");
        postResponse.put("nameservers", asList("ns1.domain.com", "ns2.domain.com"));

        try {
            when(this.mockRequestor.processRequest(any(NetkiClient.class), eq("/v1/partner/domain/domain.com"), eq("POST"), anyString())).thenReturn(mapper.writeValueAsString(postResponse));
        } catch (Exception e) {
            e.printStackTrace();
        }
        NetkiClient netki = new NetkiClient(this.partnerId, this.apiKey, this.apiUrl, this.mockRequestor);

        Partner partner = new Partner();
        partner.setId("SubPartnerId");
        Domain domain = null;
        try {
            domain = netki.createDomain("domain.com", partner);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Validate Call
        try {
            verify(this.mockRequestor, times(1)).processRequest(any(NetkiClient.class), eq("/v1/partner/domain/domain.com"), eq("POST"), eq("{\"partner_id\":\"SubPartnerId\"}"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals("domain.com", domain.getName());
        assertEquals("completed", domain.getStatus());
        assertTrue(domain.getNameservers().contains("ns1.domain.com"));
        assertTrue(domain.getNameservers().contains("ns2.domain.com"));
    }

    @Test
    public void CreateDomainWithoutSubpartner()
    {
        Map<String, Object> postResponse = new HashMap<String, Object>();
        postResponse.put("status", "completed");
        postResponse.put("nameservers", asList("ns1.domain.com", "ns2.domain.com"));

        try {
            when(this.mockRequestor.processRequest(any(NetkiClient.class), eq("/v1/partner/domain/domain.com"), eq("POST"), anyString())).thenReturn(mapper.writeValueAsString(postResponse));
        } catch (Exception e) {
            e.printStackTrace();
        }

        NetkiClient netki = new NetkiClient(this.partnerId, this.apiKey, this.apiUrl, this.mockRequestor);

        Domain domain = null;
        try {
            domain = netki.createDomain("domain.com", null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Validate Call
        try {
            verify(this.mockRequestor, times(1)).processRequest(any(NetkiClient.class), eq("/v1/partner/domain/domain.com"), eq("POST"), isNull(String.class));
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals("domain.com", domain.getName());
        assertEquals("completed", domain.getStatus());
        assertTrue(domain.getNameservers().contains("ns1.domain.com"));
        assertTrue(domain.getNameservers().contains("ns2.domain.com"));
    }

    @Test
    public void GetDomainsNull()
    {
        Map<String, String> getResponse = new HashMap<String, String>();
        try {
            when(this.mockRequestor.processRequest(any(NetkiClient.class), eq("/api/domain"), eq("GET"), isNull(String.class))).thenReturn(mapper.writeValueAsString(getResponse));
        } catch (Exception e) {
            e.printStackTrace();
        }
        NetkiClient netki = new NetkiClient(this.partnerId, this.apiKey, this.apiUrl, this.mockRequestor);

        List<Domain> result = null;
        try {
            result = netki.getDomains();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Validate Call
        try {
            verify(this.mockRequestor, times(1)).processRequest(any(NetkiClient.class), eq("/api/domain"), eq("GET"), isNull(String.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(0, result.size());
    }

    @Test
    public void GetDomains()
    {

        // Setup Domain Data Loads
        Map<String, Object> domainStatusData = new HashMap<String, Object>();
        domainStatusData.put("status", "status");
        domainStatusData.put("delegation_status", true);
        domainStatusData.put("delegation_message", "delegated");
        domainStatusData.put("wallet_name_count", 42);

        Map<String, Object> domainDnssecStatusData = new HashMap<String, Object>();
        domainDnssecStatusData.put("public_key_signing_key", "PUBKEY");
        domainDnssecStatusData.put("nextroll_date", "1980-06-13 01:02:03");
        domainDnssecStatusData.put("ds_records", asList("DS1", "DS2"));
        domainDnssecStatusData.put("nameservers", asList("ns1.domain.com", "ns2.domain.com"));

        try {
            when(this.mockRequestor.processRequest(any(NetkiClient.class), eq("/v1/partner/domain/domain.com"), eq("GET"), isNull(String.class))).thenReturn(mapper.writeValueAsString(domainStatusData));
            when(this.mockRequestor.processRequest(any(NetkiClient.class), eq("/v1/partner/domain/dnssec/domain.com"), eq("GET"), isNull(String.class))).thenReturn(mapper.writeValueAsString(domainDnssecStatusData));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Setup GetDomain Response Data
        Map<String, Object> getResponse = new HashMap<String, Object>();
        Map<String, String> domainObj = new HashMap<String, String>();

        domainObj.put("domain_name", "domain.com");
        getResponse.put("domains", Collections.singletonList(domainObj));

        try {
            when(this.mockRequestor.processRequest(any(NetkiClient.class), eq("/api/domain"), eq("GET"), isNull(String.class))).thenReturn(mapper.writeValueAsString(getResponse));
        } catch (Exception e) {
            e.printStackTrace();
        }
        NetkiClient netki = new NetkiClient(this.partnerId, this.apiKey, this.apiUrl, this.mockRequestor);

        List<Domain> result = null;
        try {
            result = netki.getDomains();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Validate Call
        try {
            verify(this.mockRequestor, times(1)).processRequest(any(NetkiClient.class), eq("/api/domain"), eq("GET"), isNull(String.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(1, result.size());
        assertEquals("domain.com", result.get(0).getName());
        assertEquals(netki, result.get(0).getClient());
    }

}
