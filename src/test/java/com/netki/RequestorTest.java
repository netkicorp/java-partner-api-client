package com.netki;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.google.api.client.http.HttpStatusCodes;
import com.google.common.io.BaseEncoding;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.*;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.security.*;
import java.util.*;

import static com.netki.TestUtil.generateKey;
import static org.junit.Assert.*;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@PowerMockIgnore("javax.net.ssl.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest(Requestor.class)
public class RequestorTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9191);
    private ObjectMapper mapper = new ObjectMapper();
    private static KeyPair userKeyPair = generateKey("ecdsa");

    public void setupHttpStub(String endpoint, RequestMethod method, int statusCode, String responseData) {

        stubFor(new MappingBuilder(method, urlEqualTo(endpoint))
                .withHeader("Authorization", equalTo("api_key"))
                .withHeader("X-Partner-ID", equalTo("partner_id"))
                .willReturn(
                        aResponse()
                                .withStatus(statusCode)
                                .withHeader("Content-Type", "application/json")
                                .withBody(responseData)
                )
        );
    }

    public void setupHttpStubDistributedAccess(String endpoint, RequestMethod method, int statusCode, String responseData) {
        stubFor(new MappingBuilder(method, urlEqualTo(endpoint))
                .withHeader("X-IdentityDocument", equalTo(BaseEncoding.base16().encode(RequestorTest.userKeyPair.getPublic().getEncoded())))
                .withHeader("X-Partner-Key", equalTo("partner_ksk_hex"))
                .withHeader("X-Partner-KeySig", equalTo("partner_ksk_sig_hex"))
                .willReturn(
                        aResponse()
                                .withStatus(statusCode)
                                .withHeader("Content-Type", "application/json")
                                .withBody(responseData)
                )
        );
    }

    // ProcessResquest Tests
    @Test
    public void ProcessRequestGetSuccessGoRight()
    {
        Map<String, Boolean> respData = new HashMap<String, Boolean>();
        respData.put("success", true);

        try {
            this.setupHttpStub("/endpoint", RequestMethod.GET, HttpStatusCodes.STATUS_CODE_OK, this.mapper.writeValueAsString(respData));

            NetkiClient client = new NetkiClient("partner_id", "api_key", "http://localhost:9191");
            Requestor requestor = new Requestor();
            String returnData = requestor.processRequest(client, "/endpoint", "GET", null);

            verify(getRequestedFor(urlMatching("/endpoint")).withRequestBody(matching("")));

            JsonNode assertData = this.mapper.readTree(returnData);
            assertTrue(assertData.get("success").asBoolean());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ProcessRequest Distributed API Access
    @Test
    public void ProcessRequestDistributedAPIAccess()
    {
        Map<String, Boolean> respData = new HashMap<String, Boolean>();
        respData.put("success", true);

        try {
            this.setupHttpStubDistributedAccess("/endpoint", RequestMethod.GET, HttpStatusCodes.STATUS_CODE_OK, this.mapper.writeValueAsString(respData));

            NetkiClient client = new NetkiClient("partner_ksk_hex", "partner_ksk_sig_hex", RequestorTest.userKeyPair, "http://localhost:9191");
            Requestor requestor = new Requestor();
            String returnData = requestor.processRequest(client, "/endpoint", "GET", null);

            verify(getRequestedFor(urlMatching("/endpoint")).withRequestBody(matching("")));

            JsonNode assertData = this.mapper.readTree(returnData);
            assertTrue(assertData.get("success").asBoolean());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void ProcessRequestDeleteNoContent()
    {

        try {
            this.setupHttpStub("/endpoint", RequestMethod.DELETE, HttpStatusCodes.STATUS_CODE_NO_CONTENT, "");

            NetkiClient client = new NetkiClient("partner_id", "api_key", "http://localhost:9191");
            Requestor requestor = new Requestor();
            String returnData = requestor.processRequest(client, "/endpoint", "DELETE", null);

            verify(deleteRequestedFor(urlMatching("/endpoint")).withRequestBody(matching("")));
            assertEquals("", returnData);

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void ProcessRequestPostSuccessGoRight()
    {
        Map<String, Boolean> respData = new HashMap<String, Boolean>();
        respData.put("success", true);

        try {
            this.setupHttpStub("/endpoint", RequestMethod.POST, HttpStatusCodes.STATUS_CODE_OK, this.mapper.writeValueAsString(respData));

            NetkiClient client = new NetkiClient("partner_id", "api_key", "http://localhost:9191");
            Requestor requestor = new Requestor();
            String returnData = requestor.processRequest(client, "/endpoint", "POST", "post data");

            verify(postRequestedFor(urlMatching("/endpoint")).withRequestBody(matching("post data")));
            JsonNode assertData = this.mapper.readTree(returnData);
            assertTrue(assertData.get("success").asBoolean());

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void ProcessRequestGetBasicError()
    {
        Map<String, Object> respData = new HashMap<String, Object>();
        respData.put("success", false);
        respData.put("message", "failure message");

        try {
            this.setupHttpStub("/endpoint", RequestMethod.GET, HttpStatusCodes.STATUS_CODE_OK, this.mapper.writeValueAsString(respData));

            NetkiClient client = new NetkiClient("partner_id", "api_key", "http://localhost:9191");
            Requestor requestor = new Requestor();

            try
            {
                requestor.processRequest(client, "/endpoint", "GET", null);
                fail();
            } catch (Exception e) {
                assertEquals("failure message", e.getMessage());
            }

            verify(getRequestedFor(urlMatching("/endpoint")).withRequestBody(matching("")));

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void ProcessRequestGetBasicErrorNotFoundCode()
    {
        Map<String, Object> respData = new HashMap<String, Object>();
        respData.put("success", true);
        respData.put("message", "failure message");

        try {
            this.setupHttpStub("/endpoint", RequestMethod.GET, HttpStatusCodes.STATUS_CODE_NOT_FOUND, this.mapper.writeValueAsString(respData));

            NetkiClient client = new NetkiClient("partner_id", "api_key", "http://localhost:9191");
            Requestor requestor = new Requestor();
            try
            {
                requestor.processRequest(client, "/endpoint", "GET", null);
                fail();
            }
            catch (Exception e)
            {
                assertEquals("failure message", e.getMessage());
            }

            verify(getRequestedFor(urlMatching("/endpoint")).withRequestBody(matching("")));

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void ProcessRequestGetFailureList()
    {
        Map<String, Object> respData = new HashMap<String, Object>();
        Map<String, String> f1 = new HashMap<String, String>();
        Map<String, String> f2 = new HashMap<String, String>();
        Map<String, String> f3 = new HashMap<String, String>();

        f1.put("message", "fail1");
        f2.put("message", "fail2");
        f3.put("message", "fail3");

        respData.put("success", false);
        respData.put("message", "failure message");
        respData.put("failures", Arrays.asList(f1, f2, f3));

        try {
            this.setupHttpStub("/endpoint", RequestMethod.GET, HttpStatusCodes.STATUS_CODE_OK, this.mapper.writeValueAsString(respData));

            NetkiClient client = new NetkiClient("partner_id", "api_key", "http://localhost:9191");
            Requestor requestor = new Requestor();
            try
            {
                requestor.processRequest(client, "/endpoint", "GET", null);
                fail();
            }
            catch (Exception e)
            {
                assertEquals("failure message [FAILURES: fail1,fail2,fail3]", e.getMessage());
            }

            verify(getRequestedFor(urlMatching("/endpoint")).withRequestBody(matching("")));

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

}