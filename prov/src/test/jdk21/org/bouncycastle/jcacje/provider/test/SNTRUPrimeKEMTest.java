package org.bouncycastle.jcacje.provider.test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.KEM;
import javax.crypto.SecretKey;

import junit.framework.TestCase;
import org.bouncycastle.jcajce.spec.KEMParameterSpec;
import org.bouncycastle.jcajce.spec.KTSParameterSpec;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.bouncycastle.pqc.jcajce.spec.SNTRUPrimeParameterSpec;
import org.bouncycastle.util.Arrays;


public class SNTRUPrimeKEMTest
    extends TestCase
{
    public void setUp()
    {
        if (Security.getProvider(BouncyCastlePQCProvider.PROVIDER_NAME) == null)
        {
            Security.addProvider(new BouncyCastlePQCProvider());
        }
    }
    
    public void testKEM()
            throws Exception
    {
        // Receiver side
        KeyPairGenerator g = KeyPairGenerator.getInstance("SNTRUPrime");

        g.initialize(SNTRUPrimeParameterSpec.sntrup653, new SecureRandom());

        KeyPair kp = g.generateKeyPair();
        PublicKey pkR = kp.getPublic();

        // Sender side
        KEM kemS = KEM.getInstance("SNTRUPrime"); //Should the name be "SNTRUPrime-KEM" ?
        KTSParameterSpec ktsSpec = null;
        KEM.Encapsulator e = kemS.newEncapsulator(pkR, ktsSpec, null);
        KEM.Encapsulated enc = e.encapsulate();
        SecretKey secS = enc.key();
        byte[] em = enc.encapsulation();
        byte[] params = enc.params();

        // Receiver side
        KEM kemR = KEM.getInstance("SNTRUPrime");
//        AlgorithmParameters algParams = AlgorithmParameters.getInstance("SNTRUPrime");
//        algParams.init(params);
//        SNTRUPrimeParameterSpec specR = algParams.getParameterSpec(SNTRUPrimeParameterSpec.class);
        KEM.Decapsulator d = kemR.newDecapsulator(kp.getPrivate(), ktsSpec);
        SecretKey secR = d.decapsulate(em);

        // secS and secR will be identical
        assertEquals(secS.getAlgorithm(), secR.getAlgorithm());
        assertTrue(Arrays.areEqual(secS.getEncoded(), secR.getEncoded()));
    }

    public void testBasicKEMAES()
            throws Exception
    {
        if (Security.getProvider(BouncyCastlePQCProvider.PROVIDER_NAME) == null)
        {
            Security.addProvider(new BouncyCastlePQCProvider());
        }
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("SNTRUPrime", "BCPQC");
        kpg.initialize(SNTRUPrimeParameterSpec.sntrup653, new SecureRandom());

        performKEM(kpg.generateKeyPair(), new KEMParameterSpec("AES"));
        performKEM(kpg.generateKeyPair(),0, 16, "AES", new KEMParameterSpec("AES"));
        performKEM(kpg.generateKeyPair(), new KEMParameterSpec("AES-KWP"));

        try
        {
            performKEM(kpg.generateKeyPair(),0, 16, "AES-KWP", new KEMParameterSpec("AES"));
            fail();
        }
        catch (Exception ex)
        {
        }

        kpg.initialize(SNTRUPrimeParameterSpec.sntrup1013, new SecureRandom());
        performKEM(kpg.generateKeyPair(), new KEMParameterSpec("AES"));



    }

    public void testBasicKEMCamellia()
            throws Exception
    {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("SNTRUPrime", "BCPQC");
        kpg.initialize(SNTRUPrimeParameterSpec.sntrup653, new SecureRandom());

        performKEM(kpg.generateKeyPair(), new KTSParameterSpec.Builder("Camellia", 256).build());
        performKEM(kpg.generateKeyPair(), new KTSParameterSpec.Builder("Camellia-KWP", 256).build());
    }

    public void testBasicKEMSEED()
            throws Exception
    {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("SNTRUPrime", "BCPQC");
        kpg.initialize(SNTRUPrimeParameterSpec.sntrup653, new SecureRandom());

        performKEM(kpg.generateKeyPair(), new KTSParameterSpec.Builder("SEED", 128).build());
    }

    public void testBasicKEMARIA()
            throws Exception
    {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("SNTRUPrime", "BCPQC");
        kpg.initialize(SNTRUPrimeParameterSpec.sntrup653, new SecureRandom());

        performKEM(kpg.generateKeyPair(), new KEMParameterSpec("ARIA"));
        performKEM(kpg.generateKeyPair(), new KEMParameterSpec("ARIA-KWP"));
    }

    private void performKEM(KeyPair kp, int from, int to, String algorithm, KTSParameterSpec ktsParameterSpec)
            throws Exception
    {
        PublicKey pkR = kp.getPublic();

        // Sender side
        KEM kemS = KEM.getInstance("SNTRUPrime");
        KEM.Encapsulator e = kemS.newEncapsulator(pkR, ktsParameterSpec, null);
        KEM.Encapsulated enc = e.encapsulate(from, to, algorithm);
        SecretKey secS = enc.key();
        byte[] em = enc.encapsulation();

        // Receiver side
        KEM kemR = KEM.getInstance("SNTRUPrime");
        KEM.Decapsulator d = kemR.newDecapsulator(kp.getPrivate(), ktsParameterSpec);
        SecretKey secR = d.decapsulate(em, from, to, algorithm);

        // secS and secR will be identical
        assertEquals(secS.getAlgorithm(), secR.getAlgorithm());
        assertTrue(Arrays.areEqual(secS.getEncoded(), secR.getEncoded()));
    }

    private void performKEM(KeyPair kp, KTSParameterSpec ktsParameterSpec)
            throws Exception
    {
        PublicKey pkR = kp.getPublic();

        // Sender side
        KEM kemS = KEM.getInstance("SNTRUPrime");
        KEM.Encapsulator e = kemS.newEncapsulator(pkR, ktsParameterSpec, null);
        KEM.Encapsulated enc = e.encapsulate();
        SecretKey secS = enc.key();
        byte[] em = enc.encapsulation();

        // Receiver side
        KEM kemR = KEM.getInstance("SNTRUPrime");
//        KTSParameterSpec RktsParameterSpec = new KTSParameterSpec.Builder(
//                ktsParameterSpec.getKeyAlgorithmName(),
//                enc.key().getEncoded().length
//        ).withParameterSpec(ktsParameterSpec).build();
        KEM.Decapsulator d = kemR.newDecapsulator(kp.getPrivate(), ktsParameterSpec);
        SecretKey secR = d.decapsulate(em);

        // secS and secR will be identical
        assertEquals(secS.getAlgorithm(), secR.getAlgorithm());
        assertTrue(Arrays.areEqual(secS.getEncoded(), secR.getEncoded()));
    }
}
