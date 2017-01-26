package com.netki;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.io.IOException;
import java.io.StringWriter;
import java.security.Security;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.api.client.util.Joiner;
import com.google.common.io.BaseEncoding;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.spongycastle.jce.provider.BouncyCastleProvider;

/**
 * Make and process HTTP calls to the Netki API
 */
public class Requestor {

    static final private HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /**
     *
     * Process Netki API request and response
     *
     * @param nkClient NetkiClient
     * @param uri Netki Partner URI (i.e., /v1/partner/walletname)
     * @param method HTTP Method
     * @param data POST/PUT Data
     * @return API Response Content
     * @throws Exception Occurs on Bad HTTP Request / Response
     */
    public String processRequest(NetkiClient nkClient, String uri, String method, String data) throws Exception {

        List<String> supportedMethods = new ArrayList<String>(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        if (!supportedMethods.contains(method)) {
            throw new Exception("Unsupported HTTP Method: " + method);
        }

        HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory();
        HttpContent content = null;

        if (data != null) {
            content = new ByteArrayContent("application/json", data.getBytes());

        }

        HttpRequest request = requestFactory.buildRequest(method.toUpperCase(), new GenericUrl(nkClient.getApiUrl() + uri), content);

        if(nkClient.getApiKey() != null && nkClient.getPartnerId() != null) {

            // Traditional PartnerID & API Key
            request.getHeaders().set("Authorization", Collections.singletonList(nkClient.getApiKey()));
            request.getHeaders().set("X-Partner-ID", Collections.singletonList(nkClient.getPartnerId()));

        } else if(nkClient.getPartnerKskHex() != null && nkClient.getPartnerKskSigHex() != null && nkClient.getUserKey() != null) {

            Security.addProvider(new BouncyCastleProvider());

            // Distributed API Access
            byte[] dataByteArray = new byte[0];
            byte[] urlByteArray = request.getUrl().toString().getBytes();
            if(data != null) {
                dataByteArray = data.getBytes();
            }

            byte[] sigData = new byte[urlByteArray.length + dataByteArray.length];
            System.arraycopy(urlByteArray, 0, sigData, 0, urlByteArray.length);
            System.arraycopy(dataByteArray, 0, sigData, urlByteArray.length, dataByteArray.length);

            Signature ecdsaSig;
            ecdsaSig = Signature.getInstance("SHA256withECDSA", "SC");
            ecdsaSig.initSign(nkClient.getUserKey().getPrivate());

            ecdsaSig.update(sigData);
            byte[] sig = ecdsaSig.sign();

            // Set Header Values
            String encodedPKString = BaseEncoding.base16().encode(nkClient.getUserKey().getPublic().getEncoded());
            String encodedSig = BaseEncoding.base16().encode(sig);

            request.getHeaders().set("X-Partner-Key", nkClient.getPartnerKskHex());
            request.getHeaders().set("X-Partner-KeySig", nkClient.getPartnerKskSigHex());
            request.getHeaders().set("X-Identity", encodedPKString);
            request.getHeaders().set("X-Signature", encodedSig);

        } else {
            throw new Exception("Invalid Access Type Defined");
        }

        HttpResponse response = null;
        HttpResponseException errorResponse = null;
        try {
            response = request.execute();
        } catch(IOException e)
        {
            if(e instanceof HttpResponseException) {
                errorResponse = (HttpResponseException)e;
            } else {
                throw new Exception("HTTP Request Failed: " + e.getMessage());
            }
        }

        int statusCode = errorResponse != null ? errorResponse.getStatusCode() : response.getStatusCode();
        if (method.equals("DELETE") && statusCode == HttpStatusCodes.STATUS_CODE_NO_CONTENT) {
            return "";
        }

        String responseString;
        if(errorResponse != null) {
            responseString = errorResponse.getContent();
        } else {
            StringWriter writer = new StringWriter();
            IOUtils.copy(response.getContent(), writer, "utf8");
            responseString = writer.toString();
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode retData = mapper.readTree(responseString);

        if (statusCode >= HttpStatusCodes.STATUS_CODE_MULTIPLE_CHOICES || !retData.get("success").asBoolean()) {

            String errorMessage = retData.get("message").asText();

            if (retData.get("failures") != null && retData.get("failures").isArray()) {
                List<String> failures = new ArrayList<String>();

                for(JsonNode node : retData.get("failures")) {
                    failures.add(node.get("message").asText());
                }

                errorMessage = errorMessage + " [FAILURES: " + Joiner.on(',').join(failures) + "]";
            }

            throw new Exception(errorMessage);
        }

        return responseString;
    }

}
