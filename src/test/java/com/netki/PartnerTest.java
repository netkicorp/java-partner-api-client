package com.netki;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Partner.class)
public class PartnerTest {

    Requestor mockRequestor;

    @Before
    public void setUp() {
        this.mockRequestor = mock(Requestor.class);
        try {
            when(this.mockRequestor.processRequest(any(NetkiClient.class), anyString(), anyString(), anyString())).thenReturn("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        reset(this.mockRequestor);
    }

    @Test
    public void PartnerCreate()
    {
        Partner partner = new Partner("id", "name");
        assertEquals("id", partner.getId());
        assertEquals("name", partner.getName());
    }

    @Test
    public void PartnerDelete()
    {

        // Setup Partner
        NetkiClient client = new NetkiClient("partner_id", "api_key", "http://server");
        Partner partner = new Partner(this.mockRequestor);
        partner.setId("id");
        partner.setName("name");
        partner.setClient(client);

        try {
            partner.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Validate Call
        try {
            verify(this.mockRequestor, times(1)).processRequest(any(NetkiClient.class), eq("/v1/admin/partner/name"), eq("DELETE"), anyString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
