package com.netki;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WalletName.class)
public class WalletNameTest {

    private Requestor mockRequestor;
    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        this.mockRequestor = mock(Requestor.class);
    }

    @After
    public void tearDown() {
        reset(this.mockRequestor);
    }

    @Test
    public void WalletNameAccessorsTest()
    {
        WalletName walletName = new WalletName();
        assertNull(walletName.getId());
        assertNull(walletName.getName());
        assertNull(walletName.getDomainName());
        assertNull(walletName.getExternalId());

        // Validate Empty Getter Returns
        assertEquals(0, walletName.getUsedCurrencies().size());
        assertNull(walletName.getWalletAddress("btc"));

        // Set Currency Address
        walletName.setCurrencyAddress("btc", "1btcaddress");
        assertEquals("1btcaddress", walletName.getWalletAddress("btc"));

        // Remove Currency Address
        walletName.removeCurrencyAddress("btc");
        assertNull(walletName.getWalletAddress("btc"));
    }

    @Test
    public void TestSaveNewMatchingReturnData()
    {
        Map<String, String> retWallet = new HashMap<String, String>();
        retWallet.put("name", "wallet");
        retWallet.put("domain_name", "domain.com");
        retWallet.put("id", "new_id");

        List<Object> retList = new ArrayList<Object>();
        retList.add(retWallet);

        Map<String, Object> retData = new HashMap<String, Object>();
        retData.put("wallet_names", retList);

        try {
            when(mockRequestor.processRequest(any(NetkiClient.class), eq("/v1/partner/walletname"), eq("POST"), anyString())).thenReturn(mapper.writeValueAsString(retData));
        } catch (Exception e) {
            e.printStackTrace();
        }

        NetkiClient client = new NetkiClient("api_key", "partner_id", "http://server");

        WalletName walletName = new WalletName(this.mockRequestor);
        walletName.setClient(client);
        walletName.setDomainName("domain.com");
        walletName.setName("wallet");
        walletName.setExternalId("external_id");
        walletName.setCurrencyAddress("btc", "1btcaddress");

        try {
            walletName.save();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Validate Call
        try {
            verify(this.mockRequestor, times(1)).processRequest(any(NetkiClient.class), eq("/v1/partner/walletname"), eq("POST"), anyString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Validate ID
        assertEquals("new_id", walletName.getId());
    }

    @Test
    public void TestSaveNewNoMatchReturnData()
    {
        Map<String, String> retWallet = new HashMap<String, String>();
        retWallet.put("name", "wrongwallet");
        retWallet.put("domain_name", "domain.com");
        retWallet.put("id", "new_id");

        List<Object> retList = new ArrayList<Object>();
        retList.add(retWallet);

        Map<String, Object> retData = new HashMap<String, Object>();
        retData.put("wallet_names", retList);

        try {
            when(this.mockRequestor.processRequest(any(NetkiClient.class), eq("/v1/partner/walletname"), eq("POST"), anyString())).thenReturn(mapper.writeValueAsString(retData));
        } catch (Exception e) {
            e.printStackTrace();
        }

        NetkiClient client = new NetkiClient("api_key", "partner_id", "http://server");

        WalletName walletName = new WalletName(this.mockRequestor);
        walletName.setClient(client);
        walletName.setDomainName("domain.com");
        walletName.setName("wallet");
        walletName.setExternalId("external_id");
        walletName.setCurrencyAddress("btc", "1btcaddress");

        try {
            walletName.save();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Validate Call
        try {
            verify(this.mockRequestor, times(1)).processRequest(any(NetkiClient.class), eq("/v1/partner/walletname"), eq("POST"), anyString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Validate ID
        assertNull(walletName.getId());
    }

    @Test
    public void TestSaveExisting()
    {
        Map<String, String> retWallet = new HashMap<String, String>();
        retWallet.put("name", "wallet");
        retWallet.put("domain_name", "domain.com");
        retWallet.put("id", "new_id");

        List<Object> retList = new ArrayList<Object>();
        retList.add(retWallet);

        Map<String, Object> retData = new HashMap<String, Object>();
        retData.put("wallet_names", retList);

        try {
            when(this.mockRequestor.processRequest(any(NetkiClient.class), eq("/v1/partner/walletname"), eq("PUT"), anyString())).thenReturn(mapper.writeValueAsString(retData));
        } catch (Exception e) {
            e.printStackTrace();
        }

        NetkiClient client = new NetkiClient("api_key", "partner_id", "http://server");

        WalletName walletName = new WalletName(this.mockRequestor);
        walletName.setClient(client);
        walletName.setDomainName("domain.com");
        walletName.setName("wallet");
        walletName.setExternalId("external_id");
        walletName.setId("id");
        walletName.setCurrencyAddress("btc", "1btcaddress");

        try {
            walletName.save();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Validate Call
        try {
            verify(this.mockRequestor, times(1)).processRequest(any(NetkiClient.class), eq("/v1/partner/walletname"), eq("PUT"), anyString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Validate ID
        assertEquals("id", walletName.getId());
    }

    @Test
    public void TestDeleteMissingId()
    {
        WalletName walletName = new WalletName();
        try
        {
            walletName.delete();
            fail();
        } catch (Exception e) {
            assertEquals("Unable to Delete Object that Does Not Exist Remotely", e.getMessage());
        }

    }

    @Test
    public void TestDelete()
    {
        NetkiClient client = new NetkiClient("api_key", "partner_id", "http://server");

        WalletName walletName = new WalletName(this.mockRequestor);
        walletName.setDomainName("domain.com");
        walletName.setId("id");
        walletName.setClient(client);

        try {
            when(this.mockRequestor.processRequest(any(NetkiClient.class), eq("/v1/partner/walletname/domain.com/id"), eq("DELETE"), isNull(String.class))).thenReturn("");
            walletName.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Validate Call
        try {
            String nullString = null;
            verify(this.mockRequestor, times(1)).processRequest(any(NetkiClient.class), eq("/v1/partner/walletname/domain.com/id"), eq("DELETE"), eq(nullString));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
