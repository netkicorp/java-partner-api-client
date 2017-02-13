package com.netki;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.spongycastle.asn1.ASN1Encodable;
import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x500.X500NameBuilder;
import org.spongycastle.asn1.x509.*;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.ContentVerifierProvider;
import org.spongycastle.pkcs.PKCS10CertificationRequest;
import org.spongycastle.pkcs.PKCS10CertificationRequestBuilder;

import java.security.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static com.netki.TestUtil.generateKey;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@PowerMockIgnore({"javax.*", "org.spongycastle.*", "org.mockito.*", "com.madgag.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({Certificate.class, SubjectPublicKeyInfo.class, PKCS10CertificationRequestBuilder.class})
public class CertificateTest {

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

    // Build Customer Data
    private CustomerData buildCustomerData() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");

        CustomerData cd = new CustomerData();
        cd.setCity("Los Angeles");
        cd.setCountry("US");
        cd.setFirstName("Testy");
        cd.setLastName("Testerson");
        cd.setStreetAddress("123 Main St.");
        cd.setPostalCode("11111");
        cd.setOrganizationName("Netki, Inc.");
        cd.setEmail("user@domain.com");
        cd.setSsn("1234567890");
        cd.setPhone("+18182234567");

        IdentityDocument id1 = new IdentityDocument();
        id1.setIdentity("12345678");
        id1.setType("drivers licence");
        id1.setState("CA");

        IdentityDocument id2 = new IdentityDocument();
        id2.setIdentity("P12345678");
        id2.setType("passport");
        id2.setState("CA");
        id2.setDlRtaNumber("12345");

        try {
            cd.setDob(sdf.parse("1980-04-02"));
            id1.setExpiration(sdf.parse("2030-01-02"));
            id2.setExpiration(sdf.parse("2031-07-12"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        cd.addIdentity(id1);
        cd.addIdentity(id2);

        return cd;
    }

    @Test
    public void GenerateCsr_GoRight() throws Exception {

        KeyPair keyPair = generateKey("RSA");

        // Setup Mocks
        X500NameBuilder mockBuilder = spy(X500NameBuilder.class);
        SubjectPublicKeyInfo mockSubjectPublicKeyInfo = mock(SubjectPublicKeyInfo.class);
        PKCS10CertificationRequestBuilder mockCsrBuilder = mock(PKCS10CertificationRequestBuilder.class);
        ExtensionsGenerator mockExtGenerator = spy(ExtensionsGenerator.class);
        PKCS10CertificationRequest mockReq = mock(PKCS10CertificationRequest.class);

        whenNew(X500NameBuilder.class).withAnyArguments().thenReturn(mockBuilder);
        whenNew(PKCS10CertificationRequestBuilder.class).withArguments(any(X500Name.class), any(SubjectPublicKeyInfo.class)).thenReturn(mockCsrBuilder);
        whenNew(ExtensionsGenerator.class).withNoArguments().thenReturn(mockExtGenerator);

        PowerMockito.mockStatic(SubjectPublicKeyInfo.class);
        when(SubjectPublicKeyInfo.getInstance(Matchers.anyObject())).thenReturn(mockSubjectPublicKeyInfo);

        when(mockCsrBuilder.build(any(ContentSigner.class))).thenReturn(mockReq);
        when(mockReq.isSignatureValid(any(ContentVerifierProvider.class))).thenReturn(true);
        when(mockReq.getEncoded()).thenReturn("TEST".getBytes());

        // Setup Certificate Object
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
        Certificate cert = new Certificate();
        cert.setProductId("product_id");
        cert.setCustomerData(buildCustomerData());

        // Run Method under test
        String resultPem = null;
        try {
            resultPem = cert.generateCsr(keyPair);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertEquals("-----BEGIN CERTIFICATE REQUEST-----\nVEVTVA==\n-----END CERTIFICATE REQUEST-----\n", resultPem);

        // Verify Calls
        verify(mockBuilder, times(6)).addRDN(any(ASN1ObjectIdentifier.class), anyString());

        verifyStatic(times(1));
        SubjectPublicKeyInfo.getInstance(eq(keyPair.getPublic().getEncoded()));

        KeyUsage expectedKU = new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment | KeyUsage.nonRepudiation);
        BasicConstraints expectedBC = new BasicConstraints(false);

        verify(mockExtGenerator, times(1)).addExtension(eq(Extension.basicConstraints), eq(false), eq(expectedBC));
        verify(mockExtGenerator, times(1)).addExtension(eq(Extension.keyUsage), eq(false), eq(expectedKU));
        verify(mockExtGenerator, times(1)).generate();

        verify(mockCsrBuilder, times(1)).addAttribute(any(ASN1ObjectIdentifier.class), any(ASN1Encodable.class));
        verify(mockCsrBuilder, times(1)).build(any(ContentSigner.class));

        verify(mockReq, times(1)).isSignatureValid(any(ContentVerifierProvider.class));

    }

    @Test
    public void GenerateCsr_BadKey() throws Exception {

        KeyPair keyPair = generateKey("ECDSA");

        try {
            Certificate cert = new Certificate();
            cert.generateCsr(keyPair);
            fail("NoSuchAlgorithmException Expected");
        } catch(NoSuchAlgorithmException nsae) {
            assertEquals("RSA KeyPair Required", nsae.getMessage());
        }
    }

    @Test
    public void GenerateCsr_BadSignature() throws Exception {

        KeyPair keyPair = generateKey("RSA");

        // Setup Mocked PKCS10CertificationRequest
        PKCS10CertificationRequest mockReq = mock(PKCS10CertificationRequest.class);
        PKCS10CertificationRequestBuilder mockCsrBuilder = mock(PKCS10CertificationRequestBuilder.class);
        whenNew(PKCS10CertificationRequestBuilder.class).withArguments(any(X500Name.class), any(SubjectPublicKeyInfo.class)).thenReturn(mockCsrBuilder);
        when(mockCsrBuilder.build(any(ContentSigner.class))).thenReturn(mockReq);
        when(mockReq.isSignatureValid(any(ContentVerifierProvider.class))).thenReturn(false);

        Certificate cert = new Certificate();
        cert.setCustomerData(buildCustomerData());

        try {
            cert.generateCsr(keyPair);
            fail("SignatureException Expected");
        } catch(SignatureException se) {
            assertEquals("CSR Signature Failure", se.getMessage());
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void SubmitUserData_GoRight() throws Exception {

        Certificate cert = new Certificate(this.mockRequestor);
        cert.setClient(new NetkiClient("partnerId", "apiKey", null));
        cert.setCustomerData(buildCustomerData());
        cert.setProductId("product_id");

        Map<String, Object> retData = new HashMap<String, Object>();
        retData.put("token", "data");
        when(this.mockRequestor.processRequest(any(NetkiClient.class), eq("/v1/certificate/token"), eq("POST"), anyString())).thenReturn(mapper.writeValueAsString(retData));

        assertNull(cert.getDataToken());
        cert.submitUserData();
        verify(this.mockRequestor, times(1)).processRequest(
                any(NetkiClient.class),
                eq("/v1/certificate/token"),
                eq("POST"),
                eq("{\"street_address\":\"123 Main St.\",\"country\":\"US\",\"product\":\"product_id\",\"identity_\":\"12345678\",\"city\":\"Los Angeles\",\"last_name\":\"Testerson\",\"identity_state\":\"CA\",\"identity_expiration\":\"2030-01-02\",\"ssn\":\"1234567890\",\"identity_state2\":\"CA\",\"phone\":\"+18182234567\",\"dob\":\"1980-04-02\",\"identity_type2\":\"passport\",\"postal_code\":\"11111\",\"identity_type\":\"drivers licence\",\"first_name\":\"Testy\",\"identity_expiration2\":\"2031-07-12\",\"email\":\"user@domain.com\",\"identity_2\":\"P12345678\",\"identity_dl_rta_number2\":\"12345\"}")
        );

        assertEquals("data", cert.getDataToken());
    }

    @Test
    public void SubmitUserData_MissingToken() throws Exception {

        Certificate cert = new Certificate(this.mockRequestor);
        cert.setClient(new NetkiClient("partnerId", "apiKey", null));
        cert.setCustomerData(buildCustomerData());
        cert.setProductId("product_id");

        Map<String, Object> retData = new HashMap<String, Object>();
        retData.put("key", "value");
        when(this.mockRequestor.processRequest(any(NetkiClient.class), eq("/v1/certificate/token"), eq("POST"), anyString())).thenReturn(mapper.writeValueAsString(retData));

        assertNull(cert.getDataToken());
        try {
            cert.submitUserData();
            fail("Expected Exception");
        } catch(Exception e) {
            assertEquals("Data Token Missing from API Response", e.getMessage());
        }
        assertNull(cert.getDataToken());
    }

    @Test
    public void SubmitUserData_MissingCustomerData() throws Exception {

        Certificate cert = new Certificate(this.mockRequestor);

        try {
            cert.submitUserData();
            fail("Expected Exception");
        } catch(Exception e) {
            assertEquals("Missing Customer Data", e.getMessage());
            verify(this.mockRequestor, never()).processRequest(any(NetkiClient.class), anyString(), anyString(), anyString());
        }
    }

    @Test
    public void SubmitOrder_GoRightWithStripeToken() throws Exception {
        Certificate cert = new Certificate(this.mockRequestor);
        cert.setCustomerData(buildCustomerData());
        cert.setProductId("product_id");
        cert.setDataToken("token");

        Map<String, Object> retData = new HashMap<String, Object>();
        retData.put("order_id", "order_id_value");
        when(this.mockRequestor.processRequest(any(NetkiClient.class), eq("/v1/certificate"), eq("POST"), anyString())).thenReturn(mapper.writeValueAsString(retData));

        cert.submitOrder("stripe_token");
        verify(this.mockRequestor, times(1)).processRequest(
                any(NetkiClient.class),
                eq("/v1/certificate"),
                eq("POST"),
                eq("{\"certdata_token\":\"token\",\"product\":\"product_id\",\"stripe_token\":\"stripe_token\",\"email\":\"user@domain.com\"}")
        );
        assertEquals("order_id_value", cert.getId());
    }

    @Test
    public void SubmitOrder_GoRightWithoutStripeToken() throws Exception {
        Certificate cert = new Certificate(this.mockRequestor);
        cert.setCustomerData(buildCustomerData());
        cert.setProductId("product_id");
        cert.setDataToken("token");

        Map<String, Object> retData = new HashMap<String, Object>();
        retData.put("order_id", "order_id_value");
        when(this.mockRequestor.processRequest(any(NetkiClient.class), eq("/v1/certificate"), eq("POST"), anyString())).thenReturn(mapper.writeValueAsString(retData));

        cert.submitOrder(null);
        verify(this.mockRequestor, times(1)).processRequest(
                any(NetkiClient.class),
                eq("/v1/certificate"),
                eq("POST"),
                eq("{\"certdata_token\":\"token\",\"product\":\"product_id\",\"email\":\"user@domain.com\"}")
        );
        assertEquals("order_id_value", cert.getId());
    }

    @Test
    public void SubmitOrder_MissingResponseOrderId() throws Exception {
        Certificate cert = new Certificate(this.mockRequestor);
        cert.setCustomerData(buildCustomerData());
        cert.setProductId("product_id");
        cert.setDataToken("token");

        Map<String, Object> retData = new HashMap<String, Object>();
        retData.put("something_else", "order_id_value");
        when(this.mockRequestor.processRequest(any(NetkiClient.class), eq("/v1/certificate"), eq("POST"), anyString())).thenReturn(mapper.writeValueAsString(retData));

        try {
            cert.submitOrder(null);
            fail("Expected Exception");
        } catch (Exception e) {
            assertEquals("Order ID Missing from API Response", e.getMessage());
        }

        verify(this.mockRequestor, times(1)).processRequest(
                any(NetkiClient.class),
                eq("/v1/certificate"),
                eq("POST"),
                eq("{\"certdata_token\":\"token\",\"product\":\"product_id\",\"email\":\"user@domain.com\"}")
        );
        assertNull(cert.getId());
    }

    @Test
    public void SubmitOrder_MissingDataToken() throws Exception {
        Certificate cert = new Certificate(this.mockRequestor);
        cert.setCustomerData(buildCustomerData());

        try {
            assertNull(cert.getId());
            cert.submitOrder(null);
            fail("Expected Exception");
        } catch(Exception e) {
            assertEquals("Missing dataToken", e.getMessage());
        }
        verify(this.mockRequestor, never()).processRequest(
                any(NetkiClient.class),
                eq("/v1/certificate"),
                eq("POST"),
                eq("{\"product\":\"product_id\",\"certdata_token\":\"token\",\"email\":\"user@domain.com\",\"stripe_token\":\"stripe_token\"}")
        );
        assertNull(cert.getId());
    }

    @Test
    public void SubmitCSR_GoRight() throws Exception {

        Certificate cert = spy(new Certificate(this.mockRequestor));
        KeyPair keyPair = generateKey("RSA");

        doReturn("CSR_DATA").when(cert).generateCsr(any(KeyPair.class));
        cert.setId("id");

        cert.submitCSR(keyPair);

        verify(this.mockRequestor, times(1)).processRequest(
                any(NetkiClient.class),
                eq("/v1/certificate/id/csr"),
                eq("POST"),
                eq("{\"signed_csr\":\"CSR_DATA\"}")
        );

    }

    @Test
    public void SubmitCSR_MissingID() throws Exception {

        Certificate cert = new Certificate(this.mockRequestor);
        KeyPair keyPair = generateKey("RSA");

        try {
            cert.submitCSR(keyPair);
            fail("Expected Exception");
        } catch(Exception e) {
            assertEquals("Certificate Must Have a Valid Order Number", e.getMessage());
        }

        verify(this.mockRequestor, never()).processRequest(
                any(NetkiClient.class),
                eq("/v1/certificate/id/csr"),
                eq("POST"),
                eq("{\"signed_csr\":\"CSR_DATA\"}")
        );
    }

    @Test
    public void Revoke_GoRight() throws Exception {

        Certificate cert = new Certificate(this.mockRequestor);
        cert.setId("id");

        cert.revoke("reason");

        verify(this.mockRequestor, times(1)).processRequest(
                any(NetkiClient.class),
                eq("/v1/certificate/id"),
                eq("DELETE"),
                eq("{\"revocation_reason\":\"reason\"}")
        );
    }

    @Test
    public void Revoke_NoId() throws Exception {
        Certificate cert = new Certificate(this.mockRequestor);

        try {
            cert.revoke("reason");
            fail("Expected Exception");
        } catch(Exception e) {
            assertEquals("Certificate Must Have a Valid Order Number", e.getMessage());
        }

        verify(this.mockRequestor, never()).processRequest(
                any(NetkiClient.class),
                eq("/v1/certificate/id"),
                eq("DELETE"),
                eq("{\"revocation_reason\":\"reason\"}")
        );
    }

    @Test
    public void GetStatus_GoRight() throws Exception {

        String respJson = "{\"order_error\": \"some error\", \"order_status\": \"status\", \"certificate_bundle\": {\"intermediate\": [\"INT1_PEM\", \"INT2_PEM\"], \"root\": \"ROOT_PEM\", \"certificate\": \"CERT_PEM\"}}";
        doReturn(respJson).when(this.mockRequestor).processRequest(any(NetkiClient.class), eq("/v1/certificate/id"), eq("GET"), anyString());

        Certificate cert = new Certificate(this.mockRequestor);
        cert.setId("id");

        cert.getStatus();

        verify(this.mockRequestor, times(1)).processRequest(
                any(NetkiClient.class),
                eq("/v1/certificate/id"),
                eq("GET"),
                anyString()
        );

        assertEquals(cert.getOrderStatus(), "status");
        assertEquals(cert.getOrderError(), "some error");
        assertEquals(cert.getRootPem(), "ROOT_PEM");
        assertEquals(cert.getCertPem(), "CERT_PEM");
        assertEquals(cert.getIntermediateCerts().size(), 2);
        assertEquals(cert.getIntermediateCerts().get(0), "INT1_PEM");
        assertEquals(cert.getIntermediateCerts().get(1), "INT2_PEM");

    }

    @Test
    public void GetStatus_StatusOnly() throws Exception {

        String respJson = "{\"order_status\": \"status\"}";
        doReturn(respJson).when(this.mockRequestor).processRequest(any(NetkiClient.class), eq("/v1/certificate/id"), eq("GET"), anyString());

        Certificate cert = new Certificate(this.mockRequestor);
        cert.setId("id");

        cert.getStatus();

        verify(this.mockRequestor, times(1)).processRequest(
                any(NetkiClient.class),
                eq("/v1/certificate/id"),
                eq("GET"),
                anyString()
        );

        assertEquals(cert.getOrderStatus(), "status");
        assertNull(cert.getOrderError());
        assertNull(cert.getRootPem());
        assertNull(cert.getCertPem());
        assertEquals(cert.getIntermediateCerts().size(), 0);

    }

    @Test
    public void GetStatus_NoId() throws Exception {

        Certificate cert = new Certificate(this.mockRequestor);

        try {
            cert.getStatus();
            fail("Expected Exception");
        } catch(Exception e) {
            assertEquals("Certificate Must Have a Valid Order Number", e.getMessage());
        }

        verify(this.mockRequestor, never()).processRequest(
                any(NetkiClient.class),
                eq("/v1/certificate/id"),
                eq("GET"),
                anyString()
        );
    }
}
