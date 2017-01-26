# Netki Java Partner Client

![JitPack Badge](https://img.shields.io/github/tag/netkicorp/java-partner-client.svg?label=JitPack)
![Maven Central](https://img.shields.io/maven-central/v/com.netki/netki-partner-client.svg)

This is the Netki Partner library written in Java. It allows you to use the Netki Partner API to CRUD all of your partner data:

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

### Example

```java

String partnerId = "XXXXXXXXXXXXXXXXXXXX";
String apiKey = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";

String partnerKskDerHex = "3056301006072a8648ce3d020106052b8104000a03420004b185c46e29ad04654caa96f168c9b7c7d476103ec07274749c08af8c91a0ded94b04ba8b791b79913776a05a77b6f2408aea43cb4a9e3c30d593a475de82c3f5";
String partnerKeySignDerHex = "30450221008cecbf4776c5d6ef713cd4bb4e4c4db7181ae0629859ccd77aca4a62a39bfc7a0220273145a3baf7338efa8bed231f9af0afcb3b88bacad4dbb653385cd3448d42d3";
KeyPair userKey = ....;

// Access Type: Partner ID & API Key
NetkiClient client = new NetkiClient(partnerId, apiKey, "https://api.netki.com");

// Access Type: Distributed API Access
NetkiClient client = new NetkiClient(partnerKskDerHex, partnerKeySignDerHex, userKey, "https://api.netki.com");

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