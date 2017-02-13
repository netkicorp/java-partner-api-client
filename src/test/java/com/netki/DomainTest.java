package com.netki;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Domain.class)
public class DomainTest {

    private Requestor mockRequestor;

    @Before
    public void setUp() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        String domainStatusJson;
        String domainDnssecStatusJson;

        this.mockRequestor = mock(Requestor.class);

        Map<String, Object> domainStatusData = new HashMap<String, Object>();
        domainStatusData.put("status", "status");
        domainStatusData.put("delegation_status", true);
        domainStatusData.put("delegation_message", "delegated");
        domainStatusData.put("wallet_name_count", 42);

        Map<String, Object> domainDnssecStatusData = new HashMap<String, Object>();
        domainDnssecStatusData.put("public_key_signing_key", "PUBKEY");
        domainDnssecStatusData.put("nextroll_date", "1980-06-13 01:02:03");
        domainDnssecStatusData.put("ds_records", Arrays.asList("DS1", "DS2"));
        domainDnssecStatusData.put("nameservers", Arrays.asList("ns1.domain.com", "ns2.domain.com"));

        try {
            domainStatusJson = mapper.writeValueAsString(domainStatusData);
            domainDnssecStatusJson = mapper.writeValueAsString(domainDnssecStatusData);
        } catch (IOException e) {
            throw new Exception("TestDomain Setup Failed");
        }

        when(this.mockRequestor.processRequest(any(NetkiClient.class), eq("/v1/partner/domain/domain.com"), eq("GET"), anyString())).thenReturn(domainStatusJson);
        when(this.mockRequestor.processRequest(any(NetkiClient.class), eq("/v1/partner/domain/dnssec/domain.com"), eq("GET"), anyString())).thenReturn(domainDnssecStatusJson);
        when(this.mockRequestor.processRequest(any(NetkiClient.class), eq("/v1/partner/domain/domain.com"), eq("DELETE"), anyString())).thenReturn("");

    }

    @After
    public void tearDown() {
        reset(this.mockRequestor);
    }

    @Test
    public void DomainCreate() {
        Domain domain = new Domain("domain.com");
        assertEquals("domain.com", domain.getName());
    }

    @Test
    public void DomainDelete()
    {

        // Setup Domain
        NetkiClient client = new NetkiClient("partner_id", "api_key", "http://server");
        Domain domain = new Domain("domain.com", this.mockRequestor);
        domain.setClient(client);

        try {
            domain.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Validate Call
        try {
            verify(this.mockRequestor, times(1)).processRequest(any(NetkiClient.class), eq("/v1/partner/domain/domain.com"), eq("DELETE"), anyString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void LoadStatus()
    {

        // Setup Domain
        NetkiClient client = new NetkiClient("partner_id", "api_key", "http://server");
        Domain domain = new Domain("domain.com", this.mockRequestor);
        domain.setClient(client);

        try {
            domain.loadStatus();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Validate Call
        try {
            verify(this.mockRequestor, times(1)).processRequest(any(NetkiClient.class), eq("/v1/partner/domain/domain.com"), eq("GET"), anyString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Validate domain status data
        assertEquals("status", domain.getStatus());
        assertTrue(domain.getDelegationStatus());
        assertEquals("delegated", domain.getDelegationMessage());
        assertEquals(42, domain.getWalletNameCount());
    }

    @Test
    public void LoadDnssecDetails()
    {

        // Setup Domain
        NetkiClient client = new NetkiClient("partner_id", "api_key", "http://server");
        Domain domain = new Domain("domain.com", this.mockRequestor);
        domain.setClient(client);

        try {
            domain.loadDnssecDetails();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Validate Call
        try {
            verify(this.mockRequestor, times(1)).processRequest(any(NetkiClient.class), eq("/v1/partner/domain/dnssec/domain.com"), eq("GET"), anyString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Validate domain status data
        assertEquals("PUBKEY", domain.getPublicKeySigningKey());

        assertEquals(2, domain.getDsRecords().size());
        assertTrue(domain.getDsRecords().contains("DS1"));
        assertTrue(domain.getDsRecords().contains("DS2"));

        assertEquals(2, domain.getNameservers().size());
        assertTrue(domain.getNameservers().contains("ns1.domain.com"));
        assertTrue(domain.getNameservers().contains("ns2.domain.com"));

        Calendar cal = Calendar.getInstance();
        cal.set(1980, Calendar.JUNE, 13, 1, 2, 3);
        cal.clear(Calendar.MILLISECOND);

        assertEquals(cal.getTime(), domain.getNextRoll());
    }
}
