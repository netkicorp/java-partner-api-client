package com.netki;

import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.jce.spec.ECParameterSpec;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;

class TestUtil {

    // Utility Functionality
    static KeyPair generateKey(String type) {

        try {
            Security.addProvider(new BouncyCastleProvider());
            KeyPairGenerator generator;

            if(type.equals("RSA")) {
                generator = KeyPairGenerator.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME);
                generator.initialize(1024);
            } else if (type.equals("ECDSA")) {
                ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
                generator = KeyPairGenerator.getInstance("ECDSA", BouncyCastleProvider.PROVIDER_NAME);
                generator.initialize(ecSpec, new SecureRandom());
            } else {
                return null;
            }
            return generator.generateKeyPair();

        } catch (Exception e) {
            return null;
        }
    }

}
