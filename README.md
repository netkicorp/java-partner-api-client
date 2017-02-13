# Netki Java Partner Client

![JitPack Badge](https://img.shields.io/github/tag/netkicorp/java-partner-client.svg?label=JitPack)
![Maven Central](https://img.shields.io/maven-central/v/com.netki/netki-partner-client.svg)

This is the Netki Partner library written in Java. It allows you to use the Netki API to CRUD all of your partner data:

* Wallet Names
* Domains
* Partners

### Library Inclusion

This library can be included directly from Maven Central / OSS Sonatype.

##### Maven

    <dependency>
        <groupId>com.netki</groupId>
        <artifactId>netki-partner-client</artifactId>
    </dependency>

##### Gradle

    'com.netki:netki-partner-client:0.0.1+'

### Wallet Name Example

```java

String partnerId = "partner_id";
String apiKey = "api_key";

// Access Type: Partner ID & API Key
NetkiClient client = new NetkiClient(partnerId, apiKey, "https://api.netki.com");

List<Domain> domains;
List<Partner> partners;
List<WalletName> walletNames;
List<WalletName> filteredWalletNames;

// Get All Domains
domains = client.getDomains();

// Create a new domain not belonging to a partner
Domain newTestDomain = client.createDomain("testdomain.com", null);

// Get All Partners
partners = client.getPartners();

// Create a new partner
Partner newPartner = client.createPartner("Partner");

// Create a new domain belonging to a partner
Domain partnerTestDomain = client.createDomain("partnerdomain.com", newPartner);

// Delete Domain 
partnerTestDomain.delete();

// Delete Partner
newPartner.delete();

// Get All Wallet Names
walletNames = client.getWalletNames();

// Update a Wallet Name's BTC Wallet Address
WalletName walletNameToUpdate = walletNames.get(0);
walletNameToUpdate.setCurrencyAddress("btc", "3J98t1WpEZ73CNmQviecrnyiWrnqRhWNLy");
walletNameToUpdate.save();

// Create a New Wallet Name
WalletName walletName = client.createWalletName("testdomain.com", "testwallet", "externalId");
walletName.setCurrencyAddress("btc", "1CpLXM15vjULK3ZPGUTDMUcGATGR9xGitv");
walletName.save();

// Add Litecoin Wallet Address
walletName.setCurrencyAddress("ltc", "LQVeWKif6kR1Z5KemVcijyNTL2dE3SfYQM");
walletName.save();

// Get all Wallet Names for a Domain
filteredWalletNames = client.getWalletNames("testdomain.com", null);

// Get all Wallet Names matching an External ID
filteredWalletNames = client.getWalletNames(null, "externalId");

// Get all Wallet Names for a Domain matching an External ID
filteredWalletNames = client.getWalletNames("testdomain.com", "externalId");

// Delete Wallet Name
walletName.delete();
```

# Distributed API Access for Wallet Names
When using Distributed API Access, the client has access only to their Wallet Name(s) created 
using their user's public key.
```java

String partnerKskDerHex = "3056301006072a8648ce3d020106052...";
String partnerKeySignDerHex = "30450221008cecbf4776c5d6ef713c...";
KeyPair userKey;

// Generate New ECDSA Key
try {
    Security.addProvider(new BouncyCastleProvider());
    KeyPairGenerator generator = KeyPairGenerator.getInstance("ECDSA", "SC");
    ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
    generator.initialize(ecSpec, new SecureRandom());
    userkey = generator.generateKeyPair();
} catch (Exception e) {
    e.printStackTrace();
}

// Access Type: Distributed API Access (Remote Access for WalletName CRUD)
NetkiClient client = new NetkiClient(partnerKskDerHex, partnerKeySignDerHex, userKey, "https://api.netki.com");

```

# Certificate API Access with Partner Signed Authentication NetkiClient
```java

NetkiClient client = new NetkiClient(partnerId, userKey);

Security.addProvider(new BouncyCastleProvider());
KeyPair keyPair;
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");

// Generate New RSA Key
try {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "SC");
    generator.initialize(1024);
    keyPair = generator.generateKeyPair();
} catch (Exception e) {
    e.printStackTrace();
}

// Get Available Products
List<Product> products = client.getAvailableProducts();
Product selectedProduct = products.get(0);

// Create Certificate
Certificate cert = client.createCertificate();
cert.setProductId(selectedProduct.getId());

// Setup CustomerData for Submission
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

// Create Multiple IdentityDocument objects (in this case: Driver's License and Passport)
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
cert.setCustomerData(cd);
try {
    
    // Submit User Data for Tokenization
    cert.submitUserData();
    
    // Submit Certificate Order
    cert.submitOrder();
    
    // Submit CSR Based on CustomerData
    cert.submitCsr();
    
    // Poll for order completion
    while(!cert.isOrderComplete()) {
        cert.getStatus();
        Thread.sleep(10000);
    }
} catch (Exception e) {
    e.printStackTrace();
}
```