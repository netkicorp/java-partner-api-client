package com.netki;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.api.client.util.Joiner;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Make and process HTTP calls to the Netki API
 */
public class Requestor {

    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /**
     *
     * Process Netki API request and response
     *
     * @param apiKey Netki Partner API Key
     * @param partnerId Netki Partner ID
     * @param uri Netki Partner URI Base (i.e., https://api.netki.com)
     * @param method HTTP Method
     * @param data POST/PUT Data
     * @return API Response Content
     * @throws Exception Occurs on Bad HTTP Request / Response
     */
    public String processRequest(String apiKey, String partnerId, String uri, String method, String data) throws Exception {

        List<String> supportedMethods = new ArrayList<String>(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        if (!supportedMethods.contains(method)) {
            throw new Exception("Unsupported HTTP Method: " + method);
        }

        HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory();
        HttpContent content = null;

        if (data != null) {
            content = new ByteArrayContent("application/json", data.getBytes());

        }

        HttpRequest request = requestFactory.buildRequest(method.toUpperCase(), new GenericUrl(uri), content);
        request.getHeaders().set("Authorization", Collections.singletonList(apiKey));
        request.getHeaders().set("X-Partner-ID", Collections.singletonList(partnerId));

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
