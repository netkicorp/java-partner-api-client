package com.netki;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static java.util.Arrays.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

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
    public void NetkiInstantiation()
    {
        NetkiClient netki = new NetkiClient(this.partnerId, this.apiKey, this.apiUrl);
        assertNotNull(netki);
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
        assertEquals(this.partnerId, walletName.getNkClient().getPartnerId());
        assertEquals(this.apiKey, walletName.getNkClient().getApiKey());
        assertEquals(this.apiUrl, walletName.getNkClient().getApiUrl());
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
        assertEquals(this.partnerId, partner.getNkClient().getPartnerId());
        assertEquals(this.apiKey, partner.getNkClient().getApiKey());
        assertEquals(this.apiUrl, partner.getNkClient().getApiUrl());

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
        assertEquals(this.partnerId, partners.get(0).getNkClient().getPartnerId());
        assertEquals(this.apiKey, partners.get(0).getNkClient().getApiKey());
        assertEquals(this.apiUrl, partners.get(0).getNkClient().getApiUrl());
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
        assertEquals(this.partnerId, result.get(0).getNkClient().getPartnerId());
        assertEquals(this.apiKey, result.get(0).getNkClient().getApiKey());
        assertEquals(this.apiUrl, result.get(0).getNkClient().getApiUrl());
    }

}
