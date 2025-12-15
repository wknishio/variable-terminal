package org.vash.vate.tls;

import java.lang.reflect.Method;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class VTTLSUtilities
{
  private static class OverlyOptimisticHostnameVerifier implements HostnameVerifier
  {
    public boolean verify(String arg0, SSLSession arg1)
    {
      return true;
    }
  }
  
  @SuppressWarnings("all")
  private static class OverlyOptimisticTrustManager implements TrustManager, X509TrustManager
  {
    public java.security.cert.X509Certificate[] getAcceptedIssuers()
    {
      return new java.security.cert.X509Certificate[] {};
    }
    
    public boolean isServerTrusted(java.security.cert.X509Certificate[] certs)
    {
      return true;
    }
    
    public boolean isClientTrusted(java.security.cert.X509Certificate[] certs)
    {
      return true;
    }
    
    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) throws java.security.cert.CertificateException
    {
      return;
    }
    
    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) throws java.security.cert.CertificateException
    {
      return;
    }
  }
  
  @SuppressWarnings("all")
  private static class OverlyOptimisticKeyManager implements KeyManager
  {
    public String chooseClientAlias(String[] arg0, Principal[] arg1, Socket arg2)
    {
      return "";
    }
    
    public String chooseServerAlias(String arg0, Principal[] arg1, Socket arg2)
    {
      return "";
    }
    
    public X509Certificate[] getCertificateChain(String arg0)
    {
      return new X509Certificate[] {};
    }
    
    public String[] getClientAliases(String arg0, Principal[] arg1)
    {
      return new String[] {};
    }
    
    public PrivateKey getPrivateKey(String arg0)
    {
      return null;
    }
    
    public String[] getServerAliases(String arg0, Principal[] arg1)
    {
      return new String[] {};
    }
  }
  
  public static boolean disableHttpsTLSVerifications()
  {
    try
    {
      System.setProperty("sun.security.ssl.allowLegacyHelloMessages", "true");
      //System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
      TrustManager[] trustAnything = new TrustManager[]
      { new OverlyOptimisticTrustManager() };
      //KeyManager[] manageNothing = new KeyManager[] { new OverlyOptimisticKeyManager() };
      SSLContext unverifiedTLS = SSLContext.getInstance("TLS");
      unverifiedTLS.init(null, trustAnything, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(unverifiedTLS.getSocketFactory());
      HttpsURLConnection.setDefaultHostnameVerifier(new OverlyOptimisticHostnameVerifier());
      try
      {
        Method setDefaultMethod = SSLContext.class.getDeclaredMethod("setDefault", SSLContext.class);
        setDefaultMethod.invoke(null, unverifiedTLS);
        // SSLContext.setDefault(unverifiedSSL);
      }
      catch (Throwable ei)
      {
        //ei.printStackTrace();
        // return false;
      }
    }
    catch (Throwable e)
    {
      //e.printStackTrace();
      return false;
    }
    return true;
  }
  
  public static SSLContext createOptimisticTLSClientContext() throws Throwable
  {
    TrustManager[] trustAnything = new TrustManager[]
    { new OverlyOptimisticTrustManager() };
    SSLContext unverifiedTLS = SSLContext.getInstance("TLS");
    unverifiedTLS.init(null, trustAnything, new SecureRandom());
    return unverifiedTLS;
  }
  
//  public static SSLContext createUnsafeTLSServerContext() throws Throwable
//  {
//    Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(createSelfSignedCertificateData(1024)));
//    KeyStore keyStore = KeyStore.getInstance("PKCS12");
//    keyStore.load(null, null);
//    keyStore.setCertificateEntry("", certificate);
//    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//    trustManagerFactory.init(keyStore);
//    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//    keyManagerFactory.init(keyStore, null);
//    SSLContext sslContext = SSLContext.getInstance("TLS");
//    sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
//    return sslContext;
//  }
//  
//  private static byte[] createSelfSignedCertificateData(int bitStrength) throws Throwable
//  {
//    RSAKeyPairGenerator generator = new RSAKeyPairGenerator();
//    RSAKeyGenerationParameters parameters = new RSAKeyGenerationParameters(BigInteger.valueOf(65537), new SecureRandom(), bitStrength, 128);
//    generator.init(parameters);
//    AsymmetricCipherKeyPair keyPair = generator.generateKeyPair();
//    
//    X500Name subjectName = new X500Name("CN=" + UUID.randomUUID());
//    BigInteger serial = BigInteger.valueOf(new SecureRandom().nextLong());
//    Date notBefore = new Date(System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 30)); // 30 days ago
//    Date notAfter = new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365 * 10)); // 10 years from now
//    
//    String signatureAlgorithm = "SHA512WITHRSA";
//    
//    DefaultSignatureAlgorithmIdentifierFinder sigAlgFinder = new DefaultSignatureAlgorithmIdentifierFinder();
//    AlgorithmIdentifier sigAlgId = sigAlgFinder.find(signatureAlgorithm);
//    DigestAlgorithmIdentifierFinder digAlgFinder = new DefaultDigestAlgorithmIdentifierFinder();
//    AlgorithmIdentifier digAlgId = digAlgFinder.find(sigAlgId);
//    
//    ContentSigner signer = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(keyPair.getPrivate());
//    
//    X509v3CertificateBuilder certBuilder = new BcX509v3CertificateBuilder(subjectName, serial, notBefore, notAfter, subjectName, keyPair.getPublic());
//    
//    X509CertificateHolder certificateHolder = certBuilder.build(signer);
//    
//    byte[] encodedCert = certificateHolder.getEncoded();
//    return encodedCert;
//  }
}